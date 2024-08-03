package net.weavemc.loader.impl.mod.model.version

import com.unascribed.flexver.FlexVerComparator
import net.weavemc.loader.api.mod.RawVersion
import net.weavemc.loader.api.mod.Version

internal data class RawVersionImpl(override val raw: String) : RawVersion {
    override fun compareTo(other: Version): Int =
        FlexVerComparator.compare(raw, other.raw)

    override fun equals(other: Any?): Boolean = other is RawVersion && raw == other.raw
    override fun hashCode(): Int = raw.hashCode()
    override fun toString(): String = raw
}