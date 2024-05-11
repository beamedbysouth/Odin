package com.github.stivais.ui

import com.github.stivais.ui.constraints.Constraints
import com.github.stivais.ui.constraints.px
import com.github.stivais.ui.elements.Element
import com.github.stivais.ui.elements.impl.Group
import com.github.stivais.ui.events.EventManager
import com.github.stivais.ui.renderer.Framebuffer
import com.github.stivais.ui.renderer.NVGRenderer
import com.github.stivais.ui.renderer.Renderer
import com.github.stivais.ui.utils.forLoop
import org.lwjgl.opengl.Display
import java.util.logging.Logger

class UI(
    val renderer: Renderer = NVGRenderer,
    settings: UISettings? = null
) {

    val settings: UISettings = settings ?: UISettings()

    // temp
    val main: Group = Group(Constraints(0.px, 0.px, Display.getWidth().px, Display.getHeight().px)).also {
        it.initialize(this)
    }

    constructor(renderer: Renderer = NVGRenderer, block: Group.() -> Unit) : this(renderer) {
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
        if (settings.cacheFrames && renderer.supportsFramebuffers()) {
            framebuffer = renderer.createFramebuffer(main.width, main.height)
        }
    }

    // frametime metrics
    private var frames: Int = 0
    private var frameTime: Long = 0
    private var performance: String = ""

    var needsRedraw = true

    private var framebuffer: Framebuffer? = null

    fun render() {
        val start = System.nanoTime()
        val fbo = framebuffer
        if (fbo == null) {
            renderer.beginFrame(main.width, main.height)
            if (needsRedraw) {
                needsRedraw = false
                main.position()
                main.clip()
            }
            main.render()
            if (settings.frameMetrics) {
                renderer.text(performance, main.width - renderer.textWidth(performance, 12f), main.height - 12f, 12f)
            }
            renderer.endFrame()
        } else {
            if (needsRedraw) {
                needsRedraw = false
                renderer.bindFramebuffer(fbo) // thanks ilmars for helping me fix
                renderer.beginFrame(fbo.width, fbo.height)
                main.position()
                main.clip()
                main.render()
                renderer.endFrame()
                renderer.unbindFramebuffer()
            }
            renderer.beginFrame(fbo.width, fbo.height)
            renderer.drawFramebuffer(fbo, 0f, 0f)
            if (settings.frameMetrics) {
                renderer.text(performance, fbo.width - renderer.textWidth(performance, 12f), fbo.height - 12f, 12f,)
            }
            renderer.endFrame()
        }

        if (settings.frameMetrics) {
            frames++
            frameTime += System.nanoTime() - start
            if (frames > 100) {
                performance =
                    "total elements: ${getStats(main, false)}, " +
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
        needsRedraw = true
        if (framebuffer != null) {
            renderer.destroyFramebuffer(framebuffer!!)
            framebuffer = renderer.createFramebuffer(main.width, main.height)
        }
    }

    fun cleanup() {
        val fbo = framebuffer ?: return
        renderer.destroyFramebuffer(fbo)
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
        // temp name
        // future: maybe make a logging class, so you can get an element's "errors" and details
        val logger: Logger = Logger.getLogger("Odin/UI")
    }
}