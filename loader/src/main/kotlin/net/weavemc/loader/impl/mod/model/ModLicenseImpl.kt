package net.weavemc.loader.impl.mod.model

import net.weavemc.loader.api.mod.ModLicense

internal class ModLicenseImpl(
    override val id: String,
    override val name: String = id,
    override val url: String? = if (id == "ARR") null else "https://spdx.org/licenses/${id}.html",
) : ModLicense