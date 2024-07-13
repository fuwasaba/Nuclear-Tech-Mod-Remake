package at.martinthedragon.nucleartech.block.entity.renderer

import at.martinthedragon.nucleartech.block.entity.RadarBlockEntity
import at.martinthedragon.nucleartech.rendering.SpecialModels
import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.math.Vector3f
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.renderer.RenderType
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider
import net.minecraftforge.client.model.renderable.MultipartTransforms

class RadarRenderer(val context: BlockEntityRendererProvider.Context): BlockEntityRenderer<RadarBlockEntity> {
    override fun render(blockEntity: RadarBlockEntity, partials: Float, matrix: PoseStack, buffers: MultiBufferSource, light: Int, overlay: Int) {
        val dish = SpecialModels.RADAR_DISH.get()
        val base = SpecialModels.RADAR_BASE.get()
        matrix.pushPose()
        matrix.translate(.5, .0, .5)
        if (blockEntity.energyClient > 0)
            matrix.mulPose(Vector3f.YP.rotationDegrees(((-System.currentTimeMillis() / 10) % 360).toFloat()))
        dish.render(matrix, buffers, RenderType::entityCutoutNoCull, light, overlay, partials, MultipartTransforms.EMPTY)
        matrix.popPose()
        matrix.pushPose()
        matrix.mulPose(Vector3f.YP.rotationDegrees(180f))
        matrix.translate(-.5, -.0, -.5)
        base.render(matrix, buffers, RenderType::entityCutout, light, overlay, partials, MultipartTransforms.EMPTY)
        matrix.popPose()
    }
}
