package net.weavemc.loader.impl.mod

import net.weavemc.loader.api.mod.ModContainer
import net.weavemc.loader.api.mod.ModRegistry

object ModRegistryImpl : ModRegistry {
    override fun isModLoaded(id: String): Boolean {
        TODO("Not yet implemented")
    }

    override fun getModContainer(id: String): ModContainer? {
        TODO("Not yet implemented")
    }

    override fun getAllMods(): Collection<ModContainer> {
        TODO("Not yet implemented")
    }
}