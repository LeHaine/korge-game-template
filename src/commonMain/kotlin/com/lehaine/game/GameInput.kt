package com.lehaine.game

sealed class GameInput {
    object Horizontal : GameInput()
    object Vertical : GameInput()
}
