package com.lehaine.lib

import com.soywiz.kds.iterators.fastForEach
import com.soywiz.korge.view.*
import com.soywiz.korim.bitmap.Bitmaps
import com.soywiz.korim.bitmap.BmpSlice
import com.soywiz.korio.lang.Cancellable
import com.soywiz.korio.lang.cancel
import com.soywiz.korma.geom.vector.VectorPath

inline fun Container.enhancedSprite(
    bitmap: BmpSlice = Bitmaps.white,
    anchorX: Double = 0.0,
    anchorY: Double = 0.0,
    callback: @ViewDslMarker Sprite.() -> Unit = {}
): EnhancedSprite = EnhancedSprite(bitmap, anchorX, anchorY).addTo(this, callback)

class EnhancedSprite(
    bitmap: BmpSlice = Bitmaps.white,
    anchorX: Double = 0.0,
    anchorY: Double = anchorX,
    hitShape: VectorPath? = null,
    smoothing: Boolean = true
) : Sprite(bitmap, anchorX, anchorY, hitShape, smoothing) {

    val anim = AnimationManager()

    private var updater: Cancellable? = null

    private fun addUpdateComponent() {
        updater = addUpdater { anim.update() }
    }

    inner class AnimationManager {

        private val states = arrayListOf<AnimationState>()

        /**
         * Priority is represented by the deepest. The deepest has top priority while the shallowest has lowest.
         */
        fun registerState(anim: SpriteAnimation, loop: Boolean = true, reason: () -> Boolean) {
            if (updater == null) {
                addUpdateComponent()
            }
            states.add(AnimationState(anim, loop, reason))
        }

        fun removeState(anim: SpriteAnimation) {
            states.find { it.anim == anim }?.also { states.remove(it) }
            if (states.size == 0) {
                updater?.cancel()
                updater = null
            }
        }

        fun removeAllStates() {
            states.clear()
            if (states.size == 0) {
                updater?.cancel()
                updater = null
            }
        }

        internal fun update() {
            states.fastForEach { state ->
                if (state.reason()) {
                    if (state.loop) {
                        playAnimationLooped(state.anim)
                    } else {
                        playAnimation(state.anim)
                    }
                    return
                }
            }
        }
    }

    private data class AnimationState(val anim: SpriteAnimation, val loop: Boolean, val reason: () -> Boolean)
}

fun EnhancedSprite.registerState(anim: SpriteAnimation, loop: Boolean = true, reason: () -> Boolean) =
    this.anim.registerState(anim, loop, reason)

fun EnhancedSprite.removeState(anim: SpriteAnimation) = this.anim.removeState(anim)
fun EnhancedSprite.removeAllStates() = this.anim.removeAllStates()