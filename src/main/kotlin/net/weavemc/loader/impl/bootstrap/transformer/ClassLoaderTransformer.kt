package net.weavemc.loader.impl.bootstrap.transformer

import net.weavemc.internals.asm
import net.weavemc.internals.internalNameOf
import net.weavemc.internals.visitAsm
import net.weavemc.loader.impl.mixin.LoaderClassWriter
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.LabelNode
import java.net.URL
import java.net.URLClassLoader

interface URLClassLoaderAccessor {
    val backing: ClassLoader
    fun addWeaveURL(url: URL)
}

object URLClassLoaderTransformer : SafeTransformer {
    override fun transform(loader: ClassLoader?, className: String, originalClass: ByteArray): ByteArray? {
        if (loader == null)
            return null

        val reader = ClassReader(originalClass)
        if (reader.superName != internalNameOf<URLClassLoader>()) return null

        val node = ClassNode()
        reader.accept(node, 0)

        node.interfaces.add(internalNameOf<URLClassLoaderAccessor>())
        node.visitMethod(Opcodes.ACC_PUBLIC, "addWeaveURL", "(Ljava/net/URL;)V", null, null).visitAsm {
            aload(0)
            aload(1)
            invokevirtual(node.name, "addURL", "(Ljava/net/URL;)V")
            _return
        }

        node.visitMethod(Opcodes.ACC_PUBLIC, "getBacking", "()Ljava/lang/ClassLoader;", null, null).visitAsm {
            aload(0)
            areturn
        }

        val loadClassInject = asm {
            // TODO: dry
            aload(1)
            ldc("net.weavemc.loader.impl.bootstrap")
            invokevirtual("java/lang/String", "startsWith", "(Ljava/lang/String;)Z")

            val notBootstrap = LabelNode()
            ifeq(notBootstrap)

            invokestatic("java/lang/ClassLoader", "getSystemClassLoader", "()Ljava/lang/ClassLoader;")
            aload(1)
            invokevirtual("java/lang/ClassLoader", "loadClass", "(Ljava/lang/String;)Ljava/lang/Class;")
            areturn

            +notBootstrap
        }

        listOf("loadClass", "findClass")
            .mapNotNull { t -> node.methods.find { it.name == t } }
            .forEach { it.instructions.insert(loadClassInject) }

        return LoaderClassWriter(loader, reader, ClassWriter.COMPUTE_FRAMES).also { node.accept(it) }.toByteArray()
    }
}