package com.lehaine.lib.component

import com.soywiz.korui.UiContainer

interface Component {

    fun updateComponent(tmod: Double) {}

    fun createDebugInfo(container: UiContainer) {}
}