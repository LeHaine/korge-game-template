package com.lehaine.pixelheist

import com.lehaine.lib.CameraContainer

fun CameraContainer.follow(entity: Entity, setImmediately: Boolean = false) {
    follow(entity.container, setImmediately)
}