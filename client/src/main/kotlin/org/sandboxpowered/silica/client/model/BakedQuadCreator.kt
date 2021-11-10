package org.sandboxpowered.silica.client.model

import org.joml.*
import org.sandboxpowered.silica.client.texture.TextureAtlas
import org.sandboxpowered.silica.api.util.Direction
import org.sandboxpowered.silica.util.extensions.getQuaternionAngle
import org.sandboxpowered.silica.util.extensions.minus
import org.sandboxpowered.silica.util.extensions.mulComponents
import org.sandboxpowered.silica.util.extensions.read
import kotlin.math.abs
import kotlin.math.cos

class BakedQuadCreator {
    companion object {
        val CREATOR = BakedQuadCreator()
        private val MIN_SCALE = 1f / cos(0.39269909262657166f) - 1f
        private val MAX_SCALE = 1f / cos(0.7853981852531433f) - 1f
        const val FLOATS_PER_VERTEX = 14
    }

    fun bake(
        from: Vector3fc,
        to: Vector3fc,
        face: BlockModelFormat.Face,
        texture: TextureAtlas.Sprite,
        side: Direction,
        rotation: BlockModelFormat.Rotation?,
        shade: Boolean
    ): BakedQuad {
        //TODO support uv locking
        val vertexPositions = createVertexPositions(from, to)
        val vertices = FloatArray(4 * FLOATS_PER_VERTEX)
        for (corner in 0..3) createVertexData(vertices, corner, side, face.textureData, vertexPositions, rotation)
        val pos1 = Vector3f(vertices[0], vertices[1], vertices[2])
        val pos2 =
            Vector3f(vertices[FLOATS_PER_VERTEX], vertices[FLOATS_PER_VERTEX + 1], vertices[FLOATS_PER_VERTEX + 2])
        val pos3 = Vector3f(
            vertices[FLOATS_PER_VERTEX * 2],
            vertices[FLOATS_PER_VERTEX * 2 + 1],
            vertices[FLOATS_PER_VERTEX * 2 + 2]
        )
        val uv1 = Vector2f(face.textureData.getU(0), face.textureData.getV(0))
        val uv2 = Vector2f(face.textureData.getU(1), face.textureData.getV(1))
        val uv3 = Vector2f(face.textureData.getU(2), face.textureData.getV(2))
        val uv4 = Vector2f(face.textureData.getU(3), face.textureData.getV(3))

        val edge1 = pos2 - pos1
        val edge2 = pos3 - pos1

        val deltaUV1 = uv2 - uv1
        val deltaUV2 = uv3 - uv1

        val f: Float = 1.0f / (deltaUV1.x() * deltaUV2.y() - deltaUV2.x() * deltaUV1.y())

        val tangent1 = Vector3f()
        val bitangent1 = Vector3f()

        tangent1.x = f * (deltaUV2.y() * edge1.x() - deltaUV1.y() * edge2.x())
        tangent1.y = f * (deltaUV2.y() * edge1.y() - deltaUV1.y() * edge2.y())
        tangent1.z = f * (deltaUV2.y() * edge1.z() - deltaUV1.y() * edge2.z())

        bitangent1.x = f * (-deltaUV2.x() * edge1.x() + deltaUV1.x() * edge2.x())
        bitangent1.y = f * (-deltaUV2.x() * edge1.y() + deltaUV1.x() * edge2.y())
        bitangent1.z = f * (-deltaUV2.x() * edge1.z() + deltaUV1.x() * edge2.z())

        for (corner in 0..3) {
            val vertexIndex = corner * FLOATS_PER_VERTEX
            vertices[vertexIndex + 8] = tangent1.x
            vertices[vertexIndex + 9] = tangent1.y
            vertices[vertexIndex + 10] = tangent1.z

            vertices[vertexIndex + 11] = bitangent1.x
            vertices[vertexIndex + 12] = bitangent1.y
            vertices[vertexIndex + 13] = bitangent1.z
        }
        return BakedQuad(vertices, face.tintIndex, side, texture, shade)
    }

    private fun createVertexData(
        vertices: FloatArray,
        cornerIdx: Int,
        side: Direction,
        textureData: BlockModelFormat.Texture,
        vertexPositions: FloatArray,
        rotation: BlockModelFormat.Rotation?
    ) {
        val corner = CubeFace.getCorner(side, cornerIdx)
        val pos = Vector3f(vertexPositions[corner.x], vertexPositions[corner.y], vertexPositions[corner.z])
        val normal = rotateVertex(side, pos, rotation)

        val vertexIndex = cornerIdx * FLOATS_PER_VERTEX
        vertices[vertexIndex] = pos.x()
        vertices[vertexIndex + 1] = pos.y()
        vertices[vertexIndex + 2] = pos.z()
        vertices[vertexIndex + 3] = textureData.getU(cornerIdx)
        vertices[vertexIndex + 4] = textureData.getV(cornerIdx)
        vertices[vertexIndex + 5] = normal.x
        vertices[vertexIndex + 6] = normal.y
        vertices[vertexIndex + 7] = normal.z
    }

    private fun rotateVertex(side: Direction, vec: Vector3f, rot: BlockModelFormat.Rotation?): Vector3f {
        if (rot == null) return side.offset.toFloat()
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
        return side.offset.toFloat().mulDirection(rotationMatrix)
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

private fun Vector3ic.toFloat(): Vector3f = Vector3f(x().toFloat(), y().toFloat(), z().toFloat())
