package me.odinmain.features.settings.impl

import com.github.stivais.ui.animation.Animations
import com.github.stivais.ui.color.Color
import com.github.stivais.ui.color.toHSB
import com.github.stivais.ui.constraints.*
import com.github.stivais.ui.constraints.measurements.Animatable
import com.github.stivais.ui.constraints.sizes.Copying
import com.github.stivais.ui.elements.*
import com.github.stivais.ui.events.onClick
import com.github.stivais.ui.renderer.GradientDirection
import com.github.stivais.ui.utils.color
import com.github.stivais.ui.utils.radii
import com.github.stivais.ui.utils.seconds
import me.odinmain.features.settings.Setting

class NewColorSetting(
    name: String,
    color: Color.HSB,
    val allowAlpha: Boolean = true,
    description: String = "",
    hidden: Boolean = false
) : Setting<Color.HSB>(name, hidden, description) {

    constructor(name: String, color: Color, description: String = "", hidden: Boolean = false) : this(
        name = name,
        color = color.toHSB(),
        allowAlpha = true,
        description = description,
        hidden = hidden
    )

    override val default: Color.HSB = color

    override var value: Color.HSB = default

    // this is quite messy, however it would've been messier getting it to work with some animation
    override fun getElement(parent: Element): SettingElement {
        val size = Animatable(from = 40.px, to = if (allowAlpha) 285.px else 255.px)
        return parent.setting(size) {
            column(constraints = copies()) {
                group(
                    constraints = constrain(
                        w = Copying,
                        h = 40.px
                    )
                ) {
                    text(
                        text = name,
                        pos = at(x = 6.px),
                        size = 40.percent
                    )
                    button(
                        constraints = constrain(x = -(6.px), w = 30.px, h = 50.percent),
                        color = color(from = value, to = value),
                        radii = radii(all = 5)
                    ).onClick {
                        size.animate(0.25.seconds, Animations.EaseInOutQuint)
                        true
                    }
                }

                block(
                    constraints = size(w = 95.percent, h = 170.px),
                    color1 = Color.WHITE,
                    color2 = value,
                    direction = GradientDirection.LeftToRight
                ) {
                    block(
                        constraints = copies(),
                        color1 = Color.TRANSPARENT,
                        color2 = Color.BLACK,
                        direction = GradientDirection.TopToBottom
                    )
                }
            }
        }
    }
}