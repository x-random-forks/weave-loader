@file:Suppress("invisible_reference", "invisible_member")

package net.weavemc.weave.api.hooks

import net.weavemc.weave.api.Hook
import net.weavemc.weave.api.bytecode.asm
import net.weavemc.weave.api.bytecode.callEvent
import net.weavemc.weave.api.bytecode.internalNameOf
import net.weavemc.weave.api.bytecode.search
import net.weavemc.weave.api.event.RenderGameOverlayEvent
import net.weavemc.weave.api.not
import org.objectweb.asm.Opcodes.RETURN
import org.objectweb.asm.tree.ClassNode

internal class RenderGameOverlayHook : Hook(
    !"net/minecraft/client/gui/GuiIngame",
    "net/minecraftforge/client/GuiIngameForge"
) {
    override fun transform(node: ClassNode, cfg: AssemblerConfig) {
        val mn = node.methods.search(!"renderGameOverlay", "V", "F", "Z", "I", "I")

        mn.instructions.insert(asm {
            new(internalNameOf<RenderGameOverlayEvent.Pre>())
            dup
            fload(1)
            invokespecial(
                internalNameOf<RenderGameOverlayEvent.Pre>(),
                "<init>",
                "(F)V"
            )
            callEvent()
        })

        mn.instructions.insertBefore(mn.instructions.findLast { it.opcode == RETURN }, asm {
            new(internalNameOf<RenderGameOverlayEvent.Post>())
            dup
            fload(1)
            invokespecial(
                internalNameOf<RenderGameOverlayEvent.Post>(),
                "<init>",
                "(F)V"
            )
            callEvent()
        })
    }
}