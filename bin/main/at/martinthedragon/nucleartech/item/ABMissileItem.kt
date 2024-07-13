package at.martinthedragon.nucleartech.item

import at.martinthedragon.nucleartech.entity.missile.AbstractMissile
import net.minecraft.client.renderer.item.ItemProperties
import net.minecraft.core.BlockPos
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.TooltipFlag
import net.minecraft.world.level.Level

class ABMissileItem<out M : AbstractMissile>(
    override val missileSupplier: (level: Level, startPos: BlockPos, targetPos: BlockPos) -> M,
    properties: Properties,
    private val missileTexture: ResourceLocation? = null,
    override val renderModel: ResourceLocation = AbstractMissile.missileModel("missile_v2"),
    override val renderScale: Float = 1F,
    override val hasTooltip: Boolean = false
) : MissileItem<M>(missileSupplier, properties, missileTexture, renderModel, renderScale, hasTooltip) {
    override val renderTexture: ResourceLocation get() = missileTexture ?: AbstractMissile.missileTexture(registryName!!.path)

    override fun appendHoverText(stack: ItemStack, level: Level?, tooltip: MutableList<Component>, flag: TooltipFlag) {
        if (hasTooltip) super.appendHoverText(stack, level, tooltip, flag)
    }
}
