package org.sandboxpowered.silica.client.model

import com.google.common.collect.Lists
import com.google.gson.*
import com.google.gson.JsonParseException
import org.joml.Vector3f
import org.sandboxpowered.silica.client.texture.TextureAtlas
import org.sandboxpowered.silica.util.Identifier
import org.sandboxpowered.silica.util.content.Direction
import org.sandboxpowered.silica.util.extensions.*
import java.lang.reflect.Type
import java.util.*
import kotlin.math.abs


val jsonModelGson = GsonBuilder()
    .setLenient()
    .registerTypeAdapter(JSONModel.Deserializer())
    .registerTypeAdapter(JSONTexture.Deserializer())
    .registerTypeAdapter(JSONFace.Deserializer())
    .registerTypeAdapter(JSONElement.Deserializer())
    .registerTypeAdapter(IdentifierDeserializer())
    .create()

data class JSONModel(
    val ambientOcclusion: Boolean,
    private val elements: List<JSONElement>,
    private val textures: Map<String, Any>,
    val parent: Identifier?
) {
    private var parentModel: JSONModel? = null

    fun getReferences(modelFunction: (Identifier) -> JSONModel): Collection<TextureAtlas.Reference> {
        val models: MutableSet<JSONModel> = HashSet()
        var model: JSONModel? = this

        //This does the magic parent soul finding adventure where usually it ends with *someone* not having a parent, this is so sad
        while (model?.parent != null && model.parentModel == null) {
            models.add(model)
            var parent: JSONModel? = modelFunction(model.parent!!)
            if (parent == null) {
                // no parent
                System.out.printf("no parent found for %s this is so sad%n", model)
            }
            if (models.contains(parent)) {
                System.out.printf("found parent loop from %s %s%n", model, parent)
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
        var model: JSONModel? = this
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
            require(obj is String) { String.format("Unknown element %s", obj) }
            tex = obj
            if (list.contains(tex)) {
                //We've hit a loop
                return TextureAtlas.Reference(TextureAtlas.BLOCK_ATLAS, TextureAtlas.MISSING_TEXTURE)
            }
            list.add(tex)
        }
    }

    fun getElements(): List<JSONElement> {
        return elements.ifEmpty { parentModel?.getElements() ?: emptyList() }
    }

    class Deserializer : JsonDeserializer<JSONModel> {
        override fun deserialize(json: JsonElement, type: Type, context: JsonDeserializationContext): JSONModel {
            val obj = json.asJsonObject
            val elements = getElements(context, obj)
            val parent: String = obj.getString("parent", "")
            val textures: Map<String, Any> = getTextures(obj)
            val ao: Boolean = obj.getBoolean("ambientocclusion", true)
            return JSONModel(ao, elements, textures, if (parent.isEmpty()) null else Identifier(parent))
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

        private fun getElements(context: JsonDeserializationContext, obj: JsonObject): List<JSONElement> {
            val elements: MutableList<JSONElement> = Lists.newArrayList()
            if (obj.has("elements")) {
                for (element in obj.getAsJsonArray("elements")) {
                    elements.add(context.deserialize(element))
                }
            }
            return elements
        }
    }
}

class IdentifierDeserializer : JsonDeserializer<Identifier> {
    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): Identifier {
        return Identifier(json.asString)
    }
}

data class JSONElement(
    val from: Vector3f,
    val to: Vector3f,
    val faces: Map<Direction, JSONFace>,
    val rotation: JSONRotation?,
    val shade: Boolean
) {
    init {
        initTextures()
    }

    private fun initTextures() = faces.filter { (_, face) -> face.textureData.uvs == null }.forEach { (dir, face) ->
        val fs = getRotatedMatrix(dir)
        face.textureData.uvs = fs
    }

    private fun getRotatedMatrix(direction: Direction): FloatArray {
        return when (direction) {
            Direction.DOWN -> floatArrayOf(from.x(), 16.0f - to.z(), to.x(), 16.0f - from.z())
            Direction.UP -> floatArrayOf(from.x(), from.z(), to.x(), to.z())
            Direction.NORTH -> floatArrayOf(16.0f - to.x(), 16.0f - to.y(), 16.0f - from.x(), 16.0f - from.y())
            Direction.SOUTH -> floatArrayOf(from.x(), 16.0f - to.y(), to.x(), 16.0f - from.y())
            Direction.WEST -> floatArrayOf(from.z(), 16.0f - to.y(), to.z(), 16.0f - from.y())
            Direction.EAST -> floatArrayOf(16.0f - to.z(), 16.0f - to.y(), 16.0f - from.z(), 16.0f - from.y())
        }
    }

    class Deserializer : JsonDeserializer<JSONElement> {
        override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): JSONElement {
            val obj = json.asJsonObject
            val from = errorOutOfBounds(obj, "from")
            val to = errorOutOfBounds(obj, "to")
            val rotation = deserializeRotation(obj)
            val faces = validateFaces(context, obj)
            val shade = obj.getBoolean("shade", true)
            return JSONElement(from, to, faces, rotation, shade)
        }

        private fun deserializeRotation(obj: JsonObject): JSONRotation? {
            if (!obj.has("rotation")) return null
            val rotation = obj.getAsJsonObject("rotation")
            return JSONRotation(
                toVec(rotation, "origin").mul(0.0625f),
                getAxis(rotation),
                getRotationAngle(rotation),
                obj.getBoolean("resize", false)
            )
        }

        @Throws(JsonParseException::class)
        private fun getRotationAngle(obj: JsonObject): Float {
            val angle = obj.getAsJsonPrimitive("angle").asFloat
            return if (angle != 0.0f && abs(angle) != 22.5f && abs(angle) != 45.0f) {
                throw JsonParseException("Invalid rotation $angle, not one of -45/-22.5/0/22.5/45")
            } else {
                angle
            }
        }

        private fun getAxis(jsonObject: JsonObject): Direction.Axis {
            return Direction.Axis.valueOf(jsonObject["axis"].asString.uppercase())
        }

        private fun validateFaces(
            jsonDeserializationContext: JsonDeserializationContext,
            jsonObject: JsonObject
        ): Map<Direction, JSONFace> {
            val map: Map<Direction, JSONFace> = getFaces(jsonDeserializationContext, jsonObject)
            return map.ifEmpty { error("Expected between 1 and 6 unique faces, got 0") }
        }

        private fun getFaces(context: JsonDeserializationContext, jsonObject: JsonObject): Map<Direction, JSONFace> {
            val map: MutableMap<Direction, JSONFace> = EnumMap(Direction::class.java)
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
            error("'$member' specifier exceeds the allowed boundaries: $vector3f")
        }

        private fun toVec(jsonObject: JsonObject, member: String): Vector3f {
            val array = jsonObject.getAsJsonArray(member)
            return if (array.size() != 3) {
                error("Expected 3 $member values, found: ${array.size()}")
            } else {
                val vec = FloatArray(3)
                for (i in vec.indices) vec[i] = array[i].asFloat
                Vector3f(vec[0], vec[1], vec[2])
            }
        }

        private fun isVecWithinBounds(vec: Vector3f): Boolean =
            vec.x >= (-16).toFloat() && vec.y >= (-16).toFloat() && vec.z >= (-16).toFloat() && vec.x <= 32.toFloat() && vec.y <= 32.toFloat() && vec.z <= 32.toFloat()
    }
}

data class JSONRotation(val origin: Vector3f, val axis: Direction.Axis, val angle: Float, val rescale: Boolean)

data class JSONFace(val cullFace: Direction?, val tintIndex: Int, val texture: String, val textureData: JSONTexture) {
    class Deserializer : JsonDeserializer<JSONFace> {
        override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): JSONFace {
            val obj = json.asJsonObject
            val cullFace = Direction.byName(obj.getString("cullFace", ""))
            val tintIndex = obj.getInt("tintindex", -1)
            val string = obj["texture"].asString
            val texture: JSONTexture = context.deserialize(obj)
            return JSONFace(cullFace, tintIndex, string, texture)
        }
    }
}

data class JSONTexture(val rotation: Int, var uvs: FloatArray?) {
    class Deserializer : JsonDeserializer<JSONTexture> {
        override fun deserialize(json: JsonElement, type: Type, context: JsonDeserializationContext): JSONTexture {
            val obj = json.asJsonObject
            val uvs = deserializeUVs(obj)
            val rotation = deserializeRotation(obj)
            return JSONTexture(rotation, uvs)
        }

        private fun deserializeRotation(jsonObject: JsonObject): Int {
            val rotation: Int = jsonObject.getInt("rotation", 0)
            if (rotation >= 0 && rotation % 90 == 0 && rotation / 90 <= 3) return rotation
            else error("Invalid rotation $rotation found, only 0/90/180/270 allowed")
        }

        private fun deserializeUVs(jsonObject: JsonObject): FloatArray? {
            return if (!jsonObject.has("uv")) {
                null
            } else {
                val jsonArray: JsonArray = jsonObject.getAsJsonArray("uv")
                if (jsonArray.size() != 4) {
                    throw JsonParseException("Expected 4 uv values, found: " + jsonArray.size())
                } else {
                    val uvs = FloatArray(4)
                    for (i in uvs.indices) {
                        uvs[i] = jsonArray.get(i).asFloat
                    }
                    uvs
                }
            }
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as JSONTexture

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