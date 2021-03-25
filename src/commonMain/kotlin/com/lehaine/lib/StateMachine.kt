package com.lehaine.lib

import com.soywiz.kds.iterators.fastForEach
import com.soywiz.klock.TimeSpan

@DslMarker
annotation class StateMachineDsl

data class State<STATE : Any>(
    val type: STATE,
    val reason: () -> Boolean,
    val update: (dt: TimeSpan) -> Unit,
    val begin: () -> Unit,
    val end: () -> Unit
)

class StateMachine<STATE : Any>(private val states: ArrayList<State<out STATE>>) {

    var onStateChanged: ((STATE) -> Unit)? = null
    private var _currentState: State<out STATE>? = null
    val currentState get() = _currentState

    fun update(dt: TimeSpan) {
        states.fastForEach { state ->
            if (state.reason()) {
                if (_currentState != state) {
                    _currentState?.end?.invoke()
                    _currentState = state
                    _currentState?.begin?.invoke()
                    _currentState?.also { onStateChanged?.invoke(it.type) }
                }
                _currentState?.update?.invoke(dt)
                return
            }
        }
    }

    @StateMachineDsl
    class StateMachineBuilder<STATE : Any> {
        private val states = arrayListOf<State<out STATE>>()
        private var onStateChanged: (STATE) -> Unit = {}
        fun <S : STATE> state(type: S, action: StateBuilder<S>.() -> Unit) {
            states.add(StateBuilder<S>().apply(action).build(type))
        }

        fun stateChanged(onStateChanged: (STATE) -> Unit) {
            this.onStateChanged = onStateChanged
        }

        fun build(): StateMachine<STATE> {
            return StateMachine(states).apply { this.onStateChanged = this@StateMachineBuilder.onStateChanged }
        }
    }
}

@StateMachineDsl
class StateBuilder<STATE : Any> {

    private var reason: () -> Boolean = { false }
    private var update: (dt: TimeSpan) -> Unit = {}
    private var begin: () -> Unit = {}
    private var end: () -> Unit = {}
    fun reason(reason: () -> Boolean) {
        this.reason = reason
    }

    fun update(update: (dt: TimeSpan) -> Unit) {
        this.update = update
    }

    fun begin(begin: () -> Unit) {
        this.begin = begin
    }

    fun end(end: () -> Unit) {
        this.end = end
    }

    fun build(type: STATE): State<STATE> {
        return State(type, reason, update, begin, end)
    }
}

fun <STATE : Any> stateMachine(action: StateMachine.StateMachineBuilder<STATE>.() -> Unit): StateMachine<STATE> {
    return StateMachine.StateMachineBuilder<STATE>().apply(action).build()
}