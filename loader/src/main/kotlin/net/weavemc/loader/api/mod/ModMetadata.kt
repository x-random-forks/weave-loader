package net.weavemc.loader.api.mod

import net.weavemc.loader.impl.mod.model.ModLicenseImpl

public interface ModMetadata {
    /**
     * A unique identifier for the mod.
     */
    public val id: String

    /**
     * The parsed version object for the mod.
     */
    public val version: Version

    /**
     * The display name of the mod.
     */
    public val name: String

    /**
     * A description of the mod's contents.
     */
    public val description: String?

    /**
     * A collection of authors/contributors for the mod.
     */
    public val contributors: Collection<ModContributor>

    /**
     * Contact/support information for the mod.
     */
    public val contact: ModContact

    /**
     * A collection of licenses for the mod.
     */
    public val license: Collection<ModLicense>

    public fun getIcon(size: Int): String
}

public interface ModContributor {
    /**
     * The name of the contributor.
     */
    public val name: String

    /**
     * A map of contact information for the contributor.
     *
     * Some keys are recommended to be used:
     * - `email`
     * - `discord` (username)
     * - `github`
     * - `minecraft` (with either a username or a UUID)
     * - `website`
     */
    public val contact: Map<String, String>

    /**
     * A collection of roles, or empty if not specified/applicable.
     */
    public val roles: Collection<String>
}

public interface ModContact {
    public val homepage: String?

    public val issues: String?

    public val sources: String?

    public val custom: Map<String, String>
}

public interface ModLicense {
    /**
     * The license identifier, typically a [SPDX](https://spdx.org/licenses/) identifier.
     */
    public val id: String

    /**
     * The name of the license.
     */
    public val name: String

    /**
     * A URL to either the license or a page that explains it.
     */
    public val url: String?

    public companion object {
        public fun of(id: String): ModLicense = ModLicenseImpl(id)
    }
}
