package me.odinmain.features.impl.floor7.p3.termsim


import me.odinmain.OdinMain.display
import me.odinmain.OdinMain.mc
import me.odinmain.events.impl.GuiLoadedEvent
import me.odinmain.features.impl.floor7.p3.TerminalSolver
import me.odinmain.utils.skyblock.ChatUtils.modMessage
import net.minecraft.client.gui.inventory.GuiChest
import net.minecraft.inventory.ContainerChest
import net.minecraft.inventory.InventoryBasic
import net.minecraft.inventory.Slot
import net.minecraft.item.Item
import net.minecraft.item.ItemStack

open class TermSimGui(val name: String, val size: Int) : GuiChest(
    mc.thePlayer.inventory,
    InventoryBasic(name, true, size)
) {
    val pane: Item = Item.getItemById(160)
    val blackPane = ItemStack(pane, 1, 15).apply { setStackDisplayName("") }
    private var startTime = 0L

    open fun create() {
        this.inventorySlots.inventorySlots.subList(0, size).forEach { it.putStack(blackPane) } // override
    }

    fun open() {
        create()
        display = this
        startTime = System.currentTimeMillis()
        TerminalSolver.onGuiLoad(GuiLoadedEvent(name, inventorySlots as ContainerChest))
    }

    fun solved() {
        modMessage("§aTerminal solved in §6${(System.currentTimeMillis() - startTime) / 1000.0}s §a!")
        StartGui.open()
    }

    open fun slotClick(slot: Slot, button: Int) {}

    final override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        val slot = slotUnderMouse ?: return
        if (slot.stack?.item == pane && slot.stack?.metadata == 15) return
        slotClick(slot, mouseButton)
        inventorySlots
    }

    final override fun handleMouseClick(slotIn: Slot?, slotId: Int, clickedButton: Int, clickType: Int) {
        val slot = slotIn ?: return
        if (slot.stack?.item == pane && slot.stack?.metadata == 15 || clickedButton != 4) return
        slotClick(slot, 0)
    }
}