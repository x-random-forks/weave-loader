package net.weavemc.loader.api

/**
 * The public-facing Weave Loader API.
 *
 * This interface contains everything that Weave consumers should need to interact with the
 * loader's different mechanisms.
 *
 * @since 1.0.0-beta.3
 */
interface WeaveLoader {
    companion object {
        @JvmStatic
        val instance: WeaveLoader by lazy {
            WeaveLoaderImpl()
        }
    }
}