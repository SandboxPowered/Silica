package org.sandboxpowered.silica.api.util.extensions

import akka.actor.typed.ActorRef
import akka.actor.typed.Behavior
import akka.actor.typed.Scheduler
import akka.actor.typed.Signal
import akka.actor.typed.javadsl.ActorContext
import akka.actor.typed.javadsl.AskPattern
import akka.actor.typed.javadsl.BehaviorBuilder
import akka.actor.typed.javadsl.Behaviors
import akka.japi.pf.FI
import akka.japi.pf.ReceiveBuilder
import org.slf4j.Logger
import java.time.Duration
import java.util.concurrent.CompletionStage
import akka.actor.typed.javadsl.ReceiveBuilder as TypedReceiveBuilder

inline fun <reified T> ReceiveBuilder.match(apply: FI.UnitApply<T>): ReceiveBuilder = this.match(T::class.java, apply)

inline fun <T, reified M : T> BehaviorBuilder<T>.onMessage(noinline apply: (M) -> Behavior<T>): BehaviorBuilder<T> =
    this.onMessage(M::class.java, apply)

inline fun <T, reified M : T> TypedReceiveBuilder<T>.onMessage(noinline apply: (M) -> Behavior<T>): TypedReceiveBuilder<T> =
    this.onMessage(M::class.java, apply)

inline fun <T, reified M : Signal> TypedReceiveBuilder<T>.onSignal(noinline apply: (M) -> Behavior<T>): TypedReceiveBuilder<T> =
    this.onSignal(M::class.java, apply)

inline fun <T, reified U> ActorContext<T>.messageAdapter(noinline apply: (U) -> T): ActorRef<U> =
    this.messageAdapter(U::class.java, apply)

inline fun <reified T> receive(): BehaviorBuilder<T> = Behaviors.receive(T::class.java)

interface WithScheduler {
    val scheduler: Scheduler

    fun <T, U> ActorRef<in T>.ask(
        timeout: Duration = Duration.ofSeconds(3),
        messageFactory: (actorRef: ActorRef<U>) -> T
    ): CompletionStage<U> = AskPattern.ask(this, messageFactory, timeout, scheduler)
}

interface WithContext<T> : WithScheduler {
    fun getContext(): ActorContext<T>

    override val scheduler: Scheduler get() = getContext().system.scheduler()

    fun <U> CompletionStage<U>.pipeToSelf(block: (U?, Throwable?) -> T) {
        getContext().pipeToSelf(this, block)
    }

    val logger: Logger get() = getContext().log
}
