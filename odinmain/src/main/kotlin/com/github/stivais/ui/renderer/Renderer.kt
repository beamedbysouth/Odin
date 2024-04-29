package com.github.stivais.ui.renderer

interface Renderer {

    fun beginFrame()

    fun endFrame()

    fun rect(x: Float, y: Float, w: Float, h: Float, color: Int)

    fun rect(x: Float, y: Float, w: Float, h: Float, color: Int, tl: Float, bl: Float, br: Float, tr: Float)

    fun rect(x: Float, y: Float, w: Float, h: Float, color: Int, radius: Float) {
        rect(x, y, w, h, color, radius, radius, radius, radius)
    }

    fun hollowRect(x: Float, y: Float, w: Float, h: Float, thickness: Float, color: Int, tl: Float, bl: Float, br: Float, tr: Float)

    fun hollowRect(x: Float, y: Float, w: Float, h: Float, thickness: Float, color: Int, radius: Float) {
        hollowRect(x, y, w, h, thickness, color, radius, radius, radius, radius)
    }

    fun text(text: String, x: Float, y: Float, size: Float, color: Int)
}