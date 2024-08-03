package net.weavemc.loader.impl

import com.grappenmaker.mappings.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import me.xtrm.klog.Logger
import net.weavemc.internals.GameInfo
import net.weavemc.loader.api.Hook
import net.weavemc.loader.api.ModInitializer
import net.weavemc.loader.api.WeaveLoader
import net.weavemc.loader.api.mod.ModContact
import net.weavemc.loader.api.mod.ModContributor
import net.weavemc.loader.api.mod.ModLicense
import net.weavemc.loader.impl.bootstrap.transformer.URLClassLoaderAccessor
import net.weavemc.loader.impl.mixin.SandboxedMixinLoader
import net.weavemc.loader.impl.mod.ModContainerImpl
import net.weavemc.loader.impl.mod.model.ModContactImpl
import net.weavemc.loader.impl.mod.model.ModContributorImpl
import net.weavemc.loader.impl.mod.model.ModMetadataImpl
import net.weavemc.loader.impl.mod.model.version.RawVersionImpl
import net.weavemc.loader.impl.mod.model.version.SemanticVersionImpl
import net.weavemc.loader.impl.util.*
import org.objectweb.asm.tree.ClassNode
import java.io.File
import java.lang.instrument.Instrumentation
import java.nio.file.Path
import java.util.jar.JarFile
import kotlin.io.path.Path

private const val CONTRIBUTORS_FILE_PATH = "/weave-contributors.json"

/**
 * The main class of the Weave Loader.
 */
internal class WeaveLoaderImpl(
    private val classLoader: URLClassLoaderAccessor,
    private val instrumentation: Instrumentation,
    private val mappedModJars: List<File>
) : WeaveLoader {
    private val logger = Logger(WeaveLoaderImpl::class.java.name)

    override val gameDir: Path
        get() = GameInfo.gameDir
    override val modsDir: Path
        get() = FileManager.MODS_DIRECTORY
    override val configDir: Path
        get() = FileManager.CONFIG_DIRECTORY

    override val mods: MutableList<ModContainerImpl> = mutableListOf()

//    /**
//     * Stored list of [WeaveMod]s.
//     *
//     * @see ModConfig
//     */
//    private val mods = mutableListOf(
//        // Fake base game mod so dependencies can be made on them
//        // TODO: proper versioned dependencies, eg. optional, version greater/less than...
//        WeaveMod(
//            modId = "minecraft", config = ModConfig(
//                name = "Minecraft",
//                modId = "minecraft",
//                entryPoints = emptyList(),
//                mixinConfigs = emptyList(),
//                hooks = emptyList(),
//                tweakers = emptyList(),
//                namespace = MappingsHandler.environmentNamespace,
//                dependencies = emptyList(),
//                compiledFor = GameInfo.version.versionName
//            )
//        )
//    )

    private val mixinInstances = mutableMapOf<String, SandboxedMixinLoader>()

    companion object {
        private var INSTANCE: WeaveLoaderImpl? = null

        @JvmStatic
        internal fun getInstance(): WeaveLoaderImpl = INSTANCE
            ?: fatalError("Attempted to retrieve WeaveLoader instance before it has been instantiated!")
    }

    init {
        logger.info("Initializing Weave")

        INSTANCE = this
        launchStart = System.currentTimeMillis()
        instrumentation.addTransformer(InjectionHandler)

        addBuiltinMods()

        finalize()
    }

    private fun finalize() {
        logger.trace("Finalizing Weave loading...")
        mappedModJars.forEach { it.registerAsMod() }
        logger.trace("Verifying dependencies")
        verifyDependencies()
        logger.trace("Populating mixin modifiers")
        populateMixinModifiers()
        logger.trace("Setting up access wideners")
        setupAccessWideners()

        logger.trace("Calling preInit() for mods")
        // TODO remove
        // Invoke preInit() once everything is done.
        mods.forEach { weaveMod ->
            weaveMod.config.entryPoints.forEach { entrypoint ->
                runCatching {
                    logger.debug("Calling $entrypoint#preInit")
                    instantiate<ModInitializer>(entrypoint)
                }.onFailure {
                    logger.error("Failed to instantiate $entrypoint#preInit", it)
                }.onSuccess {
                    runCatching {
                        @Suppress("DEPRECATION")
                        it.preInit(instrumentation)
                    }.onFailure {
                        logger.error("Exception thrown when invoking $entrypoint#preInit", it)
                    }
                }
            }
        }

        logger.info("Weave initialized in ${System.currentTimeMillis() - launchStart}ms")
        updateLaunchTimes()
    }

    /**
     * Invokes Weave Mods' init. @see net.weavemc.api.ModInitializer
     * Invoked at the head of Minecraft's main method.
     *
     * @see [net.weavemc.loader.impl.bootstrap.transformer.ModInitializerHook]
     */
    @Suppress("unused")
    fun initializeMods() {
        mods.forEach { weaveMod ->
            weaveMod.config.entryPoints.forEach { entrypoint ->
                runCatching {
                    instantiate<ModInitializer>(entrypoint)
                }.onFailure {
                    logger.error("Failed to instantiate $entrypoint#init", it)
                }.onSuccess {
                    runCatching {
                        it.init()
                    }.onFailure {
                        logger.error("Exception thrown when invoking $entrypoint#init", it)
                    }
                }
            }
        }
    }

    private fun verifyDependencies() {
        val duplicates = mods.groupingBy { it.modId }.eachCount().filterValues { it > 1 }.keys
        if (duplicates.isNotEmpty()) fatalError("Duplicate mods ${duplicates.joinToString()} were found")

        val dependencyGraph = mods.associate { it.modId to it.config.dependencies }
        val seen = hashSetOf<String>()
        dependencyGraph.keys.forEach { toDetermine ->
            // soFar = List to keep order
            // Supposedly the list will be small enough such that a linear search is efficient enough
            fun verify(curr: String, soFar: List<String>) {
                if (curr in soFar) fatalError(
                    "Circular dependency: $toDetermine's dependency graph eventually " +
                            "meets $curr again through ${(soFar + curr).joinToString(" -> ")}"
                )

                if (!seen.add(curr)) return
                val deps = dependencyGraph[curr] ?: fatalError("Dependency $curr for mod $toDetermine is not available")
                deps.forEach { verify(it, soFar + curr) }
            }

            verify(toDetermine, emptyList())
        }
    }

    private fun mixinForNamespace(namespace: String) = mixinInstances.getOrPut(namespace) {
        logger.debug("Creating a new SandboxedMixinLoader for namespace $namespace")
        val parent = classLoader.weaveBacking
        SandboxedMixinLoader(
            parent = parent,
            loader = ClasspathLoaders.fromLoader(parent)
                .remappingNames(MappingsHandler.mergedMappings.mappings, "official", namespace),
        ).apply { state.initialize() }
    }

    private fun populateMixinModifiers() {
        for (ns in MappingsHandler.mergedMappings.mappings.namespaces) {
            val state = mixinForNamespace(ns).state
            val targets = state.findTargets(state.transformer)
            if (targets.isEmpty()) continue

            val mapper = MappingsHandler.mapper(ns, MappingsHandler.environmentNamespace)
            InjectionHandler.registerModifier(object : Modifier {
                override val namespace = ns
                override val targets = targets.mapTo(hashSetOf()) { mapper.map(it.replace('.', '/')) }
                override fun apply(node: ClassNode, cfg: Hook.AssemblerConfig) {
                    cfg.computeFrames()
                    state.transform(node.name, node)
                }
            })
        }
    }

    private fun setupAccessWideners() {
        val tree = mods.flatMap { it.config.accessWideners }.mapNotNull { aw ->
            val res = javaClass.classLoader.getResourceAsStream(aw) ?: return@mapNotNull let {
                println("[Weave] Could not load access widener configuration $aw")
                null
            }

            loadAccessWidener(res.readBytes().decodeToString().trim().lines())
                .remap(MappingsHandler.mergedMappings.mappings, MappingsHandler.environmentNamespace)
        }.join().toTree()

        InjectionHandler.registerModifier(object : Modifier {
            override val namespace = MappingsHandler.environmentNamespace
            override val targets = tree.classes.mapTo(hashSetOf()) { it.key }

            override fun apply(node: ClassNode, cfg: Hook.AssemblerConfig) = node.applyWidener(tree)
        })
    }

    private fun buildContributors(): List<ModContributor> {
        val url = javaClass.classLoader.getResource(CONTRIBUTORS_FILE_PATH)
            ?: fatalError("Failed to find $CONTRIBUTORS_FILE_PATH")
        val text = url.readText()

        @Serializable
        data class InternalContributor(
            val login: String,
            val name: String,
        )

        return Json.decodeFromString<List<InternalContributor>>(text).map {
            ModContributorImpl(
                name = it.name,
                contact = mapOf("github" to it.login),
            )
        }
    }

    private fun addBuiltinMods() {
        mods += ModContainerImpl(
            ModMetadataImpl(
                "weave-loader",
                SemanticVersionImpl("1.0.0"),
                "Weave Loader",
                "A universal mod loader for Minecraft",
                buildContributors(),
                ModContactImpl(
                    issues = "https://github.com/WeaveMC/Weave-Loader/issues",
                    sources = "https://github.com/WeaveMC/Weave-Loader",
                ),
                listOf(ModLicense.of("GPL-3.0"))
            ),
            Path(""),
            builtin = true
        )
        //TODO: add java as a minecraft dependency
        mods += ModContainerImpl(
            ModMetadataImpl(
                "minecraft",
                RawVersionImpl(GameInfo.versionString),
                "Minecraft",
                "The base game",
                listOf(ModContributorImpl("Mojang")),
                ModContactImpl(
                    homepage = "https://minecraft.net",
                    issues = "https://bugs.mojang.com/secure/Dashboard.jspa",
                ),
                listOf(ModLicense.of("ARR"))
            ),
            Path(""),
            builtin = true
        )
    }

    /**
     * Registers mod's hooks and mixins then adds to mods list for later instantiation
     */
    private fun File.registerAsMod() {
        logger.trace("Registering mod $name")
        classLoader.addWeaveURL(this.toURI().toURL())

        JarFile(this).use { jar ->
            val config = jar.configOrFatal()
            val modId = config.modId

            // Added a backup classloader search for precautionary measures
            instrumentation.appendToSystemClassLoaderSearch(jar)

            config.hooks.forEach { hook ->
                logger.trace("Registering hook $hook")
                InjectionHandler.registerModifier(ModHook(config.namespace, instantiate(hook)))
            }

            val state = mixinForNamespace(config.namespace).state
            config.mixinConfigs.forEach { state.registerMixin(modId, it) }

            mods += ModContainerImpl(this.toPath(), config)
            logger.trace("Registered mod $name")
        }
    }
}