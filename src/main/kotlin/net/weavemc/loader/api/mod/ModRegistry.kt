package net.weavemc.loader.api.mod

/**
 * This interface is used to query the loaded
 */
interface ModRegistry {
    fun isModLoaded(id: String): Boolean

    fun getModContainer(id: String): ModContainer?

    fun getAllMods(): Collection<ModContainer>
}