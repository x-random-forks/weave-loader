package net.weavemc.loader.impl.mod.model

import net.weavemc.loader.api.mod.ModContributor

internal data class ModContributorImpl(
    override val name: String,
    override val contact: Map<String, String> = emptyMap(),
    override val roles: Collection<String> = emptyList(),
) : ModContributor