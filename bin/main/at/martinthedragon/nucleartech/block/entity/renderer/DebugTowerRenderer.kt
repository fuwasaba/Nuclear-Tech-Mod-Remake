package at.martinthedragon.nucleartech.block.entity.renderer

import at.martinthedragon.nucleartech.block.entity.DebugTowerBlockEntity
import at.martinthedragon.nucleartech.rendering.SpecialModels
import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.renderer.RenderType
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider
import net.minecraftforge.client.model.renderable.MultipartTransforms
import net.minecraftforge.client.model.renderable.SimpleRenderable

class DebugTowerRenderer(context: BlockEntityRendererProvider.Context): RotatedBlockEntityRenderer<DebugTowerBlockEntity>(context) {
    override fun renderRotated(blockEntity: DebugTowerBlockEntity, model: SimpleRenderable, partials: Float, matrix: PoseStack, buffers: MultiBufferSource, light: Int, overlay: Int) {
        matrix.translate(.0, .0, -11.0)
        getModel(blockEntity).render(matrix, buffers, RenderType::entityCutoutNoCull, light, overlay, partials, MultipartTransforms.EMPTY)
    }
    override fun getModel(blockEntity: DebugTowerBlockEntity) = SpecialModels.YASHIRO.get()
}
