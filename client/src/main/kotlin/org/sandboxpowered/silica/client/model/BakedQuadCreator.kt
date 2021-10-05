package org.sandboxpowered.silica.client.model

import org.joml.Matrix4f
import org.joml.Vector3f
import org.joml.Vector3fc
import org.joml.Vector4f
import org.sandboxpowered.silica.client.texture.TextureAtlas
import org.sandboxpowered.silica.util.content.Direction
import org.sandboxpowered.silica.util.extensions.getQuaternionAngle
import org.sandboxpowered.silica.util.extensions.mulComponents
import org.sandboxpowered.silica.util.extensions.read
import kotlin.math.abs
import kotlin.math.cos

class BakedQuadCreator {
    companion object {
        val CREATOR = BakedQuadCreator()
        private val MIN_SCALE = 1f / cos(0.39269909262657166f) - 1f
        private val MAX_SCALE = 1f / cos(0.7853981852531433f) - 1f
    }

    fun bake(
        from: Vector3fc,
        to: Vector3fc,
        face: JSONFace,
        texture: TextureAtlas.Sprite,
        side: Direction,
        rotation: JSONRotation?,
        shade: Boolean
    ): BakedQuad {
        //TODO support uv locking
        val vertexPositions = createVertexPositions(from, to)
        val vertices = FloatArray(4 * 8)
        for (corner in 0..3) createVertexData(vertices, corner, side, face.textureData, vertexPositions, rotation)
        return BakedQuad(vertices, face.tintIndex, side, texture, shade)
    }

    private fun createVertexData(
        vertices: FloatArray,
        cornerIdx: Int,
        side: Direction,
        textureData: JSONTexture,
        vertexPositions: FloatArray,
        rotation: JSONRotation?
    ) {
        val corner = CubeFace.getCorner(side, cornerIdx)
        val pos = Vector3f(vertexPositions[corner.x], vertexPositions[corner.y], vertexPositions[corner.z])
        rotateVertex(pos, rotation)

        val vertexIndex = cornerIdx * 8
        vertices[vertexIndex] = pos.x()
        vertices[vertexIndex + 1] = pos.y()
        vertices[vertexIndex + 2] = pos.z()
        vertices[vertexIndex + 3] = textureData.getU(cornerIdx)
        vertices[vertexIndex + 4] = textureData.getV(cornerIdx)
        //TODO generate normals & other data
    }

    private fun rotateVertex(vec: Vector3f, rot: JSONRotation?) {
        if (rot == null) return
        val (dir, scale) = when (rot.axis) {
            Direction.Axis.X -> Vector3f(1f, 0f, 0f) to Vector3f(0f, 1f, 1f)
            Direction.Axis.Y -> Vector3f(0f, 1f, 0f) to Vector3f(1f, 0f, 1f)
            Direction.Axis.Z -> Vector3f(0f, 0f, 1f) to Vector3f(1f, 1f, 0f)
        }
        if (rot.rescale) {
            if (abs(rot.angle) == 22.5f) scale.mul(MIN_SCALE) else scale.mul(MAX_SCALE)
            scale.add(1f, 1f, 1f)
        } else scale.set(1f, 1f, 1f)

        val rotationMatrix = Matrix4f().read(getQuaternionAngle(dir, rot.angle, true))

        val origin = rot.origin

        val vec4 = Vector4f(vec.x() - origin.x(), vec.y() - origin.y(), vec.z() - origin.z(), 1f)
        vec4.mulTranspose(rotationMatrix)
        vec4.mulComponents(scale)
        vec.set(vec4.x + origin.x(), vec4.y + origin.y(), vec4.z + origin.z())
    }

    private fun createVertexPositions(from: Vector3fc, to: Vector3fc) = FloatArray(6).apply {
        this[Direction.WEST.id] = from.x() / 16f
        this[Direction.DOWN.id] = from.y() / 16f
        this[Direction.NORTH.id] = from.z() / 16f
        this[Direction.EAST.id] = to.x() / 16f
        this[Direction.UP.id] = to.y() / 16f
        this[Direction.SOUTH.id] = to.z() / 16f
    }
}
