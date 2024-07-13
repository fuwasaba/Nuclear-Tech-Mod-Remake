package at.martinthedragon.nucleartech.block.entity.renderer.reactor

import at.martinthedragon.nucleartech.block.entity.reactors.ReactorMk2BlockEntity
import at.martinthedragon.nucleartech.block.entity.renderer.RotatedBlockEntityRenderer
import at.martinthedragon.nucleartech.item.RBMKRodItem
import at.martinthedragon.nucleartech.rendering.SpecialModels
import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.renderer.RenderType
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider
import net.minecraftforge.client.ForgeHooksClient
import net.minecraftforge.client.model.renderable.MultipartTransforms
import net.minecraftforge.client.model.renderable.SimpleRenderable
import net.minecraftforge.fluids.FluidStack

class ReactorMk2Renderer(val context: BlockEntityRendererProvider.Context): RotatedBlockEntityRenderer<ReactorMk2BlockEntity>(context) {
    override fun renderRotated(blockEntity: ReactorMk2BlockEntity, model: SimpleRenderable, partials: Float, matrix: PoseStack, buffers: MultiBufferSource, light: Int, overlay: Int) {
        matrix.pushPose()
        matrix.translate(.5, .0, -.5)
        model.render(matrix, buffers, renderType, light, overlay, partials, MultipartTransforms.EMPTY)
        if (blockEntity.getItem(0).item is RBMKRodItem) {
            SpecialModels.REACTOR_MK2_ROD.get().render(matrix, buffers, renderType, light, overlay, partials, MultipartTransforms.EMPTY)
        }
        val steamTank = blockEntity.steamTank.fluid
        val waterTank = blockEntity.waterTank.fluid
        if (!steamTank.isEmpty) {
            matrix.pushPose()
            matrix.translate(-.625, .0, .625)
            val renderBuffer = ForgeHooksClient.getBlockMaterial(getTextureForFluid(blockEntity, steamTank)).buffer(buffers, RenderType::entityTranslucent)
            val color = steamTank.fluid.attributes.color
            val red = (color shr 16 and 0xFF) / 255F
            val green = (color shr 8 and 0xFF) / 255F
            val blue = (color and 0xFF) / 255F
            val alpha = (color shr 24 and 0xFF) / 255F
            val count = blockEntity.steamTank.fluidAmount * 16 / blockEntity.steamTank.capacity

            // @TODO Render Fluids to tank
            // 残量とキャパ割ったやつ(count)の回数分それっぽい高さのモデルを表示する的な

            matrix.popPose()
        }
        if (!waterTank.isEmpty) {
            matrix.pushPose()
            matrix.translate(-.625, .0, .625)
            val renderBuffer = ForgeHooksClient.getBlockMaterial(getTextureForFluid(blockEntity, waterTank)).buffer(buffers, RenderType::entityTranslucent)
            val color = steamTank.fluid.attributes.color
            val red = (color shr 16 and 0xFF) / 255F
            val green = (color shr 8 and 0xFF) / 255F
            val blue = (color and 0xFF) / 255F
            val alpha = (color shr 24 and 0xFF) / 255F
            val count = blockEntity.waterTank.fluidAmount * 16 / blockEntity.waterTank.capacity

            // @TODO Render Fluids to tank

            matrix.popPose()
        }
        matrix.popPose()
    }
    private fun getTextureForFluid(react: ReactorMk2BlockEntity, fluid: FluidStack) = fluid.fluid.attributes.getFlowingTexture(fluid)
    override fun getModel(blockEntity: ReactorMk2BlockEntity)= SpecialModels.REACTOR_MK2.get()
}
