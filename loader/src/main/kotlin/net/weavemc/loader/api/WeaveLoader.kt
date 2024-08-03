package net.weavemc.loader.api

import net.weavemc.loader.api.mod.ModContainer
import net.weavemc.loader.impl.WeaveLoaderImpl
import java.nio.file.Path

/**
 * The main interface for Weave Loader.
 */
public interface WeaveLoader {
    public val gameDir: Path
    public val modsDir: Path
    public val configDir: Path

    public val mods: Collection<ModContainer>

    public companion object {
        /**
         * The main class of the Weave Loader.
         */
        public val INSTANCE: WeaveLoader
            get() = WeaveLoaderImpl.getInstance()
    }
}