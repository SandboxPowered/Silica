package org.sandboxpowered.silica.world.util

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.ParameterizedTest.DISPLAY_NAME_PLACEHOLDER
import org.junit.jupiter.params.ParameterizedTest.INDEX_PLACEHOLDER
import org.junit.jupiter.params.provider.MethodSource
import org.sandboxpowered.silica.state.block.BlockState
import java.util.stream.Stream

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class BlocTreeTest {
    private val air = TestData.state("air", true)
    private val bedrock = TestData.state("bedrock")
    private val stone = TestData.state("stone")
    private val dirt = TestData.state("dirt")
    private val grass = TestData.state("grass")

    @MethodSource("world layers")
    @ParameterizedTest(name = "[$INDEX_PLACEHOLDER] $DISPLAY_NAME_PLACEHOLDER")
    fun `A tree filled with layers should store correct info`(blocks: Sequence<Pair<Triple<Int, Int, Int>, BlockState>>) {
        val tree = BlocTree(-8, 0, -8, 16, air)
        var count = 0

        for ((pos, state) in blocks) {
            val (x, y, z) = pos
            tree[x, y, z] = state
            if (!state.block.isAir(state)) ++count
        }

        for ((pos, state) in blocks) {
            val (x, y, z) = pos
            Assertions.assertEquals(state, tree[x, y, z])
        }

        Assertions.assertEquals(count, tree.nonAirInChunk(-8, 0, -8))
    }

    @Suppress("unused")
    private fun `world layers`(): Stream<Sequence<Pair<Triple<Int, Int, Int>, BlockState>>> = Stream.of(sequence {
        var dy = 0
        iterateCube(-8, dy, -8, w = 16, h = 1) { x, y, z ->
            yield(Triple(x, y, z) to bedrock)
        }
        ++dy
        iterateCube(-8, dy, -8, w = 16, h = 3, d = 16) { x, y, z ->
            yield(Triple(x, y, z) to if (x % 2 == z % 2) stone else dirt)
        }
        dy += 3
        iterateCube(-8, dy, -8, w = 16, h = 1) { x, y, z ->
            yield(Triple(x, y, z) to dirt)
        }
        ++dy
        iterateCube(-8, dy, -8, w = 16, h = 1) { x, y, z ->
            yield(Triple(x, y, z) to grass)
        }
    }, sequence {
        var dy = 0
        iterateCube(-8, dy, -8, w = 16, h = 1) { x, y, z ->
            yield(Triple(x, y, z) to bedrock)
        }
        ++dy
        iterateCube(-8, dy, -8, w = 16, h = 3) { x, y, z ->
            yield(Triple(x, y, z) to stone)
        }
        dy += 3
        iterateCube(-8, dy, -8, w = 16, h = 1) { x, y, z ->
            yield(Triple(x, y, z) to dirt)
        }
        ++dy
        iterateCube(-8, dy, -8, w = 16, h = 1) { x, y, z ->
            yield(Triple(x, y, z) to grass)
        }
    })

    //    @Test
    fun `A tree filled with the same BlockState should have no nodes`() {
        val tree = BlocTree(-8, 0, -8, 16, air)

        iterateCube(-8, 0, -8, 16) { x, y, z ->
            tree[x - 8, y, z - 8] = bedrock
        }

        val subsection = tree[0, 0, 0, 1, 1, 1]
        Assertions.assertEquals(0, subsection.treeDepth)
        Assertions.assertEquals(tree, subsection)
        Assertions.assertEquals(16 * 16 * 16, tree.nonAirInChunk(-8, 0, -8))
    }

    @MethodSource("illegal pos")
    @ParameterizedTest(name = "[$INDEX_PLACEHOLDER] $DISPLAY_NAME_PLACEHOLDER ${ParameterizedTest.ARGUMENTS_PLACEHOLDER}")
    fun `Trying to set a block outside of the tree's bounds should throw an ISE`(pos: Triple<Int, Int, Int>) {
        val tree = BlocTree(-8, 0, -8, 16, air)
        val (x, y, z) = pos
        Assertions.assertThrows(IllegalArgumentException::class.java) {
            tree[x, y, z] = air
        }
    }

    @Suppress("unused")
    private fun `illegal pos`(): Stream<Triple<Int, Int, Int>> = Stream.of(
        Triple(2, -1, 2),
        Triple(2, 16, 2),
        Triple(-9, 5, 2),
        Triple(8, 5, 2),
        Triple(2, 5, -9),
        Triple(2, 5, 8),
    )
}