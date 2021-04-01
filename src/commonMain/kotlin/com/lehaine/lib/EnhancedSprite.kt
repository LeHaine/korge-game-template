package com.lehaine.lib

import com.soywiz.kds.iterators.fastForEach
import com.soywiz.klock.TimeSpan
import com.soywiz.klock.milliseconds
import com.soywiz.kmem.umod
import com.soywiz.korge.view.*
import com.soywiz.korim.bitmap.Bitmaps
import com.soywiz.korim.bitmap.BmpSlice
import com.soywiz.korio.async.Signal
import com.soywiz.korma.geom.vector.VectorPath

inline fun Container.enhancedSprite(
    bitmap: BmpSlice = Bitmaps.white,
    anchorX: Double = 0.0,
    anchorY: Double = 0.0,
    hitShape: VectorPath? = null,
    smoothing: Boolean = true,
    callback: @ViewDslMarker EnhancedSprite.() -> Unit = {}
): EnhancedSprite = EnhancedSprite(bitmap, anchorX, anchorY, hitShape, smoothing).addTo(this, callback)

class EnhancedSprite(
    bitmap: BmpSlice = Bitmaps.white,
    anchorX: Double = 0.0,
    anchorY: Double = anchorX,
    hitShape: VectorPath? = null,
    smoothing: Boolean = true
) : BaseImage(bitmap, anchorX, anchorY, hitShape, smoothing) {

    private var animationRequested = false
    var totalFramesPlayed = 0
    private var animationNumberOfFramesRequested = 0
        set(value) {
            if (value == 0) {
                stopAnimation()
                when (animationType) {
                    AnimationType.STANDARD -> triggerEvent(onAnimationCompleted)
                    else -> triggerEvent(onAnimationStopped)
                }
            }
            field = value
        }
    private var animationType = AnimationType.STANDARD

    private var _onAnimationCompleted: Signal<SpriteAnimation>? = null
    private var _onAnimationStopped: Signal<SpriteAnimation>? = null
    private var _onAnimationStarted: Signal<SpriteAnimation>? = null
    private var _onFrameChanged: Signal<SpriteAnimation>? = null

    val onAnimationCompleted: Signal<SpriteAnimation>
        get() {
            if (_onAnimationCompleted == null) _onAnimationCompleted = Signal()
            return _onAnimationCompleted!!
        }

    val onAnimationStopped: Signal<SpriteAnimation>
        get() {
            if (_onAnimationStopped == null) _onAnimationStopped = Signal()
            return _onAnimationStopped!!
        }

    val onAnimationStarted: Signal<SpriteAnimation>
        get() {
            if (_onAnimationStarted == null) _onAnimationStarted = Signal()
            return _onAnimationStarted!!
        }

    val onFrameChanged: Signal<SpriteAnimation>
        get() {
            if (_onFrameChanged == null) _onFrameChanged = Signal()
            return _onFrameChanged!!
        }

    var spriteDisplayTime: TimeSpan = 50.milliseconds
    private var animationLooped = false
    private var lastAnimationFrameTime: TimeSpan = 0.milliseconds
    private var animationRemainingDuration: TimeSpan = 0.milliseconds
        set(value) {
            if (value <= 0.milliseconds && animationType == AnimationType.DURATION) {
                stopAnimation()
                triggerEvent(_onAnimationCompleted)
            }
            field = value
        }

    private var currentAnimation: SpriteAnimation? = null

    var currentSpriteIndex = 0
        private set(value) {
            field = value umod totalFrames
            bitmap = currentAnimation?.getSprite(value) ?: bitmap
        }

    private var reversed = false

    private var lastAnimation: SpriteAnimation? = null
    private var lastReversed: Boolean = false
    private var lastLooped: Boolean = false
    private var overlapPlaying: Boolean = false

    val anim = AnimationManager()

    init {
        addUpdater { frameTime ->
            //println("UPDATER: animationRequested=$animationRequested")
            if (animationRequested) {
                nextSprite(frameTime)
            }
        }
    }

    private fun getDefaultTime(spriteAnimation: SpriteAnimation?): TimeSpan = when {
        spriteAnimation != null && spriteAnimation.defaultTimePerFrame != TimeSpan.NIL -> spriteAnimation.defaultTimePerFrame
        else -> spriteDisplayTime
    }

    fun playOverlap(
        spriteAnimation: SpriteAnimation,
        spriteDisplayTime: TimeSpan = getDefaultTime(spriteAnimation)
    ) {
        lastAnimation = currentAnimation
        lastLooped = animationLooped
        lastReversed = reversed
        overlapPlaying = true
        playAnimation(spriteAnimation, spriteDisplayTime, startFrame = 0)
    }

    fun playOverlap(bmpSlice: BmpSlice, spriteDisplayTime: TimeSpan = 50.milliseconds, numFrames: Int = 1) =
        playOverlap(SpriteAnimation(List(numFrames) { bmpSlice }, spriteDisplayTime))


    fun playAnimation(
        times: Int = 1,
        spriteAnimation: SpriteAnimation? = currentAnimation,
        spriteDisplayTime: TimeSpan = getDefaultTime(spriteAnimation),
        startFrame: Int = -1,
        endFrame: Int = 0,
        reversed: Boolean = false
    ) = updateCurrentAnimation(
        spriteAnimation = spriteAnimation,
        spriteDisplayTime = spriteDisplayTime,
        animationCyclesRequested = times,
        startFrame = if (startFrame >= 0) startFrame else currentSpriteIndex,
        endFrame = endFrame,
        reversed = reversed,
        type = AnimationType.STANDARD
    )

    fun playAnimation(
        spriteAnimation: SpriteAnimation? = currentAnimation,
        spriteDisplayTime: TimeSpan = getDefaultTime(spriteAnimation),
        startFrame: Int = -1,
        endFrame: Int = 0,
        reversed: Boolean = false
    ) = updateCurrentAnimation(
        spriteAnimation = spriteAnimation,
        spriteDisplayTime = spriteDisplayTime,
        animationCyclesRequested = 1,
        startFrame = if (startFrame >= 0) startFrame else currentSpriteIndex,
        endFrame = endFrame,
        reversed = reversed,
        type = AnimationType.STANDARD
    )

    fun playAnimationForDuration(
        duration: TimeSpan,
        spriteAnimation: SpriteAnimation? = currentAnimation,
        spriteDisplayTime: TimeSpan = getDefaultTime(spriteAnimation),
        startFrame: Int = -1,
        reversed: Boolean = false
    ) = updateCurrentAnimation(
        spriteAnimation = spriteAnimation,
        spriteDisplayTime = spriteDisplayTime,
        duration = duration,
        startFrame = if (startFrame >= 0) startFrame else currentSpriteIndex,
        reversed = reversed,
        type = AnimationType.DURATION
    )

    fun playAnimationLooped(
        spriteAnimation: SpriteAnimation? = currentAnimation,
        spriteDisplayTime: TimeSpan = getDefaultTime(spriteAnimation),
        startFrame: Int = -1,
        reversed: Boolean = false
    ) = updateCurrentAnimation(
        spriteAnimation = spriteAnimation,
        spriteDisplayTime = spriteDisplayTime,
        startFrame = if (startFrame >= 0) startFrame else currentSpriteIndex,
        looped = true,
        reversed = reversed,
        type = AnimationType.LOOPED
    )

    fun stopAnimation() {
        animationRequested = false
        triggerEvent(_onAnimationStopped)
        if (overlapPlaying) {
            overlapPlaying = false
            if (lastLooped) {
                playAnimationLooped(lastAnimation, reversed = lastReversed)
            } else {
                playAnimation(lastAnimation, reversed = lastReversed)
            }
            lastAnimation = null
            lastReversed = false
            lastLooped = false
        }
    }

    private fun nextSprite(frameTime: TimeSpan) {
        lastAnimationFrameTime += frameTime
        if (lastAnimationFrameTime + frameTime >= this.spriteDisplayTime) {
            when (animationType) {
                AnimationType.STANDARD -> {
                    if (animationNumberOfFramesRequested > 0) {
                        animationNumberOfFramesRequested--
                    }
                }
                AnimationType.DURATION -> {
                    animationRemainingDuration -= lastAnimationFrameTime
                }
                AnimationType.LOOPED -> {

                }
            }
            if (reversed) --currentSpriteIndex else ++currentSpriteIndex
            totalFramesPlayed++
            triggerEvent(_onFrameChanged)
            lastAnimationFrameTime = 0.milliseconds
        }
    }

    val totalFrames: Int
        get() {
            val ca = currentAnimation ?: return 1
            return ca.size
        }

    private fun updateCurrentAnimation(
        spriteAnimation: SpriteAnimation?,
        spriteDisplayTime: TimeSpan = getDefaultTime(spriteAnimation),
        animationCyclesRequested: Int = 1,
        duration: TimeSpan = 0.milliseconds,
        startFrame: Int = 0,
        endFrame: Int = 0,
        looped: Boolean = false,
        reversed: Boolean = false,
        type: AnimationType = AnimationType.STANDARD
    ) {
        triggerEvent(_onAnimationStarted)
        this.spriteDisplayTime = spriteDisplayTime
        currentAnimation = spriteAnimation
        animationLooped = looped
        animationRemainingDuration = duration
        currentSpriteIndex = startFrame
        this.reversed = reversed
        animationType = type
        animationRequested = true
        val endFrame = endFrame umod totalFrames
        currentAnimation?.let {
            val count = when {
                startFrame > endFrame -> (if (reversed) startFrame - endFrame else it.spriteStackSize - (startFrame - endFrame))
                endFrame > startFrame -> (if (reversed) (startFrame - endFrame) umod it.spriteStackSize else endFrame - startFrame)
                else -> 0
            }
            val requestedFrames = count + (animationCyclesRequested * it.spriteStackSize)
            this.animationNumberOfFramesRequested = requestedFrames
        }
    }

    fun setFrame(index: Int) {
        currentSpriteIndex = index
    }

    private fun triggerEvent(signal: Signal<SpriteAnimation>?) {
        if (signal != null) currentAnimation?.let { signal.invoke(it) }
    }

    inner class AnimationManager {
        private val states = arrayListOf<AnimationState>()

        /**
         * Priority is represented by the deepest. The deepest has top priority while the shallowest has lowest.
         */
        fun registerState(anim: SpriteAnimation, loop: Boolean = true, reason: () -> Boolean) {
            states.add(AnimationState(anim, loop, reason))
        }

        fun removeState(anim: SpriteAnimation) {
            states.find { it.anim == anim }?.also { states.remove(it) }
        }

        fun removeAllStates() {
            states.clear()
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