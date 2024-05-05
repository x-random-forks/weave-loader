package net.weavemc.loader.api.event

import java.util.function.Consumer

/**
 * The event bus handles events and event listeners.
 *
 * @see Event
 *
 * @see SubscribeEvent
 */
interface EventBus<T> {
    /**
     * Subscribes an object to the event bus, turning methods annotated with
     * [SubscribeEvent] into listeners.
     *
     * @param obj The object to subscribe.
     */
    fun subscribe(obj: Any)

    /**
     * Subscribes a listener to the event bus.
     *
     * @param event   The class of the event to subscribe to.
     * @param handler The Consumer to handle that event.
     */
    fun <E : T> subscribe(event: Class<E>, handler: Consumer<E>)

    /**
     * Unsubscribes an object from the event bus, which unsubscribes all of its listeners.
     *
     * @param obj The object to unsubscribe.
     */
    fun unsubscribe(obj: Any)

    /**
     * Unsubscribes a listener from the event bus.
     *
     * @param consumer The Consumer to unsubscribe.
     */
    fun unsubscribe(consumer: Consumer<T>)

    /**
     * Post an event to all its listeners.
     *
     * @param event The event to call.
     */
    fun <E : T> post(event: E)
}

@Suppress("EXTENSION_SHADOWED_BY_MEMBER")
inline fun <T, reified E : T> EventBus<T>.subscribe(crossinline handler: (E) -> Unit) {
    subscribe(E::class.java) { handler(it) }
}