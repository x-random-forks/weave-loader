package net.weavemc.loader.api.mod

import java.net.URI

interface ModContainer {
    val metadata: ModMetadata
    val type: ModType
    val source: URI
}

enum class ModType {
    /**
     * A mod that is loaded by Weave Loader.
     */
    WEAVE,

    /**
     * An internal representation of a "mod". Such mods may include
     * `java`, `minecraft`, and `weaveloader` itself.
     */
    INTERNAL,
}