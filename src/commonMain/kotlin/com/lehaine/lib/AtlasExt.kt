package com.lehaine.lib

import com.soywiz.korim.atlas.Atlas

fun Atlas.getByPrefix(prefix: String = "") = this.entries.first { it.filename.startsWith(prefix) }.slice