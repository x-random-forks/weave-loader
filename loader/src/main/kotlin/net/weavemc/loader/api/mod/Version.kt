package net.weavemc.loader.api.mod

import net.weavemc.loader.impl.mod.model.version.SemanticVersionImpl

public interface Version {
    public val raw: String

    public fun compareTo(other: Version): Int

    public companion object {
        public fun ofSemantic(raw: String): Version = SemanticVersionImpl(raw)
    }
}

public interface RawVersion : Version

public interface SemanticVersion : Version{
    public val componentCount: Int

    public val major: Int
    public val minor: Int
    public val patch: Int
    public val preRelease: String?
    public val build: String?
}