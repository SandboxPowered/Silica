package org.sandboxpowered.silica.world.util

import com.artemis.utils.IntBag
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

internal class OcTreeTest {
    @Test
    fun `get inexact`() {
        val fill = IntBag()
        val tree = OcTree(-8f, -8f, -8f, 8f, 8f, 8f, 1, 8)
        fill.clear()
        tree[fill, -2.5f, -2.5f, -2.5f, 5f, 5f, 5f]
        Assertions.assertEquals(fill.size(), 0)
        tree.insert(1, -6f, -6f, -6f, 2f, 2f, 2f) // fully outside test region
        fill.clear()
        tree[fill, -2.5f, -2.5f, -2.5f, 5f, 5f, 5f]
        Assertions.assertEquals(fill.size(), 1)
        tree.insert(2, 6f, -6f, 6f, 2f, 2f, 2f) // fully outside test region
        tree.insert(3, -2f, 2f, 2f, 2f, 2f, 2f) // fully inside test region
        tree.insert(4, 2f, 2f, 2f, 2f, 2f, 2f) // overlaps test region
        tree[fill, -2.5f, -2.5f, -2.5f, 5f, 5f, 5f]
        fill.clear()
        tree[fill, -2.5f, -2.5f, -2.5f, 5f, 5f, 5f]
        Assertions.assertEquals(fill.size(), 4)

        // move inside test region
        tree.update(1, -2f, -2f, -2f, 2f, 2f, 2f)
        fill.clear()
        tree[fill, -2.5f, -2.5f, -2.5f, 5f, 5f, 5f]
        Assertions.assertEquals(fill.size(), 4)

        // move outside test region
        tree.update(1, -6f, -6f, -6f, 2f, 2f, 2f)
        fill.clear()
        tree[fill, -2.5f, -2.5f, -2.5f, 5f, 5f, 5f]
        Assertions.assertEquals(fill.size(), 4)

        // remove from outside
        tree.remove(1)
        fill.clear()
        tree[fill, -2.5f, -2.5f, -2.5f, 5f, 5f, 5f]
        Assertions.assertEquals(fill.size(), 3)

        // remove overlapping
        tree.remove(3)
        fill.clear()
        tree[fill, -2.5f, -2.5f, -2.5f, 5f, 5f, 5f]
        Assertions.assertEquals(fill.size(), 2)

        // remove inside
        tree.remove(4)
        fill.clear()
        tree[fill, -2.5f, -2.5f, -2.5f, 5f, 5f, 5f]
        Assertions.assertEquals(fill.size(), 1)
        tree.remove(2)
        fill.clear()
        tree[fill, -2.5f, -2.5f, -2.5f, 5f, 5f, 5f]
        Assertions.assertEquals(fill.size(), 0)
    }

    @Test
    fun `get exact`() {
        val fill = IntBag()
        val tree = OcTree(-8f, -8f, -8f, 8f, 8f, 8f, 1, 8)
        fill.clear()
        tree.getExact(fill, -2.5f, -2.5f, -2.5f, 5f, 5f, 5f)
        Assertions.assertEquals(fill.size(), 0)
        tree.insert(1, -6f, -6f, -6f, 2f, 2f, 2f) // fully outside test region
        fill.clear()
        tree.getExact(fill, -2.5f, -2.5f, -2.5f, 5f, 5f, 5f)
        Assertions.assertEquals(fill.size(), 0)
        tree.insert(2, 6f, -6f, 6f, 2f, 2f, 2f) // fully outside test region
        tree.insert(3, -2f, 2f, 2f, 2f, 2f, 2f) // fully inside test region
        tree.insert(4, 2f, 2f, 2f, 2f, 2f, 2f) // overlaps test region
        tree[fill, -2.5f, -2.5f, -2.5f, 5f, 5f, 5f]
        fill.clear()
        tree.getExact(fill, -2.5f, -2.5f, -2.5f, 5f, 5f, 5f)
        Assertions.assertEquals(fill.size(), 2)

        // move inside test region
        tree.update(1, -2f, -2f, -2f, 2f, 2f, 2f)
        fill.clear()
        tree.getExact(fill, -2.5f, -2.5f, -2.5f, 5f, 5f, 5f)
        Assertions.assertEquals(fill.size(), 3)

        // move outside test region
        tree.update(1, -6f, -6f, -6f, 2f, 2f, 2f)
        fill.clear()
        tree.getExact(fill, -2.5f, -2.5f, -2.5f, 5f, 5f, 5f)
        Assertions.assertEquals(fill.size(), 2)

        // remove from outside
        tree.remove(1)
        fill.clear()
        tree.getExact(fill, -2.5f, -2.5f, -2.5f, 5f, 5f, 5f)
        Assertions.assertEquals(fill.size(), 2)

        // remove overlapping
        tree.remove(3)
        fill.clear()
        tree.getExact(fill, -2.5f, -2.5f, -2.5f, 5f, 5f, 5f)
        Assertions.assertEquals(fill.size(), 1)

        // remove inside
        tree.remove(4)
        fill.clear()
        tree.getExact(fill, -2.5f, -2.5f, -2.5f, 5f, 5f, 5f)
        Assertions.assertEquals(fill.size(), 0)
        tree.remove(2)
        fill.clear()
        tree.getExact(fill, -2.5f, -2.5f, -2.5f, 5f, 5f, 5f)
        Assertions.assertEquals(fill.size(), 0)
    }

    @Test
    fun `get complex`() {
        val fill = IntBag()
        val tree = OcTree(-8f, -8f, -8f, 8f, 8f, 8f, 1, 8)
        fill.clear()
        tree[fill, -3f, -3f, -3f, 6f, 6f, 6f]
        Assertions.assertEquals(fill.size(), 0)

        // all outside
        tree.insert(1, -6f, -6f, -6f, 2f, 2f, 2f)
        tree.insert(2, 4f, -6f, 4f, 2f, 2f, 2f)
        tree.insert(3, -6f, 4f, -6f, 2f, 2f, 2f)
        tree.insert(4, 4f, 4f, 4f, 2f, 2f, 2f)
        tree.insert(5, -1f, -1f, -1f, 2f, 2f, 2f) // center
        tree.insert(6, -4f, -4f, -4f, 2f, 2f, 2f) // fully inside
        tree.insert(7, 2f, 2f, 2f, 2f, 2f, 2f) // fully inside
        tree.insert(8, -4f, -4f, -4f, 2f, 2f, 2f) // fully inside

        fill.clear()
        tree[fill, -3f, -3f, -3f, 6f, 6f, 6f]
        Assertions.assertEquals(8, fill.size())
    }

    @Test
    fun `inexact point single entity`() {
        val fill = IntBag()
        val tree = OcTree(-8f, -8f, -8f, 8f, 8f, 8f, 1, 8)
        fill.clear()
        tree[fill, -2.5f, -2.5f, -2.5f, 5f, 5f, 5f]
        Assertions.assertEquals(fill.size(), 0)
        tree.insert(1, -1f, -1f, -1f, 2f, 2f, 2f)
        fill.clear()
        tree[fill, 0f, 0f, 0f]
        Assertions.assertEquals(fill.size(), 1)
    }

    @Test
    fun `inexact point`() {
        val fill = IntBag()
        val tree = OcTree(-8f, -8f, -8f, 8f, 8f, 8f, 1, 8)
        fill.clear()
        tree[fill, -2.5f, -2.5f, -2.5f, 5f, 5f, 5f]
        Assertions.assertEquals(fill.size(), 0)
        tree.insert(1, -1f, -1f, -1f, 2f, 2f, 2f)
        tree.insert(2, -1f, -1f, -1f, 2f, 2f, 2f)
        fill.clear()
        tree[fill, 0f, 0f, 0f]
        Assertions.assertEquals(fill.size(), 2)
    }

    @Test
    fun `exact point single entity`() {
        val fill = IntBag()
        val tree = OcTree(-8f, -8f, -8f, 8f, 8f, 8f, 1, 8)
        fill.clear()
        tree[fill, -2.5f, -2.5f, -2.5f, 5f, 5f, 5f]
        Assertions.assertEquals(fill.size(), 0)
        tree.insert(1, -1f, -1f, -1f, 2f, 2f, 2f)
        fill.clear()
        tree.getExact(fill, 0f, 0f, 0f)
        Assertions.assertEquals(fill.size(), 1)
    }

    @Test
    fun `exact point`() {
        val fill = IntBag()
        val tree = OcTree(-8f, -8f, -8f, 8f, 8f, 8f, 1, 8)
        fill.clear()
        tree[fill, -2.5f, -2.5f, -2.5f, 5f, 5f, 5f]
        Assertions.assertEquals(fill.size(), 0)
        tree.insert(1, -1f, -1f, -1f, 2f, 2f, 2f)
        tree.insert(2, -1f, -1f, -1f, 2f, 2f, 2f)
        fill.clear()
        tree.getExact(fill, 0f, 0f, 0f)
        Assertions.assertEquals(fill.size(), 2)
    }

    @Test
    fun `next flag`() {
        val tree = OcTree(-8f, -8f, -8f, 8f, 8f, 8f, 1, 8)
        Assertions.assertEquals(1L, tree.nextFlag())
        Assertions.assertEquals(2L, tree.nextFlag())
        Assertions.assertEquals(4L, tree.nextFlag())
        Assertions.assertEquals(8L, tree.nextFlag())
    }

    @Test
    fun `flags inexact`() {
        val fill = IntBag()
        val tree = OcTree(-8f, -8f, -8f, 8f, 8f, 8f, 1, 8)
        fill.clear()
        tree[fill, -2.5f, -2.5f, -2.5f, 5f, 5f, 5f]
        Assertions.assertEquals(fill.size(), 0)
        tree.insert(1, 0L, -1f, -1f, -1f, 2f, 2f, 2f)
        tree.insert(2, 1L, -1f, -1f, -1f, 2f, 2f, 2f)
        tree.insert(3, 2L, -1f, -1f, -1f, 2f, 2f, 2f)
        tree.insert(4, 2L, -1f, -1f, -1f, 2f, 2f, 2f)
        tree.insert(5, 3L, -1f, -1f, -1f, 2f, 2f, 2f)

        // 0 flag
        fill.clear()
        tree[fill, -2f, -2f, -2f, 2f, 2f, 2f, 0L]
        Assertions.assertEquals(fill.size(), 5)

        // 1 flag
        fill.clear()
        tree[fill, -2f, -2f, -2f, 2f, 2f, 2f, 1L]
        Assertions.assertEquals(fill.size(), 2)

        // 2 flag
        fill.clear()
        tree[fill, -2f, -2f, -2f, 2f, 2f, 2f, 2L]
        Assertions.assertEquals(fill.size(), 3)
    }

    @Test
    fun `flags exact`() {
        val fill = IntBag()
        val tree = OcTree(-8f, -8f, -8f, 8f, 8f, 8f, 1, 8)
        fill.clear()
        tree.getExact(fill, -2.5f, -2.5f, -2.5f, 5f, 5f, 5f)
        Assertions.assertEquals(fill.size(), 0)
        tree.insert(1, 0L, -1f, -1f, -1f, 2f, 2f, 2f)
        tree.insert(2, 1L, -1f, -1f, -1f, 2f, 2f, 2f)
        tree.insert(3, 2L, -1f, -1f, -1f, 2f, 2f, 2f)
        tree.insert(4, 2L, -1f, -1f, -1f, 2f, 2f, 2f)
        tree.insert(5, 3L, -1f, -1f, -1f, 2f, 2f, 2f)

        // 0 flag
        fill.clear()
        tree.getExact(fill, -2f, -2f, -2f, 2f, 2f, 2f, 0L)
        Assertions.assertEquals(fill.size(), 5)

        // 1 flag
        fill.clear()
        tree.getExact(fill, -2f, -2f, -2f, 2f, 2f, 2f, 1L)
        Assertions.assertEquals(fill.size(), 2)

        // 2 flag
        fill.clear()
        tree.getExact(fill, -2f, -2f, -2f, 2f, 2f, 2f, 2L)
        Assertions.assertEquals(fill.size(), 3)
    }

    @Test
    fun `flags inexact point`() {
        val fill = IntBag()
        val tree = OcTree(-8f, -8f, -8f, 8f, 8f, 8f, 1, 8)
        fill.clear()
        tree[fill, -2.5f, -2.5f, -2.5f, 5f, 5f, 5f]
        Assertions.assertEquals(fill.size(), 0)
        tree.insert(1, 0L, -1f, -1f, -1f, 2f, 2f, 2f)
        tree.insert(2, 1L, -1f, -1f, -1f, 2f, 2f, 2f)
        tree.insert(3, 2L, -1f, -1f, -1f, 2f, 2f, 2f)
        tree.insert(4, 2L, -1f, -1f, -1f, 2f, 2f, 2f)
        tree.insert(5, 3L, -1f, -1f, -1f, 2f, 2f, 2f)

        // 0 flag
        fill.clear()
        tree[fill, 0f, 0f, 0f, 0L]
        Assertions.assertEquals(fill.size(), 5)

        // 1 flag
        fill.clear()
        tree[fill, 0f, 0f, 0f, 1L]
        Assertions.assertEquals(fill.size(), 2)

        // 2 flag
        fill.clear()
        tree[fill, 0f, 0f, 0f, 2L]
        Assertions.assertEquals(fill.size(), 3)
    }

    @Test
    fun `flags exact point`() {
        val fill = IntBag()
        val tree = OcTree(-8f, -8f, -8f, 8f, 8f, 8f, 1, 8)
        fill.clear()
        tree.getExact(fill, -2.5f, -2.5f, -2.5f, 5f, 5f, 5f)
        Assertions.assertEquals(fill.size(), 0)
        tree.insert(1, 0L, -1f, -1f, -1f, 2f, 2f, 2f)
        tree.insert(2, 1L, -1f, -1f, -1f, 2f, 2f, 2f)
        tree.insert(3, 2L, -1f, -1f, -1f, 2f, 2f, 2f)
        tree.insert(4, 2L, -1f, -1f, -1f, 2f, 2f, 2f)
        tree.insert(5, 3L, -1f, -1f, -1f, 2f, 2f, 2f)

        // 0 flag
        fill.clear()
        tree.getExact(fill, 0f, 0f, 0f, 0L)
        Assertions.assertEquals(fill.size(), 5)

        // 1 flag
        fill.clear()
        tree.getExact(fill, 0f, 0f, 0f, 1L)
        Assertions.assertEquals(fill.size(), 2)

        // 2 flag
        fill.clear()
        tree.getExact(fill, 0f, 0f, 0f, 2L)
        Assertions.assertEquals(fill.size(), 3)
    }

    @Test
    fun `upsert updates position`() {
        val fill = IntBag()
        val tree = OcTree(-8f, -8f, -8f, 8f, 8f, 8f, 1, 8)
        fill.clear()
        tree.getExact(fill, -2.5f, -2.5f, -2.5f, 5f, 5f, 5f)
        Assertions.assertEquals(fill.size(), 0)

        // matching
        tree.upsert(1, -1f, -1f, -1f, 2f, 2f, 2f)
        fill.clear()
        tree.getExact(fill, 0f, 0f, 0f)
        Assertions.assertEquals(fill.size(), 1)

        // not matching
        tree.upsert(1, 1f, 1f, 1f, 2f, 2f, 2f)
        fill.clear()
        tree.getExact(fill, 0f, 0f, 0f)
        Assertions.assertEquals(fill.size(), 0)
    }

    @Test
    fun `upsert updates flags`() {
        val fill = IntBag()
        val tree = OcTree(-8f, -8f, -8f, 8f, 8f, 8f, 1, 8)
        fill.clear()
        tree.getExact(fill, -2.5f, -2.5f, -2.5f, 5f, 5f, 5f)
        Assertions.assertEquals(fill.size(), 0)

        // flag 1
        tree.upsert(1, 1L, -1f, -1f, -1f, 2f, 2f, 2f)
        fill.clear()
        tree.getExact(fill, 0f, 0f, 0f, 1L)
        Assertions.assertEquals(fill.size(), 1)
        fill.clear()
        tree.getExact(fill, 0f, 0f, 0f, 2L)
        Assertions.assertEquals(fill.size(), 0)
        fill.clear()
        tree.getExact(fill, 0f, 0f, 0f, 4L)
        Assertions.assertEquals(fill.size(), 0)

        // flag 1 + 2
        tree.upsert(1, 2L, -1f, -1f, -1f, 2f, 2f, 2f)
        fill.clear()
        tree.getExact(fill, 0f, 0f, 0f, 1L)
        Assertions.assertEquals(fill.size(), 1)
        fill.clear()
        tree.getExact(fill, 0f, 0f, 0f, 2L)
        Assertions.assertEquals(fill.size(), 1)
        fill.clear()
        tree.getExact(fill, 0f, 0f, 0f, 4L)
        Assertions.assertEquals(fill.size(), 0)

        // flag 1 + 2 (upserting flags 3L changes nothing)
        tree.upsert(1, 3L, -1f, -1f, -1f, 2f, 2f, 2f)
        fill.clear()
        tree.getExact(fill, 0f, 0f, 0f, 1L)
        Assertions.assertEquals(fill.size(), 1)
        fill.clear()
        tree.getExact(fill, 0f, 0f, 0f, 2L)
        Assertions.assertEquals(fill.size(), 1)
        fill.clear()
        tree.getExact(fill, 0f, 0f, 0f, 4L)
        Assertions.assertEquals(fill.size(), 0)
    }

    @Test
    fun `upsert throws no exception on high entity ids`() {
        val tree = OcTree(-8f, -8f, -8f, 8f, 8f, 8f, 1, 8)
        tree.upsert(0, 0f, 0f, 0f, 1f, 1f, 1f)
        tree.upsert(10, 0f, 0f, 0f, 1f, 1f, 1f)
        tree.upsert(100, 0f, 0f, 0f, 1f, 1f, 1f)
        tree.upsert(1000, 0f, 0f, 0f, 1f, 1f, 1f)
    }

    @Test
    fun `upsert after remove inserts`() {
        val fill = IntBag()
        val tree = OcTree(-8f, -8f, -8f, 8f, 8f, 8f, 1, 8)
        tree.upsert(0, 1L, 0f, 0f, 0f, 1f, 1f, 1f)
        tree.remove(0)
        tree.upsert(0, 2L, 0f, 0f, 0f, 1f, 1f, 1f)
        tree.getExact(fill, 0f, 0f, 0f, 1L)
        Assertions.assertEquals(fill.size(), 0)
        tree.getExact(fill, 0f, 0f, 0f, 2L)
        Assertions.assertEquals(fill.size(), 1)
    }
}