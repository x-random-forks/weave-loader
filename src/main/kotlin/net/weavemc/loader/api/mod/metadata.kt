package net.weavemc.loader.api.mod

interface ModMetadata {
    val id: String
    val name: String?
    val version: String
    val description: String?

    val authors: List<ModAuthor>
}

interface ModAuthor {
    val id: String
    val name: String?
}