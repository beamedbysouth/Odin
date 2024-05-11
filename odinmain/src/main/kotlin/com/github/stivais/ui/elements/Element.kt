package com.github.stivais.ui.elements

import com.github.stivais.ui.UI
import com.github.stivais.ui.UI.Companion.logger
import com.github.stivais.ui.color.Color
import com.github.stivais.ui.constraints.Constraint
import com.github.stivais.ui.constraints.Constraints
import com.github.stivais.ui.constraints.Type
import com.github.stivais.ui.constraints.measurements.Undefined
import com.github.stivais.ui.constraints.positions.Center
import com.github.stivais.ui.events.Event
import com.github.stivais.ui.events.Mouse
import com.github.stivais.ui.utils.forLoop

abstract class Element(constraints: Constraints?, var color: Color? = null) {

    // todo: maybe bring all values into here?
    val constraints: Constraints = constraints ?: Constraints(Undefined, Undefined, Undefined, Undefined)

    lateinit var ui: UI

    val renderer get() = ui.renderer

    var parent: Element? = null

    var elements: ArrayList<Element>? = null

    open var events: HashMap<Event, ArrayList<Event.() -> Boolean>>? = null

    private var initializationTasks: ArrayList<() -> Unit>? = null

    var x: Float = 0f
    var y: Float = 0f
    var width: Float = 0f
    var height: Float = 0f

    var internalX: Float = 0f

    var internalY: Float = 0f

    var isHovered = false
        set(value) {
            if (value) {
                accept(Mouse.Entered)
            } else {
                accept(Mouse.Exited)
            }
            field = value
        }

    var enabled: Boolean = true

    private var scissors: Boolean = false

    var renders: Boolean = true
        get() {
            return enabled && field
        }

    abstract fun draw()

    fun position() {
        if (!enabled) return
        prePosition()
        if (!constraints.width.reliesOnChild()) width = constraints.width.get(this, Type.W)
        if (!constraints.height.reliesOnChild()) height = constraints.height.get(this, Type.H)
        internalX = constraints.x.get(this, Type.X)
        internalY = constraints.y.get(this, Type.Y)

        if (elements != null) {
            elements!!.forLoop { element ->
                element.position()
            }
        } else {
            parent?.place(this)
        }

        if (constraints.width.reliesOnChild()) width = constraints.width.get(this, Type.W)
        if (constraints.height.reliesOnChild()) height = constraints.height.get(this, Type.H)
        placed = false
    }

    fun clip() {
        elements?.forLoop {
            it.renders = it.intersects(x, y, width, height)
            it.clip()
        }
    }

    open fun prePosition() {}

    fun redraw() {
        ui.needsRedraw = true
    }

    private var placed: Boolean = false

    open fun place(element: Element) {
        if (!placed) {
            parent?.place(this)
            placed = true
        }
        element.x = x + element.internalX
        element.y = y + element.internalY
    }

    fun render() {
        if (!renders) return
        draw()
        // check if size is valid
        if (width != 0f && height != 0f) {
            if (scissors) renderer.pushScissor(x, y, width, height)
            elements?.forLoop { element ->
                element.render()
            }
            if (scissors) renderer.popScissor()
        }
    }

    var last = color?.rgba ?: 0

    open fun accept(event: Event): Boolean {
        if (events != null) {
            events?.get(event)?.let { actions -> actions.forLoop { if (it(event)) return true } }
        }
        return false
    }

    fun registerEvent(event: Event, block: Event.() -> Boolean) {
        if (events == null) events = HashMap()
        events!!.getOrPut(event) { arrayListOf() }.add(block)

    }

    fun onInitialization(action: () -> Unit) {
        if (::ui.isInitialized) return logger.warning("Tried calling \"onInitialization\" after init has already been done")
        if (initializationTasks == null) initializationTasks = arrayListOf()
        initializationTasks!!.add(action)
    }

    fun addElement(element: Element) {
        if (elements == null) elements = arrayListOf()
        elements!!.add(element)
        element.parent = this
        element.initialize(ui)
        onElementAdded(element)
        if (ui.settings.positionOnAdd) element.position()
    }

    fun initialize(ui: UI) {
        this.ui = ui
        if (initializationTasks != null) {
            initializationTasks!!.forLoop { it() }
            initializationTasks!!.clear()
            initializationTasks = null
        }
    }

    // sets up position if element being added has an undefined position
    open fun onElementAdded(element: Element) {
        val c = element.constraints
        if (c.x is Undefined) c.x = Center
        if (c.y is Undefined) c.y = Center
    }

    fun isInside(x: Float, y: Float): Boolean {
        val tx = this.x
        val ty = this.y
        return x in tx..tx + width && y in ty..ty + height
    }

    fun intersects(x: Float, y: Float, width: Float, height: Float): Boolean {
        val tx = this.x
        val ty = this.y
        val tw = this.width
        val th = this.height
        return (x < tx + tw && tx < x + width) && (y < ty + th && ty < y + height)
    }

    fun focused(): Boolean = ui.eventManager?.focused == this

    // todo: dsl, maybe move out of this class?
    fun toggle(value: Boolean = !enabled) {
        enabled = value
    }

    fun x() = constraints.x

    fun y() = constraints.y

    // todo: dsl, maybe move out of this class?
    fun width(): Constraint {
        return constraints.width
    }

    // todo: dsl, maybe move out of this class?
    fun height(): Constraint {
        return constraints.height
    }

    fun scissors() {
        scissors = true
    }

    // todo: dsl, maybe move out of this class?
    fun sibling(distance: Int = 1): Element? {
        if (parent != null) {
            val currIndex = parent!!.elements!!.indexOf(this)
            return parent!!.elements!!.getOrNull(currIndex + distance)
        }
        return null
    }

    fun afterInitialization(block: () -> Unit) {
        if (::ui.isInitialized) {
            if (ui.afterInit == null) ui.afterInit = arrayListOf()
            ui.afterInit!!.add(block)
        } else {
            onInitialization {
                if (ui.afterInit == null) ui.afterInit = arrayListOf()
                ui.afterInit!!.add(block)
            }
        }
    }

    fun takeEvents(from: Element) {
        if (from.events == null) return logger.warning("Tried to take event from an element that doesn't have events")
        if (events != null) {
            events!!.putAll(from.events!!)
        } else {
            events = from.events
        }
        from.events = null
    }

    operator fun invoke(action: Element.() -> Unit) = action()
}