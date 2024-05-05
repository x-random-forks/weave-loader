package net.weavemc.loader.api.bootstrap

import java.lang.instrument.Instrumentation

//TODO: replace with a better concept
interface Tweaker {
    fun tweak(inst: Instrumentation)
}