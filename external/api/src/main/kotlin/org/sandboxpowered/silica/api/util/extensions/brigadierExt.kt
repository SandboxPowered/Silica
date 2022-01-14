package org.sandboxpowered.silica.api.util.extensions

import com.mojang.brigadier.Command
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.*
import com.mojang.brigadier.builder.ArgumentBuilder
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.builder.RequiredArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.suggestion.SuggestionProvider
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import com.mojang.brigadier.tree.CommandNode
import java.util.concurrent.CompletableFuture
import java.util.function.Predicate
import kotlin.reflect.KClass


interface DSLCommandNode<T> {
    /**
     * returns the brigardier node (only works after setup f.e on command execute)
     * @throws UninitializedPropertyAccessException if you run before or at setup
     */
    val node: CommandNode<T>

    /**
     * just the brigadier builder
     */
    val builder: ArgumentBuilder<T, *>

    /**
     * Adds and Alias which can also used instant of the name of the Literal
     * @param alias the alias which should be added
     * @throws IllegalStateException if an alias is set for and Argument (it is wired and useless)
     */
    fun alias(alias: String)

    /**
     * This will be called to check is there sommething required
     * runs on each request
     * false -> command will be hidden
     * true(default) -> command will show normaly
     * @return is the command usable in this conditions
     * @see Predicate
     */
    fun require(onCheck: T.() -> Boolean)

    /**
     * this will be executed if the currend command is called
     * @return the result as int
     */
    fun executes(executed: T.(context: CommandContext<T>) -> Int)

    /**
     * adds an Literal (non argument/subcommand) node
     * @see LiteralArgumentBuilder
     */
    fun literal(name: String, setup: DSLCommandNode<T>.() -> Unit)

    /**
     * adds an argument node
     * @see RequiredArgumentBuilder
     * @see ArgumentBuilder.then
     */
    fun <S> argument(name: String, type: ArgumentType<S>, setup: DSLCommandNode<T>.() -> Unit)

    /**
     * adds an Suggestion provider
     * will run an each suggestion request
     * @see SuggestionProvider
     * @see RequiredArgumentBuilder.suggests
     */
    fun suggest(onSuggest: SuggestionsBuilder.(context: CommandContext<T>) -> Unit)
}

private class DSLCommandNodeImpl<T>(override val builder: ArgumentBuilder<T, *>, private val parent: CommandNode<T>) :
    DSLCommandNode<T> {


    val aliases = mutableSetOf<String>()
    var requires: Predicate<T> = Predicate { true }
    var executing: Command<T>? = null
    val childs = mutableSetOf<CommandEntry<T>>()
    private var suggestionProvider: SuggestionProvider<T>? = null

    override lateinit var node: CommandNode<T>

    override fun alias(alias: String) {
        if (this.builder !is LiteralArgumentBuilder<T>) throw IllegalStateException("Only literals can have aliases!")
        aliases.add(alias)
    }

    override fun require(onCheck: T.() -> Boolean) {
        requires = Predicate(onCheck)
    }

    override fun executes(executed: T.(context: CommandContext<T>) -> Int) {
        executing = Command { context -> executed.invoke(context.source, context) }
    }

    override fun literal(name: String, setup: DSLCommandNode<T>.() -> Unit) {
        childs.add(CommandEntry(LiteralArgumentBuilder.literal(name), setup))
    }

    override fun <S> argument(name: String, type: ArgumentType<S>, setup: DSLCommandNode<T>.() -> Unit) {
        childs.add(CommandEntry(RequiredArgumentBuilder.argument(name, type), setup))
    }

    override fun suggest(onSuggest: SuggestionsBuilder.(context: CommandContext<T>) -> Unit) {
        if (this.builder !is RequiredArgumentBuilder<T, *>) throw IllegalStateException("Only Arguments can have Custom Suggestions!")
        suggestionProvider = DSLSuggestionProvider(onSuggest)
    }

    internal fun buildTree() {
        node = builder.also { build ->
            modBuilder(build)
        }.build()
        childs.forEach { entry ->
            val dslNode = DSLCommandNodeImpl(entry.argumentbuilder, node)
            entry.setup.invoke(dslNode)
            dslNode.buildTree()
            node.addChild(dslNode.node)
        }

        aliases.forEach { alias ->
            val aliasnode = LiteralArgumentBuilder.literal<T>(alias)
                .fork(node, null).also {
                    modBuilder(it)
                }.build()
            parent.addChild(aliasnode)
        }

    }

    private fun modBuilder(builder: ArgumentBuilder<T, *>) {
        builder.requires(requires)
        builder.executes(executing)
        if (suggestionProvider != null && builder is RequiredArgumentBuilder<T, *>) {
            builder.suggests(suggestionProvider)
        }
    }

    class CommandEntry<T>(val argumentbuilder: ArgumentBuilder<T, *>, val setup: DSLCommandNode<T>.() -> Unit)
}

private class DSLSuggestionProvider<T>(val onSuggest: SuggestionsBuilder.(context: CommandContext<T>) -> Unit) :
    SuggestionProvider<T> {
    override fun getSuggestions(
        context: CommandContext<T>,
        builder: SuggestionsBuilder
    ): CompletableFuture<Suggestions> {

        onSuggest.invoke(builder, context)
        return builder.buildFuture()
    }
}

/**
 * adds an Literal (non argument/subcommand) node
 * @see LiteralArgumentBuilder
 */
fun <T> CommandDispatcher<T>.literal(name: String, setup: DSLCommandNode<T>.() -> Unit): CommandNode<T> {
    val literal = LiteralArgumentBuilder.literal<T>(name)
    val node = DSLCommandNodeImpl<T>(literal, this.root)
    setup.invoke(node)
    node.buildTree()
    this.root.addChild(node.node)
    return node.node
}

/**
 * Like executes but always return 0!
 * so the is no return need
 * @see DSLCommandNode.executes
 */
fun <T> DSLCommandNode<T>.executesNoResult(executed: T.(context: CommandContext<T>) -> Unit) {
    executes {
        executed(this, it)
        ;return@executes 0
    }
}

/**
 * Like executes but always return 0!
 * so the is no return need
 * @see DSLCommandNode.executes
 */
fun <T> DSLCommandNode<T>.executes(executed: T.(context: CommandContext<T>) -> Unit) {
    executes {
        executed(this, it)
        return@executes 0
    }
}

/**
 * Like executes but always return 0!
 * so the is no return need
 * @see DSLCommandNode.executes
 */
fun <T> DSLCommandNode<T>.runs(executed: T.(context: CommandContext<T>) -> Unit) {
    executes { executed.invoke(this, it);return@executes 0 }
}


fun <V : Any> CommandContext<*>.getArgument(name: String, clazz: KClass<V>): V = this.getArgument(name, clazz.java)
inline fun <reified V : Any> CommandContext<*>.getArgument(name: String): V = this.getArgument(name, V::class)

operator fun SuggestionsBuilder.plus(suggestion: String): SuggestionsBuilder = suggest(suggestion)
operator fun SuggestionsBuilder.plus(suggestion: Int): SuggestionsBuilder = suggest(suggestion)

val WordArgument: StringArgumentType = StringArgumentType.word()
val StringArgument: StringArgumentType = StringArgumentType.string()
val GreedyStringArgument: StringArgumentType = StringArgumentType.greedyString()
val IntegerArgument: IntegerArgumentType = IntegerArgumentType.integer()
val DoubleArgument: DoubleArgumentType = DoubleArgumentType.doubleArg()
val LongArgument: LongArgumentType = LongArgumentType.longArg()
val FloatArgument: FloatArgumentType = FloatArgumentType.floatArg()
val BoolArgument: BoolArgumentType = BoolArgumentType.bool()