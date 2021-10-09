package org.sandboxpowered.silica.client.model

import org.sandboxpowered.silica.client.SilicaClient
import org.sandboxpowered.silica.client.texture.TextureAtlas
import org.sandboxpowered.silica.client.texture.TextureStitcher
import org.sandboxpowered.silica.util.Identifier
import org.sandboxpowered.silica.world.state.block.BlockState

class ModelLoader(val client: SilicaClient, stitcher: TextureStitcher) {
    private val map = HashMap<Identifier, BlockModelFormat>()

    val func: (Identifier) -> BlockModelFormat = {
        map.computeIfAbsent(it) { id -> BlockModelFormat(client.assetManager, id) }
    }

    init {
        func(Identifier("block/andesite"))

        map.values.forEach {
            it.getReferences(func).forEach {
                stitcher.add(TextureAtlas.SpriteReference(it.texture, client.assetManager))
            }
        }
    }

    fun getModel(state: BlockState): BakedModel {
        val jsonModel = func(Identifier("block/andesite"))
        return JSONBakedModel(jsonModel) {
            client.renderer.textureAtlas.getSprite(it.texture)!!
        }
    }
}