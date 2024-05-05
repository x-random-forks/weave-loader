package net.weavemc.loader.impl.bootstrap

import net.weavemc.internals.GameInfo
import net.weavemc.loader.impl.bootstrap.transformer.ApplicationWrapper
import net.weavemc.loader.impl.bootstrap.transformer.SafeTransformer
import net.weavemc.loader.impl.bootstrap.transformer.URLClassLoaderAccessor
import net.weavemc.loader.impl.bootstrap.transformer.URLClassLoaderTransformer
import net.weavemc.loader.impl.util.fatalError
import java.lang.instrument.Instrumentation

object Bootstrap {
    fun bootstrap(inst: Instrumentation) {
        inst.addTransformer(object: SafeTransformer {
            override fun transform(loader: ClassLoader?, className: String, originalClass: ByteArray): ByteArray? {
                if (className == "net/minecraft/client/main/Main") {
                    if (loader == ClassLoader.getSystemClassLoader())
                        return ApplicationWrapper.insertWrapper(className, originalClass)

                    printBootstrap(loader)

                    // remove bootstrap transformers
                    inst.removeTransformer(this)
                    inst.removeTransformer(URLClassLoaderTransformer)

                    val clAccessor = if (loader is URLClassLoaderAccessor) loader
                    else fatalError("Failed to transform URLClassLoader to implement URLClassLoaderAccessor. Impossible to recover")

                    runCatching {
                        clAccessor.addWeaveURL(javaClass.protectionDomain.codeSource.location)
                    }.onFailure {
                        it.printStackTrace()
                        fatalError("Failed to deliberately add Weave to the target classloader")
                    }

                    println("[Weave] Bootstrapping complete.")

                    /**
                     * Start the Weave Loader initialization phase
                     */
                    val wlc = loader.loadClass("net.weavemc.loader.impl.WeaveLoaderImpl")
                    println("[Weave] Starting Weave Loader initialization...")
                    wlc.constructors.forEach {
                        println(" - Constructor: $it")
                    }
                    wlc.getConstructor(
                        URLClassLoaderAccessor::class.java,
                        Instrumentation::class.java
                    ).newInstance(clAccessor, inst)
                }

                return null
            }
        })
    }

    private fun printBootstrap(loader: ClassLoader?) {
        println(
            """
            [Weave] Bootstrapping...
                - Version: ${GameInfo.version.versionName}
                - Client: ${GameInfo.client.clientName}
                - Loader: $loader
            """.trimIndent()
        )
    }


}