package org.sandboxpowered.silica.api.util.extensions

import akka.actor.DeadLetterSuppression
import akka.actor.typed.ActorRef
import akka.actor.typed.Behavior
import akka.actor.typed.javadsl.AbstractBehavior
import akka.actor.typed.javadsl.ActorContext
import akka.actor.typed.javadsl.Behaviors
import akka.actor.typed.javadsl.Receive

sealed class Accumulator<out T : Any> {
    class Start<T : Any>(val replyTo: ActorRef<in Array<out T>>) : Accumulator<T>()
    class ReceiveValue<out T : Any>(val index: Int, val value: T) : Accumulator<T>(), DeadLetterSuppression
    class Failure<out T : Any>(val index: Int, val ex: Throwable) : Accumulator<T>()

    companion object {
        inline fun <reified T : Any, M> actor(
            amount: Int,
            target: ActorRef<in M>,
            noinline ask: (Int, ActorRef<in T>) -> M
        ): Behavior<Accumulator<T>> = actor(arrayOfNulls(amount), target, ask)

        fun <T : Any, M> actor(
            data: Array<T?>,
            target: ActorRef<in M>,
            ask: (Int, ActorRef<in T>) -> M
        ): Behavior<Accumulator<T>> = Behaviors.setup { Actor(data, target, ask, it) }
    }

    private class Actor<T : Any, M>(
        private val data: Array<T?>,
        private val target: ActorRef<in M>,
        private val ask: (Int, ActorRef<in T>) -> M,
        context: ActorContext<Accumulator<T>>
    ) : AbstractBehavior<Accumulator<T>>(context), WithContext<Accumulator<T>> {
        private var replyTo: ActorRef<in Array<T>>? = null

        override fun createReceive(): Receive<Accumulator<T>> = newReceiveBuilder()
            .onMessage(this::handleStart)
            .onMessage(this::handleFailure)
            .onMessage(this::handleReceiveValue)
            .build()

        private fun handleStart(message: Start<T>): Behavior<Accumulator<T>> {
            if (replyTo == null) {
                replyTo = message.replyTo
                data.indices.forEach { idx ->
                    target.ask { actorRef: ActorRef<T> -> ask(idx, actorRef) }
                        .thenAccept { context.self.tell(ReceiveValue(idx, it)) }
                        .onException {
                            context.log.warn("Failed to get value $idx")
                            context.self.tell(Failure(idx, it))
                        }
                }
            }

            return Behaviors.same()
        }

        private fun handleFailure(message: Failure<T>): Behavior<Accumulator<T>> {
            context.log.warn("Failed on ${message.index}", message.ex)

            return Behaviors.stopped()
        }

        private fun handleReceiveValue(message: ReceiveValue<T>): Behavior<Accumulator<T>> {
            data[message.index] = message.value
            return checkDone()
        }

        private fun checkDone(): Behavior<Accumulator<T>> =
            if (data.none { it == null }) {
                replyTo!!.tell(data.requireNoNulls())
                Behaviors.stopped()
            } else {
                Behaviors.same()
            }
    }
}