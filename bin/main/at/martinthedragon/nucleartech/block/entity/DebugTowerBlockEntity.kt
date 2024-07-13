package at.martinthedragon.nucleartech.block.entity

import net.minecraft.core.BlockPos
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.state.BlockState

class DebugTowerBlockEntity(pos: BlockPos, state: BlockState): BlockEntity(BlockEntityTypes.debugTowerEntityType.get(),  pos, state) {
}
