package org.sandboxpowered.silica.client.model

import com.google.common.collect.Lists
import com.google.gson.*
import org.joml.Vector3f
import org.sandboxpowered.silica.api.util.Direction
import org.sandboxpowered.silica.api.util.extensions.*
import org.sandboxpowered.silica.client.texture.TextureAtlas
import org.sandboxpowered.silica.resources.ResourceManager
import org.sandboxpowered.utilities.Identifier
import java.io.InputStreamReader
import java.lang.reflect.Type
import java.util.*
import kotlin.math.abs

data class BlockModelFormat(
    val ambientOcclusion: Boolean,
    private val elements: List<Element>,
    private val textures: Map<String, Any>,
    val parent: Identifier?
) {
    companion object {
        private val blockModelFormatGson = GsonBuilder()
            .setLenient()
            .registerTypeAdapter(Deserializer())
            .registerTypeAdapter(Texture.Deserializer())
            .registerTypeAdapter(Face.Deserializer())
            .registerTypeAdapter(Element.Deserializer())
            .registerTypeAdapter(IdentifierDeserializer())
            .create()

        operator fun invoke(manager: ResourceManager, id: Identifier): BlockModelFormat =
            InputStreamReader(manager.open(id.affix("models/", ".json")))
                .use { blockModelFormatGson.fromJson(it) }
    }

    private var parentModel: BlockModelFormat? = null

    fun getReferences(modelFunction: (Identifier) -> BlockModelFormat): Collection<TextureAtlas.Reference> {
        val models: MutableSet<BlockModelFormat> = HashSet()
        var model: BlockModelFormat? = this

        //This does the magic parent soul finding adventure where usually it ends with *someone* not having a parent, this is so sad
        while (model?.parent != null && model.parentModel == null) {
            models.add(model)
            var parent: BlockModelFormat? = modelFunction(model.parent!!)
            if (parent == null) {
                // no parent
                println("no parent found for $model this is so sad")
            }
            if (models.contains(parent)) {
                println("found parent loop from $model<->$parent")
                parent = null
            }
            model.parentModel = parent
            model = model.parentModel
        }
        val set: MutableSet<TextureAtlas.Reference> = HashSet()

        getElements().forEach { element ->
            element.faces.forEach { (_, face) ->
                val reference = resolve(face.texture)
                set.add(reference)
            }
        }
        return set
    }


    private fun resolveTexture(string: String): Any {
        var model: BlockModelFormat? = this
        while (model != null) {
            val out = model.textures[string]
            if (out != null) {
                return out
            }
            model = model.parentModel
        }
        return TextureAtlas.Reference(TextureAtlas.BLOCK_ATLAS, TextureAtlas.MISSING_TEXTURE)
    }

    fun resolve(texture: String): TextureAtlas.Reference {
        var tex = texture
        if (tex[0] == '#') {
            tex = tex.substring(1)
        }
        val list = ArrayList<String>()
        while (true) {
            val obj = resolveTexture(tex)
            if (obj is TextureAtlas.Reference) return obj
            require(obj is String) { "Unknown element $obj" }
            tex = obj
            if (list.contains(tex)) {
                //We've hit a loop
                return TextureAtlas.Reference(TextureAtlas.BLOCK_ATLAS, TextureAtlas.MISSING_TEXTURE)
            }
            list.add(tex)
        }
    }

    fun getElements(): List<Element> {
        return elements.ifEmpty { parentModel?.getElements() ?: emptyList() }
    }

    class Deserializer : JsonDeserializer<BlockModelFormat> {
        override fun deserialize(json: JsonElement, type: Type, context: JsonDeserializationContext): BlockModelFormat {
            val obj = json.asJsonObject
            val elements = getElements(context, obj)
            val parent: String = obj.getString("parent", "")
            val textures: Map<String, Any> = getTextures(obj)
            val ao: Boolean = obj.getBoolean("ambientocclusion", true)
            return BlockModelFormat(ao, elements, textures, if (parent.isEmpty()) null else Identifier(parent))
        }

        private fun resolveReference(atlas: Identifier, string: String): Any =
            if (string[0] == '#') string.substring(1)
            else TextureAtlas.Reference(atlas, Identifier(string))

        private fun getTextures(jsonObject: JsonObject): Map<String, Any> {
            val atlas = TextureAtlas.BLOCK_ATLAS
            val textureMap: MutableMap<String, Any> = HashMap()
            if (jsonObject.has("textures")) {
                val textures = jsonObject.getAsJsonObject("textures")
                for ((key, value) in textures.entrySet()) {
                    textureMap[key] = resolveReference(atlas, value.asString)
                }
            }
            return textureMap
        }

        private fun getElements(context: JsonDeserializationContext, obj: JsonObject): List<Element> {
            val elements: MutableList<Element> = Lists.newArrayList()
            if (obj.has("elements")) {
                for (element in obj.getAsJsonArray("elements")) {
                    elements.add(context.deserialize(element))
                }
            }
            return elements
        }
    }

    data class Element(
        val from: Vector3f,
        val to: Vector3f,
        val faces: Map<Direction, Face>,
        val rotation: Rotation?,
        val shade: Boolean
    ) {
        init {
            initTextures()
        }

        private fun initTextures() = faces.filter { (_, face) -> face.textureData.uvs == null }.forEach { (dir, face) ->
            val fs = getRotatedMatrix(dir)
            face.textureData.uvs = fs
        }

        private fun getRotatedMatrix(direction: Direction): FloatArray = when (direction) {
            Direction.DOWN -> floatArrayOf(from.x(), 16f - to.z(), to.x(), 16f - from.z())
            Direction.UP -> floatArrayOf(from.x(), from.z(), to.x(), to.z())
            Direction.NORTH -> floatArrayOf(16f - to.x(), 16f - to.y(), 16f - from.x(), 16f - from.y())
            Direction.SOUTH -> floatArrayOf(from.x(), 16f - to.y(), to.x(), 16f - from.y())
            Direction.WEST -> floatArrayOf(from.z(), 16f - to.y(), to.z(), 16f - from.y())
            Direction.EAST -> floatArrayOf(16f - to.z(), 16f - to.y(), 16f - from.z(), 16f - from.y())
        }

        class Deserializer : JsonDeserializer<Element> {
            override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): Element {
                val obj = json.asJsonObject
                val from = errorOutOfBounds(obj, "from")
                val to = errorOutOfBounds(obj, "to")
                val rotation = deserializeRotation(obj)
                val faces = validateFaces(context, obj)
                val shade = obj.getBoolean("shade", true)
                return Element(from, to, faces, rotation, shade)
            }

            private fun deserializeRotation(obj: JsonObject): Rotation? {
                if (!obj.has("rotation")) return null
                val rotation = obj.getAsJsonObject("rotation")
                return Rotation(
                    toVec(rotation, "origin").mul(0.0625f),
                    getAxis(rotation),
                    getRotationAngle(rotation),
                    obj.getBoolean("resize", false)
                )
            }

            private fun getRotationAngle(obj: JsonObject): Float {
                val angle = obj.getAsJsonPrimitive("angle").asFloat
                require(angle == 0f || abs(angle) == 22.5f || abs(angle) == 45f) { "Invalid rotation $angle, not one of -45/-22.5/0/22.5/45" }
                return angle
            }

            private fun getAxis(jsonObject: JsonObject): Direction.Axis =
                Direction.Axis.valueOf(jsonObject["axis"].asString.uppercase())

            private fun validateFaces(context: JsonDeserializationContext, obj: JsonObject): Map<Direction, Face> =
                getFaces(context, obj).ifEmpty { error("Expected between 1 and 6 unique faces, got 0") }

            private fun getFaces(context: JsonDeserializationContext, jsonObject: JsonObject): Map<Direction, Face> {
                val map: MutableMap<Direction, Face> = EnumMap(Direction::class.java)
                val faces = jsonObject.getAsJsonObject("faces")
                for ((key, value) in faces.entrySet()) {
                    val direction: Direction = Direction.byName(key) ?: error("Unknown direction $key")
                    map[direction] = context.deserialize(value)
                }
                return map
            }

            private fun errorOutOfBounds(jsonObject: JsonObject, member: String): Vector3f {
                val vector3f: Vector3f = toVec(jsonObject, member)
                if (isVecWithinBounds(vector3f)) return vector3f
                error("$member specifier exceeds the allowed boundaries: $vector3f")
            }

            private fun toVec(jsonObject: JsonObject, member: String): Vector3f {
                val array = jsonObject.getAsJsonArray(member)
                require(array.size() == 3) { "Expected 3 $member values, found: ${array.size()}" }
                val vec = FloatArray(3)
                for (i in vec.indices) vec[i] = array[i].asFloat
                return Vector3f(vec[0], vec[1], vec[2])
            }

            private fun isVecWithinBounds(vec: Vector3f): Boolean =
                vec.x >= (-16).toFloat() && vec.y >= (-16).toFloat() && vec.z >= (-16).toFloat() && vec.x <= 32.toFloat() && vec.y <= 32.toFloat() && vec.z <= 32.toFloat()
        }
    }

    data class Rotation(val origin: Vector3f, val axis: Direction.Axis, val angle: Float, val rescale: Boolean)

    data class Face(val cullFace: Direction?, val tintIndex: Int, val texture: String, val textureData: Texture) {
        class Deserializer : JsonDeserializer<Face> {
            override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): Face {
                val obj = json.asJsonObject
                val cullFace = Direction.byName(obj.getString("cullFace", ""))
                val tintIndex = obj.getInt("tintindex", -1)
                val string = obj["texture"].asString
                val texture: Texture = context.deserialize(obj)
                return Face(cullFace, tintIndex, string, texture)
            }
        }
    }

    data class Texture(val rotation: Int, var uvs: FloatArray?) {
        private fun getRotatedUVIndex(rotation: Int): Int = (rotation + this.rotation / 90) % 4

        fun getU(rotation: Int): Float {
            val i = getRotatedUVIndex(rotation)
            return uvs!![if (i != 0 && i != 1) 2 else 0]
        }

        fun getV(rotation: Int): Float {
            val i = getRotatedUVIndex(rotation)
            return uvs!![if (i != 0 && i != 3) 3 else 1]
        }

        class Deserializer : JsonDeserializer<Texture> {
            override fun deserialize(json: JsonElement, type: Type, context: JsonDeserializationContext): Texture {
                val obj = json.asJsonObject
                val uvs = deserializeUVs(obj)
                val rotation = deserializeRotation(obj)
                return Texture(rotation, uvs)
            }

            private fun deserializeRotation(jsonObject: JsonObject): Int {
                val rotation: Int = jsonObject.getInt("rotation", 0)
                require(rotation >= 0 && rotation % 90 == 0 && rotation / 90 <= 3) { "Invalid rotation $rotation found, only 0/90/180/270 allowed" }
                return rotation
            }

            private fun deserializeUVs(jsonObject: JsonObject): FloatArray? {
                return if (jsonObject.has("uv")) {
                    val jsonArray: JsonArray = jsonObject.getAsJsonArray("uv")
                    require(jsonArray.size() == 4) { "Expected 4 uv values, found: ${jsonArray.size()}" }
                    val uvs = FloatArray(4)
                    for (i in uvs.indices) {
                        uvs[i] = jsonArray.get(i).asFloat
                    }
                    return uvs
                } else null
            }
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as Texture

            if (rotation != other.rotation) return false
            if (uvs != null) {
                if (other.uvs == null) return false
                if (!uvs.contentEquals(other.uvs)) return false
            } else if (other.uvs != null) return false

            return true
        }

        override fun hashCode(): Int {
            var result = rotation
            result = 31 * result + (uvs?.contentHashCode() ?: 0)
            return result
        }
    }
}

class IdentifierDeserializer : JsonDeserializer<Identifier> {
    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): Identifier =
        Identifier(json.asString)
}