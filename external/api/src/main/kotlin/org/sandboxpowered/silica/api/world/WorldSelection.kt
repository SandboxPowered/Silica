package org.sandboxpowered.silica.api.world

interface WorldSelection {
    val x: Int
    val y: Int
    val z: Int
    val width: Int
    val height: Int
    val length: Int

    fun contains(x: Int, y: Int, z: Int): Boolean =
        this.x <= x && this.x + this.width > x
                && this.y <= y && this.y + this.height > y
                && this.z <= z && this.z + this.length > z

    fun walkX(step: Int): IntProgression = (x until (x + width) step step)
    fun walkY(step: Int): IntProgression = (y until (y + height) step step)
    fun walkZ(step: Int): IntProgression = (z until (z + length) step step)
}

inline fun WorldSelection.walk(step: Int, body: (x: Int, y: Int, z: Int) -> Unit) {
    for (x in walkX(step)) for (y in walkY(step)) for (z in walkZ(step)) body(x, y, z)
}