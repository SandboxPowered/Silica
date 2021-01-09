package org.sandboxpowered.silica.nbt

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.sandboxpowered.silica.util.math.Position
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream
import java.util.*
import java.util.stream.Stream

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class NBTSerdeTest {

    @ParameterizedTest
    @MethodSource("examples")
    fun readNbt(data: String, expected: CompoundTag) {
        val iss = ByteArrayInputStream(Base64.getDecoder().decode(data))
        val dis = DataInputStream(iss)
        val nbt = dis.readNbt() as CompoundTag
        assertEquals(-1, dis.read()) // End of stream, to make sure everything was read
        for ((k, v) in expected.tags) {
            val r = nbt.tags[k]
            assertNotNull(r)
            assertEquals(v.type, r!!.type)
            when (val vv = v.value) {
                is ByteArray -> assertArrayEquals(vv, r.value as? ByteArray)
                is IntArray -> assertArrayEquals(vv, r.value as? IntArray)
                is LongArray -> assertArrayEquals(vv, r.value as? LongArray)
                else -> assertEquals(v.value, r.value)
            }
        }
    }

    @ParameterizedTest
    @MethodSource("examples")
    fun write(expected: String, data: CompoundTag) {
        val oss = ByteArrayOutputStream()
        val dos = DataOutputStream(oss)
        dos.write(data)
        val result = Base64.getEncoder().encodeToString(oss.toByteArray())
        assertEquals(expected, result)
    }

    @Suppress("unused")
    fun examples(): Stream<Arguments> = Stream.of(
        Arguments.of("CgAAAQAIc29tZUJ5dGUDAA==", CompoundTag().apply {
            setByte("someByte", 3)
        }),
        Arguments.of("CgAAAgAJc29tZVNob3J0ACoA", CompoundTag().apply {
//            setShort("someShort", 42)
            tags["someShort"] = CompoundTag.Entry.short(42)
        }), // No support for Short in the api so setting it manually for now
        Arguments.of("CgAAAwAHc29tZUludAAAACoA", CompoundTag().apply {
            setInt("someInt", 42)
        }),
        Arguments.of("CgAABQAJc29tZUZsb2F0QigAAAA=", CompoundTag().apply {
//            setFloat("someFloat", 42f)
            tags["someFloat"] = CompoundTag.Entry.float(42f)
        }), // No support for Float in the api so setting it manually for now
        Arguments.of("CgAABgAKc29tZURvdWJsZUBRfXCj1wo9AA==", CompoundTag().apply {
            setDouble("someDouble", 69.96)
        }),
        Arguments.of("CgAABwANc29tZUJ5dGVBcnJheQAAAAUAAQIDBAA=", CompoundTag().apply {
            setByteArray("someByteArray", ByteArray(5) { it.toByte() })
        }),
        Arguments.of("CgAACAAKc29tZVN0cmluZwANSGVsbG8gV29ybGQgIQA=", CompoundTag().apply {
            setString("someString", "Hello World !")
        }),
        /*Arguments.of("", CompoundTag().apply {
            setList("someList", listOf())
        }),*/ // Lists not supported yet
        Arguments.of("CgAACgAHc29tZVRhZwEACHNvbWVCeXRlAwMAB3NvbWVJbnQAAAAqAAA=", CompoundTag().apply {
            setTag("someTag", CompoundTag().apply {
                setByte("someByte", 3)
                setInt("someInt", 42)
            })
        }),
        Arguments.of("CgAACwAMc29tZUludEFycmF5AAAABQAAAAAAAAABAAAAAgAAAAMAAAAEAA==", CompoundTag().apply {
            setIntArray("someIntArray", IntArray(5) { it })
        }),
        Arguments.of("CgAADAANc29tZUxvbmdBcnJheQAAAAUAAAAAAAAAAAAAAAAAAAABAAAAAAAAAAIAAAAAAAAAAwAAAAAAAAAEAA==", CompoundTag().apply {
//            setLongArray("someLongArray", LongArray(5) { it.toLong() })
            tags["someLongArray"] = CompoundTag.Entry.longArray(LongArray(5) { it.toLong() })
        }), // No support for LongArray in the api so setting it manually for now
        Arguments.of("CgAACgAIc29tZVVVSUQEAAFMtiUPhz6kNtEEAAFNg2D0rLzXR20AAA==", CompoundTag().apply {
            setUUID("someUUID", UUID.fromString("8360f4ac-bcd7-476d-b625-0f873ea436d1"))
        }),
        Arguments.of("CgAACgAHc29tZVBvcwMAAVgAAAAqAwABWQAAAEUDAAFaAAABpAAA", CompoundTag().apply {
            setPosition("somePos", Position(42, 69, 420))
        }),
    )
}