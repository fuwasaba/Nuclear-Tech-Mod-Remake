package at.martinthedragon.nucleartech.screen.reactor

import at.martinthedragon.nucleartech.NuclearTech
import at.martinthedragon.nucleartech.api.block.entities.createSidedTickerChecked
import at.martinthedragon.nucleartech.block.entity.BlockEntityTypes
import at.martinthedragon.nucleartech.extensions.tooltipFluidTank
import at.martinthedragon.nucleartech.menu.RadarMenu
import at.martinthedragon.nucleartech.menu.reactor.ReactorMk0Menu
import at.martinthedragon.nucleartech.networking.Mk0FluxUpdatedMessage
import at.martinthedragon.nucleartech.networking.NuclearPacketHandler
import at.martinthedragon.nucleartech.ntm
import at.martinthedragon.nucleartech.rendering.renderGuiFluidTank
import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.Minecraft
import net.minecraft.client.Options
import net.minecraft.client.ProgressOption
import net.minecraft.client.gui.components.SliderButton
import net.minecraft.client.gui.components.Widget
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import net.minecraft.client.renderer.GameRenderer
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.TextComponent
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.entity.BlockEntityType
import net.minecraft.world.level.block.state.BlockState
import kotlin.math.roundToInt

class ReactorMk0Screen(menu: ReactorMk0Menu, playerInventory: Inventory, title: Component): AbstractContainerScreen<ReactorMk0Menu>(menu, playerInventory, title) {
    private val texture = ntm("textures/gui/reactor_mk0.png")
    private val progressOption = ProgressOption("Flux", 0.0, 100.0, 0.1f, {
        menu.blockEntity.flux
    }, {
        _: Options, value: Double -> run {
            NuclearPacketHandler.INSTANCE.sendToServer(Mk0FluxUpdatedMessage(value, menu.blockEntity.blockPos))
            menu.blockEntity.flux = value
        }
    }, {
        _: Options, _: ProgressOption -> TextComponent("Flux amount: " + (menu.blockEntity.flux * 10.0).roundToInt().toDouble() / 10.0)
    })
    init {
        imageWidth = 176
        imageHeight = 222
        inventoryLabelY = imageHeight - 94
    }
    override fun render(stack: PoseStack, mouseX: Int, mouseY: Int, partialTicks: Float) {
        renderBackground(stack)
        super.render(stack, mouseX, mouseY, partialTicks)
        renderTooltip(stack, mouseX, mouseY)
    }

    override fun init() {
        super.init()
        if (minecraft == null) return
        addRenderableWidget(progressOption.createButton(minecraft!!.options, guiLeft + 70, guiTop + 114, 100))
    }
    override fun renderBg(stack: PoseStack, ticks: Float, mouseX: Int, mouseY: Int) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader)
        RenderSystem.setShaderColor(1F, 1F, 1F, 1F)
        RenderSystem.setShaderTexture(0, texture)
        blit(stack, guiLeft, guiTop, 0, 0, xSize, ySize)
        val reactor = menu.blockEntity
        Minecraft.getInstance().font.draw(stack, "Heat: " + (menu.blockEntity.coreHeat * 10).roundToInt() / 10, guiLeft.toFloat() + 8, guiTop.toFloat() + 16, 0x555555)
        renderGuiFluidTank(stack, guiLeft + 8, guiTop + 126, 16, 67, blitOffset, reactor.waterTank)
        renderGuiFluidTank(stack, guiLeft + 26, guiTop + 126, 16, 67, blitOffset, reactor.steamTank)
    }

    override fun renderTooltip(stack: PoseStack, mouseX: Int, mouseY: Int) {
        super.renderTooltip(stack, mouseX, mouseY)
        tooltipFluidTank(stack, menu.blockEntity.waterTank, 7, 59, 17, 69, mouseX, mouseY)
        tooltipFluidTank(stack, menu.blockEntity.steamTank, 25, 59, 17, 69, mouseX, mouseY)
    }
}
