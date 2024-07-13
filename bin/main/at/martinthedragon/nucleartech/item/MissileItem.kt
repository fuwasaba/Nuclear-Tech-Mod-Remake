package at.martinthedragon.nucleartech.item

import at.martinthedragon.nucleartech.entity.missile.AbstractMissile
import net.minecraft.core.BlockPos
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.TooltipFlag
import net.minecraft.world.level.Level

open class MissileItem<out M : AbstractMissile>(
    open val missileSupplier: (level: Level, startPos: BlockPos, targetPos: BlockPos) -> M,
    properties: Properties,
    private val missileTexture: ResourceLocation? = null,
    open val renderModel: ResourceLocation = AbstractMissile.missileModel("missile_v2"),
    open val renderScale: Float = 1F,
    open val hasTooltip: Boolean = false
) : AutoTooltippedItem(properties.stacksTo(1)) {
    open val renderTexture: ResourceLocation get() = missileTexture ?: AbstractMissile.missileTexture(registryName!!.path)

    override fun appendHoverText(stack: ItemStack, level: Level?, tooltip: MutableList<Component>, flag: TooltipFlag) {
        if (hasTooltip) super.appendHoverText(stack, level, tooltip, flag)
    }
}
