package at.martinthedragon.nucleartech.item

import at.martinthedragon.nucleartech.LangKeys
import at.martinthedragon.nucleartech.SoundEvents
import at.martinthedragon.nucleartech.api.explosion.IgnitableExplosive
import at.martinthedragon.nucleartech.config.NuclearConfig
import at.martinthedragon.nucleartech.extensions.darkRed
import at.martinthedragon.nucleartech.extensions.gray
import at.martinthedragon.nucleartech.extensions.green
import at.martinthedragon.nucleartech.extensions.red
import net.minecraft.core.BlockPos
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.TextComponent
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.world.InteractionResultHolder
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.TooltipFlag
import net.minecraft.world.item.context.UseOnContext
import net.minecraft.world.level.Level
import com.google.common.collect.Lists
import net.minecraft.world.phys.HitResult
import org.apache.commons.lang3.ArrayUtils

class MultiDetonatorItem(properties: Properties) : Item(properties) {
    override fun useOn(context: UseOnContext): InteractionResult {
        val player = context.player
        val world = context.level
        if (player != null && player.isSecondaryUseActive) {
            val pos = context.clickedPos
            val block = world.getBlockState(pos)
            if (block.block !is IgnitableExplosive) return InteractionResult.FAIL
            if (!world.isClientSide) {
                addLocation(context.itemInHand, pos.x, pos.y, pos.z)
            }
            player.playSound(SoundEvents.randomBoop.get(), 2F, 1F)
            if (world.isClientSide) player.displayClientMessage(LangKeys.DEVICE_POSITION_SET.green(), true)
            return InteractionResult.sidedSuccess(world.isClientSide)
        }

        return InteractionResult.PASS
    }

    override fun use(world: Level, player: Player, hand: InteractionHand): InteractionResultHolder<ItemStack> {
        player.playSound(SoundEvents.randomBleep.get(), 1F, 1F)
        val itemStack = player.getItemInHand(hand)
        if (player.pick(20.0, 20f, false).type == HitResult.Type.BLOCK) {
            //月に触れる
            if (!player.isSecondaryUseActive)
                if (!world.isClientSide) processUse(itemStack, world, player)
        } else {
            //空を掻く
            if (player.isSecondaryUseActive) {
                removeLocation(itemStack)
            } else
                if (!world.isClientSide) processUse(itemStack, world, player)
        }
        return InteractionResultHolder.sidedSuccess(itemStack, world.isClientSide)
    }

    private fun processUse(stack: ItemStack, world: Level, player: Player) {
        if (!isPositionSet(stack)) {
            player.displayClientMessage(LangKeys.DEVICE_POSITION_NOT_SET.red(), true)
            return
        }
        val detonatorTag = stack.orCreateTag
        if (detonatorTag.contains("ExplosiveXs")) {
            for (pos in getLocations(stack)) {
                if (!NuclearConfig.explosions.detonateUnloadedBombs.get() && !world.isLoaded(pos)) {
                    player.displayClientMessage(LangKeys.DEVICE_POSITION_NOT_LOADED.red(), true)
                    return
                }
                val block = world.getBlockState(pos).block
                if (block !is IgnitableExplosive) {
                    player.displayClientMessage(LangKeys.DETONATOR_NO_EXPLOSIVE.red(), true)
                    return
                }
                val messageToSend = when (block.detonate(world, pos)) {
                    IgnitableExplosive.DetonationResult.Success -> LangKeys.DETONATOR_SUCCESS.green()
                    IgnitableExplosive.DetonationResult.InvalidPosition -> LangKeys.DETONATOR_NO_EXPLOSIVE.red()
                    IgnitableExplosive.DetonationResult.InvalidBlockEntity -> LangKeys.DETONATOR_INVALID_BLOCK_ENTITY.red()
                    IgnitableExplosive.DetonationResult.Incomplete -> LangKeys.DETONATOR_MISSING_COMPONENTS.red()
                    IgnitableExplosive.DetonationResult.Prohibited -> LangKeys.DETONATOR_PROHIBITED.red()
                    IgnitableExplosive.DetonationResult.Unknown -> LangKeys.DETONATOR_UNKNOWN_ERROR.red()
                }
                player.displayClientMessage(messageToSend, true)
            }
        }
    }
    private fun addLocation(i: ItemStack, x: Int, y: Int, z: Int) {
        val tag = i.orCreateTag
        val xArr = tag.getIntArray("ExplosiveXs")
        val yArr = tag.getIntArray("ExplosiveYs")
        val zArr = tag.getIntArray("ExplosiveZs")
        if (xArr.size == 10) return
        tag.putIntArray("ExplosiveXs", ArrayUtils.add(xArr, x))
        tag.putIntArray("ExplosiveYs", ArrayUtils.add(yArr, y))
        tag.putIntArray("ExplosiveZs", ArrayUtils.add(zArr, z))
    }
    private fun removeLocation(i: ItemStack) {
        val tag = i.orCreateTag
        tag.remove("ExplosiveXs")
        tag.remove("ExplosiveYs")
        tag.remove("ExplosiveZs")
    }
    private fun getLocations(i: ItemStack): List<BlockPos> {
        val tag = i.orCreateTag
        val xArr = tag.getIntArray("ExplosiveXs")
        val yArr = tag.getIntArray("ExplosiveYs")
        val zArr = tag.getIntArray("ExplosiveZs")
        val result = ArrayList<BlockPos>()
        for (index in xArr.indices) {
            result.add(BlockPos(xArr[index], yArr[index], zArr[index]))
        }
        return result
    }
    private fun isPositionSet(stack: ItemStack): Boolean = stack.hasTag() && stack.orCreateTag.contains("ExplosiveXs")

    override fun appendHoverText(stack: ItemStack, worldIn: Level?, tooltip: MutableList<Component>, flagIn: TooltipFlag) {
        autoTooltip(stack, tooltip)
        if (!isPositionSet(stack)) {
            tooltip += LangKeys.DEVICE_POSITION_NOT_SET.darkRed()
            return
        }
        for (location in getLocations(stack)) {
            val x = location.x
            val y = location.y
            val z = location.z
            tooltip += TextComponent("$x, $y, $z").gray()
        }
    }
}
