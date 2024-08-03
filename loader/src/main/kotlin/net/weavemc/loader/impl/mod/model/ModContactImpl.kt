package net.weavemc.loader.impl.mod.model

import net.weavemc.loader.api.mod.ModContact
import java.net.URI

private const val HOMEPAGE_KEY = "homepage"
private const val ISSUES_KEY = "issues"
private const val SOURCES_KEY = "sources"

internal data class ModContactImpl(
    override val homepage: String? = null,
    override val issues: String? = null,
    override val sources: String? = null,
    override val custom: Map<String, String> = emptyMap(),
) : ModContact {
    constructor(vararg pairs: Pair<String, String>) : this(mapOf(*pairs))

    constructor(map: Map<String, String>) : this(
        homepage = map[HOMEPAGE_KEY],
        issues = map[ISSUES_KEY],
        sources = map[SOURCES_KEY],
        custom = map.filterKeys { it != HOMEPAGE_KEY && it != ISSUES_KEY && it != SOURCES_KEY }
    )

    init {
        fun validUri(uri: String): Boolean = try {
            URI(uri)
            true
        } catch (e: Exception) {
            false
        }

        homepage?.let { require(validUri(it)) { "Homepage $homepage is not a valid URI" } }
        issues?.let { require(validUri(it)) { "Issues $issues is not a valid URI" } }
        sources?.let { require(validUri(it)) { "Sources $sources is not a valid URI" } }
    }
}