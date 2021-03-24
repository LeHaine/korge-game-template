package com.lehaine.ldtkbase.ldtk

import com.lehaine.ldtk.*
import com.soywiz.kds.FastIdentityMap
import com.soywiz.kds.Pool
import com.soywiz.kds.clear
import com.soywiz.kds.getOrPut
import com.soywiz.kds.iterators.fastForEach
import com.soywiz.kmem.nextPowerOfTwo
import com.soywiz.korge.render.RenderContext
import com.soywiz.korge.render.TexturedVertexArray
import com.soywiz.korge.view.Container
import com.soywiz.korge.view.View
import com.soywiz.korge.view.addTo
import com.soywiz.korge.view.tiles.TileSet
import com.soywiz.korim.bitmap.Bitmap
import com.soywiz.korim.bitmap.Bitmaps
import com.soywiz.korim.bitmap.BmpSlice
import com.soywiz.korim.color.ColorAdd
import com.soywiz.korim.color.Colors
import com.soywiz.korim.color.RGBA
import com.soywiz.korma.geom.Point
import com.soywiz.korma.geom.Rectangle
import com.soywiz.korui.layout.MathEx.max
import com.soywiz.korui.layout.MathEx.min

inline fun Container.ldtkLayer(
    layer: Layer,
    tileset: TileSet? = null,
    callback: LDtkLayerView.() -> Unit = {}
) =
    LDtkLayerView(layer, tileset).addTo(this, callback)

class LDtkLayerView(val layer: Layer, val tileset: TileSet? = null) : View() {
    private var contentVersion = 0
    private var cachedContentVersion = 0

    private val verticesPerTex = FastIdentityMap<Bitmap, Info>()
    private val infos = arrayListOf<Info>()
    private val infosPool = Pool { Info(Bitmaps.transparent.bmpBase, dummyTexturedVertexArray) }

    private val t0 = Point(0, 0)
    private val tt0 = Point(0, 0)
    private val tt1 = Point(0, 0)
    private val tt2 = Point(0, 0)
    private val tt3 = Point(0, 0)

    private val indices = IntArray(4)
    private val tempX = FloatArray(4)
    private val tempY = FloatArray(4)

    private var lastVirtualRect = Rectangle(-1, -1, -1, -1)
    private var currentVirtualRect = Rectangle(-1, -1, -1, -1)

    private val tileSize = layer.gridSize

    override fun renderInternal(ctx: RenderContext) {
        if (!visible) {
            return
        }
        currentVirtualRect.setBounds(ctx.virtualLeft, ctx.virtualTop, ctx.virtualRight, ctx.virtualBottom)
        if (currentVirtualRect != lastVirtualRect) {
            dirtyVertices = true
            lastVirtualRect.copyFrom(currentVirtualRect)
        }

        computeVertexIfRequired(ctx)

        infos.fastForEach { buffer ->
            ctx.batch.drawVertices(
                buffer.vertices, ctx.getTex(buffer.tex), false, renderBlendMode.factors, buffer.vcount, buffer.icount
            )
        }
        ctx.flush()
    }

    override fun getLocalBoundsInternal(out: Rectangle) {
        out.setTo(0, 0, layer.cWidth * tileSize, layer.cHeight * tileSize)
    }

    private fun computeVertexIfRequired(ctx: RenderContext) {
        if (!dirtyVertices && cachedContentVersion == contentVersion) return
        cachedContentVersion = contentVersion
        dirtyVertices = false
        val m = globalMatrix

        val renderTilesCounter = ctx.stats.counter("renderedTiles")


        val posX = m.transformX(0.0, 0.0)
        val posY = m.transformY(0.0, 0.0)
        val dUX = m.transformX(tileSize.toDouble(), 0.0) - posX
        val dUY = m.transformY(tileSize.toDouble(), 0.0) - posY
        val dVX = m.transformX(0.0, tileSize.toDouble()) - posX
        val dVY = m.transformY(0.0, tileSize.toDouble()) - posY


        val colMul = renderColorMul
        val colAdd = renderColorAdd

        val pp0 = globalToLocal(t0.setTo(currentVirtualRect.left, currentVirtualRect.top), tt0)
        val pp1 = globalToLocal(t0.setTo(currentVirtualRect.right, currentVirtualRect.bottom), tt1)
        val pp2 = globalToLocal(t0.setTo(currentVirtualRect.right, currentVirtualRect.top), tt2)
        val pp3 = globalToLocal(t0.setTo(currentVirtualRect.left, currentVirtualRect.bottom), tt3)
        val mx0 = ((pp0.x / tileSize) - 1).toInt()
        val mx1 = ((pp1.x / tileSize) + 1).toInt()
        val mx2 = ((pp2.x / tileSize) + 1).toInt()
        val mx3 = ((pp3.x / tileSize) + 1).toInt()
        val my0 = ((pp0.y / tileSize) - 1).toInt()
        val my1 = ((pp1.y / tileSize) + 1).toInt()
        val my2 = ((pp2.y / tileSize) + 1).toInt()
        val my3 = ((pp3.y / tileSize) + 1).toInt()

        val ymin = max(min(min(min(my0, my1), my2), my3), 0)
        val ymax = min(max(max(max(my0, my1), my2), my3), layer.cHeight)
        val xmin = max(min(min(min(mx0, mx1), mx2), mx3), 0)
        val xmax = min(max(max(max(mx0, mx1), mx2), mx3), layer.cWidth)


        val yheight = ymax - ymin
        val xwidth = xmax - xmin
        val ntiles = xwidth * yheight
        val allocTiles = ntiles.nextPowerOfTwo

        infos.fastForEach { infosPool.free(it) }
        verticesPerTex.clear()
        infos.clear()

        var count = 0
        for (cy in ymin until ymax) {
            for (cx in xmin until xmax) {
                when (layer.type) {
                    LayerType.IntGrid -> {
                        if (layer is LayerIntGridAutoLayer) {
                            val tile = layer.autoTilesCoordIdMap[layer.getCoordId(cx, cy)] ?: continue
                            renderTile(
                                tileset?.get(tile.tileId) ?: error("unable to get tile from tileset!"),
                                tile.flips,
                                cx,
                                cy,
                                posX,
                                posY,
                                dUX,
                                dUY,
                                dVX,
                                dVY,
                                allocTiles,
                                colAdd,
                                colMul
                            )
                        } else {
                            layer as LayerIntGrid
                            if (layer.hasValue(cx, cy)) {
                                val hex = layer.getColorHex(cx, cy) ?: continue
                                val color = Colors[hex]
                                renderTile(
                                    Bitmaps.white,
                                    0,
                                    cx,
                                    cy,
                                    posX,
                                    posY,
                                    dUX,
                                    dUY,
                                    dVX,
                                    dVY,
                                    allocTiles,
                                    colAdd,
                                    color
                                )
                            }
                        }
                    }
                    LayerType.Tiles -> {
                        layer as LayerTiles
                        if (layer.hasAnyTileAt(cx, cy)) {
                            layer.getTileStackAt(cx, cy).forEach {
                                count++
                                renderTile(
                                    tileset?.get(it.tileId) ?: error("unable to get tile from tileset!"),
                                    it.flipBits,
                                    cx,
                                    cy,
                                    posX,
                                    posY,
                                    dUX,
                                    dUY,
                                    dVX,
                                    dVY,
                                    allocTiles,
                                    colAdd,
                                    colMul
                                )
                            }
                        }
                    }
                    LayerType.AutoLayer -> {
                        layer as LayerAutoLayer
                        val tile = layer.autoTiles.findTileAt(cx, cy) ?: continue
                        renderTile(
                            tileset?.get(tile.tileId) ?: error("unable to get tile from tileset!"),
                            tile.flips,
                            cx,
                            cy,
                            posX,
                            posY,
                            dUX,
                            dUY,
                            dVX,
                            dVY,
                            allocTiles,
                            colAdd,
                            colMul
                        )
                    }
                    else -> error("Layer of type ${layer.type} is not supported for rendering!")
                }

            }
        }

        renderTilesCounter.increment(count)
    }

    private fun List<LayerAutoLayer.AutoTile>.findTileAt(cx: Int, cy: Int): LayerAutoLayer.AutoTile? {
        return if (layer.isCoordValid(cx, cy)) {
            return find { it.renderX == cx * tileSize && it.renderY == cy * tileSize }
        } else {
            null
        }
    }

    private fun renderTile(
        tex: BmpSlice,
        flipBits: Int,
        cx: Int,
        cy: Int,
        posX: Double,
        posY: Double,
        dUX: Double,
        dUY: Double,
        dVX: Double,
        dVY: Double,
        allocTiles: Int,
        colAdd: ColorAdd,
        colMul: RGBA
    ) {
        val flipX = flipBits == 1 || flipBits == 3
        val flipY = flipBits == 2 || flipBits == 3

        val info = verticesPerTex.getOrPut(tex.bmpBase) {
            infosPool.alloc().also { info ->
                info.tex = tex.bmpBase
                if (info.vertices.initialVcount < allocTiles * 4) {
                    info.vertices =
                        TexturedVertexArray(allocTiles * 4, TexturedVertexArray.quadIndices(allocTiles))
                }
                info.vcount = 0
                info.icount = 0
                infos += info
            }
        }
        run {
            val p0X = posX + (dUX * cx) + (dVX * cy)
            val p0Y = posY + (dUY * cx) + (dVY * cy)

            val p1X = p0X + dUX
            val p1Y = p0Y + dUY

            val p2X = p0X + dUX + dVX
            val p2Y = p0Y + dUY + dVY

            val p3X = p0X + dVX
            val p3Y = p0Y + dVY

            tempX[0] = tex.tl_x
            tempX[1] = tex.tr_x
            tempX[2] = tex.br_x
            tempX[3] = tex.bl_x

            tempY[0] = tex.tl_y
            tempY[1] = tex.tr_y
            tempY[2] = tex.br_y
            tempY[3] = tex.bl_y

            computeIndices(flipX = flipX, flipY = flipY, rotate = false, indices = indices)

            info.vertices.quadV(
                info.vcount++,
                p0X,
                p0Y,
                tempX[indices[0]],
                tempY[indices[0]],
                colMul,
                colAdd
            )
            info.vertices.quadV(
                info.vcount++,
                p1X,
                p1Y,
                tempX[indices[1]],
                tempY[indices[1]],
                colMul,
                colAdd
            )
            info.vertices.quadV(
                info.vcount++,
                p2X,
                p2Y,
                tempX[indices[2]],
                tempY[indices[2]],
                colMul,
                colAdd
            )
            info.vertices.quadV(
                info.vcount++,
                p3X,
                p3Y,
                tempX[indices[3]],
                tempY[indices[3]],
                colMul,
                colAdd
            )
        }

        info.icount += 6
    }

    private fun computeIndices(
        flipX: Boolean,
        flipY: Boolean,
        rotate: Boolean,
        indices: IntArray = IntArray(4)
    ): IntArray {
        indices[0] = 0 // TL
        indices[1] = 1 // TR
        indices[2] = 2 // BR
        indices[3] = 3 // BL

        if (rotate) {
            indices.swap(TR, BL)
        }
        if (flipY) {
            indices.swap(TL, BL)
            indices.swap(TR, BR)
        }
        if (flipX) {
            indices.swap(TL, TR)
            indices.swap(BL, BR)
        }
        return indices
    }

    private fun IntArray.swap(a: Int, b: Int): IntArray = this.apply {
        val t = this[a]
        this[a] = this[b]
        this[b] = t
    }

    private class Info(var tex: Bitmap, var vertices: TexturedVertexArray) {
        var vcount = 0
        var icount = 0
    }

    companion object {
        private val dummyTexturedVertexArray = TexturedVertexArray(0, IntArray(0))

        private const val TL = 0
        private const val TR = 1
        private const val BR = 2
        private const val BL = 3
    }
}