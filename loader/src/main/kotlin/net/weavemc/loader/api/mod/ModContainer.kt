package net.weavemc.loader.api.mod

import java.nio.file.Path

public interface ModContainer {
    public val metadata: ModMetadata
    public val path: Path
}