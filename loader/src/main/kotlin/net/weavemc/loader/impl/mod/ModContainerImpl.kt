package net.weavemc.loader.impl.mod

import net.weavemc.internals.ModConfig
import net.weavemc.loader.api.mod.ModContainer
import net.weavemc.loader.api.mod.ModLicense
import net.weavemc.loader.api.mod.ModMetadata
import net.weavemc.loader.api.mod.Version
import net.weavemc.loader.impl.mod.model.ModContactImpl
import net.weavemc.loader.impl.mod.model.ModMetadataImpl
import java.nio.file.Path
import kotlin.io.path.Path

internal data class ModContainerImpl(
    override val metadata: ModMetadata,
    override val path: Path,
    val builtin: Boolean = false,
) : ModContainer {
    constructor(path: Path, config: ModConfig) : this(
        metadata = ModMetadataImpl(
            id = config.modId,
            version = Version.ofSemantic(
                ("0.0.0-legacy" + config.compiledFor?.let { "+mc-$it" })
            ),
            name = config.name,
            description = null,
            contact = ModContactImpl(),
            license = listOf(ModLicense.of("ARR")),
        ),
        path = path,
    )
}