package net.weavemc.loader.impl

import net.weavemc.loader.api.event.Event
import net.weavemc.loader.api.event.EventBus
import net.weavemc.loader.api.event.SubscribeEvent
import java.lang.reflect.Method
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList
import java.util.function.Consumer

object EventBusImpl : EventBus<Event> {
    private val map: MutableMap<Class<*>, MutableList<Consumer<*>>> = ConcurrentHashMap()

    override fun subscribe(obj: Any) = obj.javaClass.declaredMethods
        .filter { it.isAnnotationPresent(SubscribeEvent::class.java) && it.parameterCount == 1 }
        .forEach { getListeners(it.parameterTypes.first()) += ReflectEventConsumer(obj, it) }

    override fun <E : Event> subscribe(event: Class<E>, handler: Consumer<E>) {
        getListeners(event) += handler
    }

    /**
     * Post an event to all its listeners.
     *
     * @param event The event to call.
     */
    override fun <E : Event> post(event: E) {
        var curr: Class<*> = event.javaClass

        while (curr != Any::class.java) {
            getListeners(curr).filterIsInstance<Consumer<E>>().forEach(Consumer { it.accept(event) })
            curr = curr.superclass
        }
    }

    override fun unsubscribe(consumer: Consumer<Event>) =
        map.values.forEach { it.removeIf { c -> c === consumer } }

    override fun unsubscribe(obj: Any) =
        map.values.forEach { it.removeIf { c -> c is ReflectEventConsumer && c.obj === obj } }

    /**
     * Returns a list of listeners alongside its event class.
     *
     * @param event The corresponding event class to grab the listeners from.
     * @return a list of listeners corresponding to the event class.
     */
    private fun getListeners(event: Class<*>) = map.computeIfAbsent(event) { CopyOnWriteArrayList() }

    private class ReflectEventConsumer(val obj: Any, val method: Method) : Consumer<Event?> {
        override fun accept(event: Event?) {
            method.invoke(obj, event)
        }
    }
}
