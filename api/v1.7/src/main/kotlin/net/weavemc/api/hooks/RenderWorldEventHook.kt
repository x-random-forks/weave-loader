@file:Suppress("invisible_reference", "invisible_member")

package net.weavemc.api.hooks

import net.weavemc.api.Hook
import net.weavemc.api.bytecode.asm
import net.weavemc.api.bytecode.callEvent
import net.weavemc.api.bytecode.internalNameOf
import net.weavemc.api.bytecode.named
import net.weavemc.api.event.RenderWorldEvent
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.LdcInsnNode

/**
 * Corresponds to [RenderWorldEvent].
 */
class RenderWorldEventHook : Hook("net/minecraft/client/renderer/EntityRenderer") {

    /**
     * Inserts a call to [RenderWorldEvent]'s constructor at the head of
     * [net.minecraft.client.renderer.EntityRenderer.renderWorld], which
     * is called in the event of any world render.
     */
    override fun transform(node: ClassNode, cfg: AssemblerConfig) {
        val renderWorld = node.methods.named("renderWorld")

        renderWorld.instructions.insertBefore(
            renderWorld.instructions.find { it is LdcInsnNode && it.cst == "hand" },
            asm {
                new(internalNameOf<RenderWorldEvent>())
                dup
                fload(1)
                invokespecial(internalNameOf<RenderWorldEvent>(), "<init>", "(F)V")
                callEvent()
            }
        )
    }
}
