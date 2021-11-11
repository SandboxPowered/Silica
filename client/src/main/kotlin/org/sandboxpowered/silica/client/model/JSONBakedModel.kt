package org.sandboxpowered.silica.client.model

import org.sandboxpowered.silica.api.util.Direction
import org.sandboxpowered.silica.api.world.state.block.BlockState
import org.sandboxpowered.silica.client.texture.TextureAtlas
import kotlin.random.Random

class JSONBakedModel(val jsonModel: BlockModelFormat, val func: (TextureAtlas.Reference) -> TextureAtlas.Sprite) :
    BakedModel {
    override fun getQuads(state: BlockState, side: Direction?, rand: Random): Collection<BakedQuad> {
        val set = HashSet<BakedQuad>()
        jsonModel.getElements().forEach { element ->
            element.faces.filter { (dir, face) -> face.cullFace == side }.forEach { (dir, face) ->
                val quad = BakedQuadCreator.CREATOR.bake(
                    element.from,
                    element.to,
                    face,
                    func(jsonModel.resolve(face.texture)),
                    dir,
                    element.rotation,
                    element.shade
                )
                set.add(quad)
            }
        }
        return set
    }
}