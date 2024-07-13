package at.martinthedragon.nucleartech.block.entity.renderer.reactor

import at.martinthedragon.nucleartech.block.entity.reactors.ReactorMk0BlockEntity
import at.martinthedragon.nucleartech.block.entity.renderer.RotatedBlockEntityRenderer
import at.martinthedragon.nucleartech.rendering.SpecialModels
import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider
import net.minecraftforge.client.model.renderable.MultipartTransforms
import net.minecraftforge.client.model.renderable.SimpleRenderable

class ReactorMk0Renderer(val context: BlockEntityRendererProvider.Context): RotatedBlockEntityRenderer<ReactorMk0BlockEntity>(context) {
    override fun renderRotated(blockEntity: ReactorMk0BlockEntity, model: SimpleRenderable, partials: Float, matrix: PoseStack, buffers: MultiBufferSource, light: Int, overlay: Int) {
        matrix.pushPose()
        matrix.translate(.5, .0, -.5)
        model.render(matrix, buffers, renderType, light, overlay, partials, MultipartTransforms.EMPTY)
        matrix.popPose()
    }
    override fun getModel(blockEntity: ReactorMk0BlockEntity)= SpecialModels.REACTOR_MK0.get()
}
