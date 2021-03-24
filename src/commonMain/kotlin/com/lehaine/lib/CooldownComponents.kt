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
            timerFinished(name)
            elapsed = 0.milliseconds
            callback()
            break
        }
    }
}

class CooldownComponents(override val view: View) : UpdateComponent {
    // TODO impl pool
    private val _cooldownTimerPool = Pool { CooldownTimer(0.milliseconds, "", {}, {}) }

    private val _timers = arrayListOf<CooldownTimer>()
    private val _nameCheck = mutableMapOf<String, Boolean>()

    override fun update(dt: TimeSpan) {
        _timers.fastForEach {
            it.update(dt)
        }
    }

    private fun addTimer(name: String, timer: CooldownTimer) {
        if (_nameCheck[name] != true) {
            _timers.add(timer)
            _nameCheck[name] = true
        }
    }

    private fun removeTimer(name: String) {
        if (_nameCheck[name] == true) {
            _timers.find { it.name == name }?.also { _timers.remove(it) }
            _nameCheck.remove(name)
        }
    }

    private fun interval(name: String, time: TimeSpan, callback: () -> Unit = {}): Closeable {
        val timer = CooldownTimer(time, name, callback) { removeTimer(it) }
        addTimer(name, timer)
        return Closeable { removeTimer(name) }
    }

    fun timeout(name: String, time: TimeSpan, callback: () -> Unit = { }): Closeable =
        interval(name, time, callback)

    fun has(name: String) = _nameCheck[name] ?: false

    fun ratio(name: String): Double {
        if (!has(name)) return 0.0
        return _timers.find { it.name == name }?.ratio ?: 0.0
    }
}

val View.cooldown get() = this.getOrCreateComponentUpdate { CooldownComponents(this) }
val View.cd get() = this.cooldown

fun View.cooldown(name: String, time: TimeSpan, callback: () -> Unit = {}): Closeable =
    this.cooldown.timeout(name, time, callback)

fun View.cd(name: String, time: TimeSpan, callback: () -> Unit = {}): Closeable =
    cooldown(name, time, callback)