package net.weavemc.loader.impl.mod.model.version

import com.unascribed.flexver.FlexVerComparator
import net.weavemc.loader.api.mod.SemanticVersion
import net.weavemc.loader.api.mod.Version

private const val SEMVER_REGEX_STR = "^(0|[1-9]\\d*)\\.(0|[1-9]\\d*)\\.(0|[1-9]\\d*)(?:-((?:0|[1-9]\\d*|\\d*[a-zA-Z-][0-9a-zA-Z-]*)(?:\\.(?:0|[1-9]\\d*|\\d*[a-zA-Z-][0-9a-zA-Z-]*))*))?(?:\\+([0-9a-zA-Z-]+(?:\\.[0-9a-zA-Z-]+)*))?\$"

internal class SemanticVersionImpl(
    override val raw: String,
) : SemanticVersion {
    override val componentCount: Int
    override val major: Int
    override val minor: Int
    override val patch: Int
    override val preRelease: String?
    override val build: String?

    init {
        require(raw.isNotEmpty() && raw.isNotBlank()) { "Version string cannot be empty" }
        val regexMatch = SEMVER_REGEX.matchEntire(raw)
        require(regexMatch != null) {
            "Version string must be in the format <major>.<minor>.<patch>[-<pre-release>][+<build>], but was $raw"
        }
        componentCount = regexMatch.groups.size - 1
        major = regexMatch.groupValues[1].toInt()
        minor = regexMatch.groupValues[2].toInt()
        patch = regexMatch.groupValues[3].toInt()
        preRelease = regexMatch.groupValues.getOrNull(4)
        build = regexMatch.groupValues.getOrNull(5)
    }

    companion object {
        private val SEMVER_REGEX = SEMVER_REGEX_STR.toRegex()
    }

    override fun compareTo(other: Version): Int =
        FlexVerComparator.compare(raw, other.raw)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is SemanticVersionImpl) return false
        return major == other.major && minor == other.minor && patch == other.patch && preRelease == other.preRelease
    }
    override fun hashCode(): Int = arrayOf(major, minor, patch, preRelease).contentHashCode()
    override fun toString(): String = raw
}