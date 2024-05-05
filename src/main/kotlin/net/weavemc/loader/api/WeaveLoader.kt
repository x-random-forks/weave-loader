package net.weavemc.loader.api

import net.weavemc.loader.api.event.Event
import net.weavemc.loader.api.event.EventBus
import net.weavemc.loader.api.mod.ModRegistry
import net.weavemc.loader.impl.WeaveLoaderImpl

/**
 * The public-facing Weave Loader API.
 *
 * This interface contains everything that Weave consumers should need to interact with the
 * loader's different mechanisms.
 *
 * @since 1.0.0-beta.3
 */
interface WeaveLoader {
    val eventBus: EventBus<Event>
    val modRegistry: ModRegistry

    companion object {
        @JvmStatic
        val instance: WeaveLoader
            get() = WeaveLoaderImpl.getInstance()
    }
}