package com.github.stivais.ui

import com.github.stivais.ui.color.Color
import com.github.stivais.ui.constraints.Constraints
import com.github.stivais.ui.constraints.px
import com.github.stivais.ui.elements.Element
import com.github.stivais.ui.elements.impl.Group
import com.github.stivais.ui.events.EventManager
import com.github.stivais.ui.renderer.CookedRenderer
import com.github.stivais.ui.renderer.Renderer
import com.github.stivais.ui.utils.forLoop
import java.util.logging.Logger

class UI(
    val renderer: Renderer = CookedRenderer,
    settings: UISettings? = null
) {

    val settings: UISettings = settings ?: UISettings()

    val main: Group = Group(Constraints(0.px, 0.px, 1920.px, 1080.px)).also {
        it.initialize(this)
        it.position()
    }

    constructor(block: Group.() -> Unit) : this() {
        main.block()
    }

    var eventManager: EventManager? = EventManager(this)

    val mx get() = eventManager!!.mouseX

    val my get() = eventManager!!.mouseY

    var afterInit: ArrayList<() -> Unit>? = null

    fun initialize() {
        main.position()
        afterInit?.forLoop { it() }
        afterInit = null
    }

    // frametime metrics
    private var frames: Int = 0
    private var frameTime: Long = 0
    private var performance: String = ""

    fun render() {
        val start = System.nanoTime()

        renderer.beginFrame()
        main.position()
        main.render()
        if (settings.frameMetrics) {
            renderer.text(performance, main.width, main.height, 12f, Color.WHITE.rgba)
        }
        renderer.endFrame()

        if (settings.frameMetrics) {
            frames++
            frameTime += System.nanoTime() - start
            if (frames > 100) {
                performance =
                    "elements: ${getStats(main, false)}, " +
                    "elements rendering: ${getStats(main, true)}," +
                    "frametime avg: ${(frameTime / frames) / 1_000_000.0}ms"
                frames = 0
                frameTime = 0
            }
        }
        frames++
        frameTime += System.nanoTime() - start
    }

    private fun getStats(element: Element, onlyRender: Boolean): Int {
        var amount = 0
        if (!(onlyRender && !element.renders)) {
            amount++
            element.elements?.forLoop { amount += getStats(it, onlyRender) }
        }
        return amount
    }

    fun resize(width: Int, height: Int) {
        main.constraints.width = width.px
        main.constraints.height = height.px
    }

    fun focus(element: Element) {
        if (eventManager == null) return logger.warning("Event Manager isn't setup, but called focus")
        eventManager!!.focus(element)
    }

    fun unfocus() {
        if (eventManager == null) return logger.warning("Event Manager isn't setup, but called unfocus")
        eventManager!!.unfocus()
    }

    inline fun settings(block: UISettings.() -> Unit): UI {
        settings.apply(block)
        return this
    }

    companion object {
        val logger: Logger = Logger.getLogger("UI")
    }
}