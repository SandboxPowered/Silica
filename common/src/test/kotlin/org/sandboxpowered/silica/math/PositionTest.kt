package org.sandboxpowered.silica.math

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.sandboxpowered.silica.util.math.Position.PositionRange
import java.util.stream.Stream
import org.sandboxpowered.silica.util.math.Position as Pos

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PositionTest {
    @ParameterizedTest
    @MethodSource("ranges")
    fun `is position within range`(range: PositionRange, position: Pos, expected: Boolean) {
        Assertions.assertEquals(expected, position in range)
    }

    private fun ranges(): Stream<Arguments> = Stream.of(
        Arguments.of(Pos(0, 0, 0)..Pos(10, 10, 10), Pos(5, 5, 5), true),
        Arguments.of(Pos(0, -600, 0)..Pos(0, 600, 0), Pos(0, 66, 0), true),
        Arguments.of(Pos(0, 0, 0)..Pos(10, 10, 10), Pos(5, 15, 5), false),
        Arguments.of(Pos(-60, -30, -20)..Pos(67, 346, 32), Pos(-50, 200, 0), true),
        Arguments.of(Pos(0, 0, 0)..Pos(0, 0, 0), Pos(0, 0, 0), true),
        Arguments.of(Pos(0, 0, 0)..Pos(0, 0, 0), Pos(0, -1, 0), false),
    )
}