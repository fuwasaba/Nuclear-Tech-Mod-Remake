package at.martinthedragon.nucleartech.block

import at.martinthedragon.nucleartech.api.block.multi.MultiBlockPlacer
import at.martinthedragon.nucleartech.block.entity.DebugTowerBlockEntity
import at.martinthedragon.nucleartech.block.entity.OilDerrickBlockEntity
import at.martinthedragon.nucleartech.block.entity.SmartEntityBlock
import at.martinthedragon.nucleartech.logging.ClientLog
import net.minecraft.core.BlockPos
import net.minecraft.network.chat.TextComponent
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.phys.BlockHitResult

class DebugTowerBlock(properties: Properties): SmartEntityBlock(properties) {
    override fun newBlockEntity(pos: BlockPos, state: BlockState) = DebugTowerBlockEntity(pos, state)
    override fun use(state: BlockState, level: Level, pos: BlockPos, player: Player, hand: InteractionHand, result: BlockHitResult): InteractionResult {
        if (level.isClientSide) {
            for (log in ClientLog.logs) {
                player.displayClientMessage(TextComponent(log), false)
            }
        }
        return InteractionResult.SUCCESS
    }
    override fun onRemove(state: BlockState, level: Level, pos: BlockPos, newState: BlockState, p_196243_5_: Boolean) {
        dropMultiBlockEntityContentsAndRemoveStructure<DebugTowerBlockEntity>(state, level, pos, newState, ::placeMultiBlock)
        @Suppress("DEPRECATION") super.onRemove(state, level, pos, newState, p_196243_5_)
    }
    companion object {
        fun placeMultiBlock(placer: MultiBlockPlacer) = with(placer) {
            fill(2, 0, 0, -1, 0, 1, NTechBlocks.genericMultiBlockPart.get().defaultBlockState()) //階段一段目
            fill(2, 1, 1, -1, 1, 1, NTechBlocks.genericMultiBlockPart.get().defaultBlockState()) //階段二段目
            fill(10, 0, 2, -9, 1, 21, NTechBlocks.genericMultiBlockPart.get().defaultBlockState()) //階段二段目
        }
    }
}
