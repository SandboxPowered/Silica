package org.sandboxpowered.silica.client.texture

import org.sandboxpowered.silica.util.Util.getLogger
import org.sandboxpowered.silica.util.extensions.toPowerOfTwo
import kotlin.math.max


class TextureStitcher(private val maxWidth: Int, private val maxHeight: Int, private val onlySquare: Boolean) {
    companion object {
        val spriteDataComparator: Comparator<TextureAtlas.SpriteData> = Comparator
            .comparingInt<TextureAtlas.SpriteData> { it.height }
            .thenComparingInt { it.width }
            .thenComparing(TextureAtlas.SpriteData::id)
        val logger = getLogger<TextureStitcher>()
    }

    private val dataSet: MutableSet<TextureAtlas.SpriteData> = HashSet(256)
    private val branches: MutableList<Branch> = ArrayList(256)

    var width: Int = 0
    var height: Int = 0

    fun add(data: TextureAtlas.SpriteData) {
        if (!onlySquare || data.isSquare) dataSet.add(data)
        else logger.warn("Attempted to register non-square texture ${data.id} to square-only stitcher [${data.width}x${data.height}]")
    }

    fun stitch() {
        val dataList: List<TextureAtlas.SpriteData> = ArrayList(dataSet)
        dataList.sortedWith(spriteDataComparator)

        dataList.forEach(this::fit)

        width = width.toPowerOfTwo
        height = height.toPowerOfTwo
    }

    private fun fit(data: TextureAtlas.SpriteData) {
        for (branch in branches) {
            if (branch.fit(data)) return
        }
        stretchToFit(data)
    }

    private fun stretchToFit(data: TextureAtlas.SpriteData) {
        val newWidth = (width + data.width).toPowerOfTwo
        val newHeight = (height + data.height).toPowerOfTwo
        val withinWidth = newWidth <= maxWidth
        val withinHeight = newHeight <= maxHeight
        if (withinWidth || withinHeight) {
            val changesWidth = withinWidth && width != newWidth
            val changesHeight = withinHeight && height != newHeight
            val changesX = if (changesWidth xor changesHeight) changesWidth else withinWidth && width <= height

            val branch: Branch
            if (changesX) {
                if (this.height == 0) this.height = data.height
                branch = Branch(width, 0, data.width, height)
                width += data.width
            } else {
                branch = Branch(0, height, width, data.height)
                height += data.height
            }

            branch.fit(data)
            branches.add(branch)
        }
    }

    fun loopBranches(function: (TextureAtlas.SpriteData, Int, Int) -> Unit) {
        branches.forEach { mainBranch ->
            mainBranch.loopBranches { function(it.sprite, it.x, it.y) }
        }
    }

    class Branch(val x: Int, val y: Int, val width: Int, val height: Int) {
        fun fit(data: TextureAtlas.SpriteData): Boolean {
            if (spriteData != null) return false
            val (_, dataWidth, dataHeight) = data
            if (dataWidth <= width && dataHeight <= height) {
                if (dataWidth == width && dataHeight == height) {
                    spriteData = data
                    return true
                } else {
                    var branches = subBranches
                    if (branches == null) {
                        branches = ArrayList(1)
                        branches.add(Branch(x, y, dataWidth, dataHeight))

                        val slotWidth = width - dataWidth
                        val slotHeight = height - dataHeight

                        if (slotHeight > 0 && slotWidth > 0) {
                            val maxHeight = max(height, slotWidth)
                            val maxWidth = max(width, slotHeight)
                            if (maxHeight >= maxWidth) {
                                branches.add(Branch(x, y + dataHeight, dataWidth, slotHeight))
                                branches.add(Branch(x + dataWidth, y, slotWidth, height))
                            } else {
                                branches.add(Branch(x + dataWidth, y, slotWidth, dataHeight))
                                branches.add(Branch(x, y + dataHeight, width, slotHeight))
                            }
                        } else if (slotWidth == 0) {
                            branches.add(Branch(x, y + dataHeight, dataWidth, slotHeight))
                        } else if (slotHeight == 0) {
                            branches.add(Branch(x + dataWidth, y, slotWidth, dataHeight))
                        }

                        this.subBranches = branches
                    }

                    return branches.firstOrNull { it.fit(data) } != null
                }
            } else {
                return false
            }
        }

        fun loopBranches(function: (Branch) -> Unit) {
            subBranches?.forEach(function)
        }

        private var subBranches: MutableList<Branch>? = null
        private var spriteData: TextureAtlas.SpriteData? = null

        val sprite: TextureAtlas.SpriteData
            get() = spriteData!!
    }
}