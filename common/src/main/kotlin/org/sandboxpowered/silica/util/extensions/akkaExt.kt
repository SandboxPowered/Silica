package org.sandboxpowered.silica.util

import akka.actor.typed.ActorRef
import akka.actor.typed.Behavior
import akka.actor.typed.Signal
import akka.actor.typed.javadsl.ActorContext
import akka.japi.pf.FI
import akka.japi.pf.ReceiveBuilder
import akka.actor.typed.javadsl.ReceiveBuilder as TypedReceiveBuilder

inline fun <reified T> ReceiveBuilder.match(apply: FI.UnitApply<T>): ReceiveBuilder = this.match(T::class.java, apply)

inline fun <T, reified M : T> TypedReceiveBuilder<T>.onMessage(noinline apply: (M) -> Behavior<T>): TypedReceiveBuilder<T> =
    this.onMessage(M::class.java, apply)

inline fun <T, reified M : Signal> TypedReceiveBuilder<T>.onSignal(noinline apply: (M) -> Behavior<T>): TypedReceiveBuilder<T> =
    this.onSignal(M::class.java, apply)

inline fun <T, reified U> ActorContext<T>.messageAdapter(noinline apply: (U) -> T): ActorRef<U> =
    this.messageAdapter(U::class.java, apply)
