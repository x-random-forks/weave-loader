package net.weavemc.loader.impl.bootstrap

import net.weavemc.api.Tweaker
import net.weavemc.internals.ModConfig
import net.weavemc.loader.impl.WeaveLoaderImpl
import net.weavemc.loader.impl.bootstrap.transformer.ArgumentSanitizer
import net.weavemc.loader.impl.bootstrap.transformer.ModInitializerHook
import net.weavemc.loader.impl.bootstrap.transformer.URLClassLoaderTransformer
import net.weavemc.loader.impl.util.*
import net.weavemc.loader.impl.util.FileManager.ModJar
import java.awt.GraphicsEnvironment
import java.lang.instrument.Instrumentation
import java.util.jar.JarFile

/**
 * The JavaAgent's `premain()` method, this is where initialization of Weave Loader begins.
 * Weave Loader's initialization begins by instantiating [WeaveLoaderImpl]
 */
@Suppress("UNUSED_PARAMETER")
fun premain(opt: String?, inst: Instrumentation) {
    println("[Weave] Attached Weave")

    setGameInfo()
    callTweakers(inst)

    inst.addTransformer(URLClassLoaderTransformer)
    inst.addTransformer(ModInitializerHook(inst))

    inst.addTransformer(ArgumentSanitizer, true)
    inst.retransformClasses(Class.forName("sun.management.RuntimeImpl", false, ClassLoader.getSystemClassLoader()))
    inst.removeTransformer(ArgumentSanitizer)

    // Prevent ichor prebake
    System.setProperty("ichor.prebakeClasses", "false")

    // Hack: sometimes the state is improperly initialized, which causes Swing to feel like it is headless?
    // Calling this solves the problem
    GraphicsEnvironment.isHeadless()

    // initialize bootstrap
    Bootstrap.bootstrap(inst)
}

private fun callTweakers(inst: Instrumentation) {
    println("[Weave] Calling tweakers")

    val tweakers = FileManager
        .getMods()
        .map(ModJar::file)
        .map(::JarFile)
        .map(JarFile::configOrFatal)
        .flatMap(ModConfig::tweakers)

    for (tweaker in tweakers) {
        instantiate<Tweaker>(tweaker).tweak(inst)
    }
}

fun main() {
    fatalError("This is not how you use Weave! Please refer to the readme for instructions.")
}