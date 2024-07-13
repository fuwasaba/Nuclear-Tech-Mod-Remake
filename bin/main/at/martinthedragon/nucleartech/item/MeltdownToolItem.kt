package at.martinthedragon.nucleartech.item

import at.martinthedragon.nucleartech.block.entity.reactors.AbstractReactorBlockEntity
import at.martinthedragon.nucleartech.block.multi.MultiBlockPart
import at.martinthedragon.nucleartech.block.rbmk.RBMKPart
import net.minecraft.world.InteractionResult
import net.minecraft.world.item.Item
import net.minecraft.world.item.context.UseOnContext

class MeltdownToolItem(properties: Properties) : Item(properties) {
    override fun useOn(context: UseOnContext): InteractionResult {
        val level = context.level
        val clickedPos = context.clickedPos
        val blockState = level.getBlockState(clickedPos)
        val block = blockState.block
        val blockEntity = level.getBlockEntity(clickedPos)
        if (block is RBMKPart) {
            val rbmkBase = block.getTopRBMKBase(level, clickedPos, blockState) ?: return InteractionResult.FAIL
            if (!level.isClientSide) rbmkBase.meltdown()
        } else if (blockEntity is AbstractReactorBlockEntity) {
            blockEntity.meltdown()
        } else if (blockEntity is MultiBlockPart.MultiBlockPartBlockEntity) {
            level.getBlockEntity(blockEntity.core)?.let {
                if (it is AbstractReactorBlockEntity) it.meltdown()
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide)
    }
}
