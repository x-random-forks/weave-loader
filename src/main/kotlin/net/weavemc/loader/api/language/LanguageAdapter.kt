package net.weavemc.loader.api.language

import net.weavemc.loader.api.mod.ModContainer

interface LanguageAdapter {
    fun <T> create(container: ModContainer, value: String, type: Class<T>): T
}