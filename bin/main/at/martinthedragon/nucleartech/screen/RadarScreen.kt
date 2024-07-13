package at.martinthedragon.nucleartech.screen

import at.martinthedragon.nucleartech.menu.RadarMenu
import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import net.minecraft.network.chat.Component
import net.minecraft.world.entity.player.Inventory

class RadarScreen(menu: RadarMenu, playerInventory: Inventory, title: Component): AbstractContainerScreen<RadarMenu>(menu, playerInventory, title) {
    override fun renderBg(stack: PoseStack, partialTicks: Float, mouseX: Int, mouseY: Int) {
        renderBackground(stack)
    }
}
