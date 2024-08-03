package net.weavemc.loader.impl.mod.model

import net.weavemc.loader.api.mod.*

internal class ModMetadataImpl(
    override val id: String,
    override val version: Version,
    override val name: String,
    override val description: String?,
    override val contributors: Collection<ModContributor> = emptyList(),
    override val contact: ModContact,
    override val license: Collection<ModLicense> = emptyList(),
) : ModMetadata {
    override fun getIcon(size: Int): String {
        TODO("Not yet implemented")
    }
}