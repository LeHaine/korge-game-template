package com.lehaine.game

import com.lehaine.kiwi.CameraContainer

fun CameraContainer.follow(entity: Entity, setImmediately: Boolean = false) {
    follow(entity.container, setImmediately)
}