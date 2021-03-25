package com.lehaine.pixelheist

import com.soywiz.korge.view.Container
import com.soywiz.korge.view.ViewDslMarker
import com.soywiz.korge.view.addTo

inline fun Container.entityContainer(callback: @ViewDslMarker EntityContainer.() -> Unit = {}) =
    EntityContainer().addTo(this, callback)

class EntityContainer : Container() {
    val entities = arrayListOf<Entity>()
}

fun <T : Entity> T.addEntityTo(parent: EntityContainer): T {
    parent += this
    parent.entities.add(this)
    return this
}

inline fun <T : Entity> T.addEntityTo(instance: EntityContainer, callback: @ViewDslMarker T.() -> Unit = {}) =
    this.addTo(instance).apply(callback)
