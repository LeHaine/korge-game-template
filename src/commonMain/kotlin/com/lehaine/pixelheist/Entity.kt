package com.lehaine.pixelheist

import com.lehaine.lib.castRay
import com.lehaine.lib.enhancedSprite
import com.soywiz.kds.iterators.fastForEach
import com.soywiz.klock.TimeSpan
import com.soywiz.klock.milliseconds
import com.soywiz.kmem.clamp
import com.soywiz.korge.debug.uiCollapsibleSection
import com.soywiz.korge.debug.uiEditableValue
import com.soywiz.korge.view.*
import com.soywiz.korim.text.TextAlignment
import com.soywiz.korma.geom.vector.rect
import com.soywiz.korui.UiContainer
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.pow


open class Entity(
    cx: Int,
    cy: Int,
    val assets: Assets,
    val level: GameLevel,
    anchorX: Double = 0.5,
    anchorY: Double = 1.0
) : Container() {

    var enHeight = GRID_SIZE.toDouble()
    var enWidth = enHeight

    var cx = 0
    var cy = 0
    var xr = 0.5
    var yr = 1.0

    var dx = 0.0
    var dy = 0.0

    var frictX = 0.82
    var frictY = 0.82

    private var _squashX = 1.0
    private var _squashY = 1.0

    var squashX: Double
        get() = _squashX
        set(value) {
            _squashX = value
            _squashY = 2 - value
        }
    var squashY: Double
        get() = _squashY
        set(value) {
            _squashX = 2 - value
            _squashY = value
        }

    var spriteScaleX = 1.0
    var spriteScaleY = 1.0

    val onGround get() = dy == 0.0 && level.hasCollision(cx, cy + 1)
    var hasGravity = true
    var gravityMul = 1.0

    var dir = 1
        set(value) {
            field = value.clamp(-1, 1)
        }

    protected val gravityPulling get() = !onGround && hasGravity

    var tmod = 1.0
        private set

    val sprite = enhancedSprite {
        smoothing = false
        this.anchorX = anchorX
        this.anchorY = anchorY
    }

    val debugLabel = text("") {
        smoothing = false
        font = assets.pixelFont
        fontSize = 8.0
        alignment = TextAlignment.CENTER
        alignTopToBottomOf(sprite)
    }

    val input get() = stage!!.views.input

    private val canRayPass: (Int, Int) -> Boolean = { cx, cy ->
        !level.hasCollision(cx, cy) || this.cx == cx && this.cy == cy
    }

    private var initEntityCollisionChecks = false
    private var collisionFilter: () -> List<Entity> = { emptyList() }
        set(value) {
            field = value
            addEntityCollisionChecks()
        }

    init {
        toGridPosition(cx, cy)

        hitShape {
            rect(enWidth * 0.5, enHeight * 0.5, enWidth, enHeight)
        }

        addUpdater {
            if (stage == null) return@addUpdater
            update(it)
            postUpdate(it)
        }
    }

    /**** Helper Functions ****/
    fun anchor(ax: Double, ay: Double): Entity {
        sprite.anchor(ax, ay)
        return this
    }

    fun toGridPosition(cx: Int, cy: Int, xr: Double = 0.5, yr: Double = 1.0): Entity {
        this.cx = cx
        this.cy = cy
        this.xr = xr
        this.yr = yr
        return this
    }

    fun collisionFilter(filter: () -> List<Entity>): Entity {
        this.collisionFilter = filter
        return this
    }

    fun castRayTo(tcx: Int, tcy: Int): Boolean {
        return castRay(cx, cy, tcx, tcy, canRayPass)
    }

    fun castRayTo(entity: Entity): Boolean {
        return castRayTo(entity.cx, entity.cy)
    }

    /**** Lifecycle and other callbacks ****/
    protected open fun onEntityCollision(entity: Entity) {}

    protected open fun onEntityColliding(entity: Entity) {}

    protected open fun onEntityCollisionExit(entity: Entity) {}

    protected open fun update(dt: TimeSpan) {
        tmod = if (dt == 0.milliseconds) 0.0 else (dt / 16.666666.milliseconds)

        performXSteps()
        performYSteps()
    }

    protected open fun postUpdate(dt: TimeSpan) {
        x = (cx + xr) * GRID_SIZE
        y = (cy + yr) * GRID_SIZE
        sprite.scaleX = dir.toDouble() * spriteScaleX * squashX
        sprite.scaleY = spriteScaleY * squashY

        _squashX += (1 - _squashX) * 0.2
        _squashY += (1 - _squashY) * 0.2
    }

    private fun performXSteps() {
        var steps = ceil(abs(dx * tmod))
        val step = dx * tmod / steps
        while (steps > 0) {
            xr += step

            performXCollisionCheck()
            while (xr > 1) {
                xr--
                cx++
            }
            while (xr < 0) {
                xr++
                cx--
            }
            steps--
        }

        dx *= frictX.pow(tmod)
        if (abs(dx) <= 0.0005 * tmod) {
            dx = 0.0
        }
    }

    private fun performXCollisionCheck() {
        if (level.hasCollision(cx + 1, cy) && xr >= 0.7) {
            xr = 0.7
            dx *= 0.5.pow(tmod)
        }

        if (level.hasCollision(cx - 1, cy) && xr <= 0.3) {
            xr = 0.3
            dx *= 0.5.pow(tmod)
        }
    }

    private fun performYSteps() {
        if (gravityPulling) {
            dy += gravityMul * GRAVITY * tmod
        }
        var steps = ceil(abs(dy * tmod))
        val step = dy * tmod / steps
        while (steps > 0) {
            yr += step
            performYCollisionCheck()
            while (yr > 1) {
                yr--
                cy++
            }
            while (yr < 0) {
                yr++
                cy--
            }
            steps--
        }
        dy *= frictY.pow(tmod)
        if (abs(dy) <= 0.0005 * tmod) {
            dy = 0.0
        }
    }


    private fun performYCollisionCheck() {
        val heightCoordDiff = floor(enHeight / GRID_SIZE)
        if (level.hasCollision(cx, cy - 1) && yr <= heightCoordDiff) {
            yr = heightCoordDiff
            dy = 0.0
        }
        if (level.hasCollision(cx, cy + 1) && yr >= 1) {
            dy = 0.0
            yr = 1.0
        }
    }

    private fun addEntityCollisionChecks() {
        if (initEntityCollisionChecks) return

        val collisionState = mutableMapOf<View, Boolean>()
        addUpdater {
            collisionFilter().fastForEach {
                if (this != it) {
                    if (this.collidesWithShape(it)) {
                        if (collisionState[it] == true) {
                            onEntityColliding(it)
                        } else {
                            // we only need to call it once
                            onEntityCollision(it)
                            collisionState[it] = true
                        }
                    } else if (collisionState[it] == true) {
                        onEntityCollisionExit(it)
                        collisionState[it] = false
                    }
                }
            }
        }
    }

    override fun buildDebugComponent(views: Views, container: UiContainer) {
        container.uiCollapsibleSection("Entity") {
            uiEditableValue(listOf(this@Entity::cx, this@Entity::cy), name = "(cx, cy)", min = 0, max = 10000)
            uiEditableValue(listOf(this@Entity::xr, this@Entity::yr), name = "(xr, yr)", min = 0.0, max = 1.0)
            uiEditableValue(listOf(this@Entity::dx, this@Entity::dy), name = "Velocity (dx, dy)")
            uiEditableValue(listOf(this@Entity::_squashX, this@Entity::_squashY), name = "Squash (x, y)")
            uiEditableValue(this@Entity::hasGravity, name = "Gravity")
            uiEditableValue(this@Entity::gravityMul, name = "Gravity Multiplier")
            uiEditableValue(listOf(this@Entity::enWidth, this@Entity::enHeight), name = "Size (w, h)")
        }
        super.buildDebugComponent(views, container)
    }
}

