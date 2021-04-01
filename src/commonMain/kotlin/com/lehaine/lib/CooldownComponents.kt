package com.lehaine.lib

import com.soywiz.kds.Pool
import com.soywiz.kds.iterators.fastForEach
import com.soywiz.klock.TimeSpan
import com.soywiz.klock.milliseconds
import com.soywiz.korge.component.UpdateComponent
import com.soywiz.korge.view.View
import com.soywiz.korio.lang.Closeable

private typealias CooldownCallback = (TimeSpan) -> Unit

private data class CooldownTimer(
    var time: TimeSpan,
    var name: String,
    var callback: () -> Unit,
    var timerFinished: (String) -> Unit
) {
    val ratio get() = 1 - elapsed / time
    var elapsed = 0.milliseconds

    fun update(dt: TimeSpan) {
        elapsed += dt
        while (elapsed >= time) {
            elapsed = 0.milliseconds
            callback()
            timerFinished(name)
            break
        }
    }
}

class CooldownComponents(override val view: View) : UpdateComponent {
    private val cooldownTimerPool = Pool(
        reset = {
            it.elapsed = 0.milliseconds
            it.time = 0.milliseconds
            it.name = ""
            it.callback = {}
            it.timerFinished = {}
        },
        gen = { CooldownTimer(0.milliseconds, "", {}, {}) })

    private val timers = arrayListOf<CooldownTimer>()
    private val nameCheck = mutableMapOf<String, Boolean>()

    override fun update(dt: TimeSpan) {
        timers.fastForEach {
            it.update(dt)
        }
    }

    private fun addTimer(name: String, timer: CooldownTimer) {
        if (nameCheck[name] != true) {
            timers.add(timer)
            nameCheck[name] = true
        }
    }

    private fun removeTimer(name: String) {
        if (nameCheck[name] == true) {
            timers.find { it.name == name }?.also {
                timers.remove(it)
                cooldownTimerPool.free(it)
            }
            nameCheck.remove(name)
        }
    }

    private fun interval(name: String, time: TimeSpan, callback: () -> Unit = {}): Closeable {
        val timer = cooldownTimerPool.alloc().apply {
            this.time = time
            this.name = name
            this.callback = callback
            this.timerFinished = ::removeTimer
        }
        addTimer(name, timer)
        return Closeable { removeTimer(name) }
    }

    fun timeout(name: String, time: TimeSpan, callback: () -> Unit = { }): Closeable =
        interval(name, time, callback)

    fun has(name: String) = nameCheck[name] ?: false

    fun ratio(name: String): Double {
        if (!has(name)) return 0.0
        return timers.find { it.name == name }?.ratio ?: 0.0
    }
}

val View.cooldown get() = this.getOrCreateComponentUpdate { CooldownComponents(this) }
val View.cd get() = this.cooldown

fun View.cooldown(name: String, time: TimeSpan, callback: () -> Unit = {}): Closeable =
    this.cooldown.timeout(name, time, callback)

fun View.cd(name: String, time: TimeSpan, callback: () -> Unit = {}): Closeable =
    cooldown(name, time, callback)