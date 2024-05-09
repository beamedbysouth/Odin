package com.github.stivais.ui.constraints.operational

import com.github.stivais.ui.constraints.Constraint
import com.github.stivais.ui.constraints.Measurement
import com.github.stivais.ui.constraints.Type
import com.github.stivais.ui.elements.Element

class CoerceMax(val amount: Float, val constraint: Constraint) : Measurement {
    override fun get(element: Element, type: Type): Float {
        return constraint.get(element, type).coerceAtMost(amount)
    }

    override fun reliesOnChild(): Boolean {
        return constraint.reliesOnChild()
    }
}