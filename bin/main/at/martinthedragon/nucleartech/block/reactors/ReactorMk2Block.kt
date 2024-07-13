package at.martinthedragon.nucleartech.block.reactors

import at.martinthedragon.nucleartech.api.block.entities.createSidedTickerChecked
import at.martinthedragon.nucleartech.api.block.multi.MultiBlockPlacer
import at.martinthedragon.nucleartech.block.NTechBlocks
import at.martinthedragon.nucleartech.block.dropMultiBlockEntityContentsAndRemoveStructure
import at.martinthedragon.nucleartech.block.entity.BlockEntityTypes
import at.martinthedragon.nucleartech.block.entity.SmartEntityBlock
import at.martinthedragon.nucleartech.block.entity.reactors.ReactorMk2BlockEntity
import at.martinthedragon.nucleartech.block.openMenu
import net.minecraft.core.BlockPos
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.entity.BlockEntityType
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.phys.BlockHitResult

class ReactorMk2Block(properties: Properties) : SmartEntityBlock(properties) {
    override fun newBlockEntity(pos: BlockPos, state: BlockState): BlockEntity = ReactorMk2BlockEntity(pos, state)
    override fun onRemove(state: BlockState, level: Level, pos: BlockPos, newState: BlockState, p_196243_5_: Boolean) {
        dropMultiBlockEntityContentsAndRemoveStructure<ReactorMk2BlockEntity>(state, level, pos, newState, Companion::placeMultiBlock)
        @Suppress("DEPRECATION") super.onRemove(state, level, pos, newState, p_196243_5_)
    }

    override fun use(state: BlockState, level: Level, pos: BlockPos, player: Player, hand: InteractionHand, res: BlockHitResult): InteractionResult {
        openMenu<ReactorMk2BlockEntity>(level, pos, player)
        return InteractionResult.SUCCESS
    }
    override fun <T : BlockEntity> getTicker(level: Level, state: BlockState, type: BlockEntityType<T>) = createSidedTickerChecked(level.isClientSide, type, BlockEntityTypes.reactorMk2BlockEntityType.get())

    companion object {
        fun placeMultiBlock(placer: MultiBlockPlacer) = with(placer) {
            fill(0, 0, 0, -1, 1, 1, NTechBlocks.genericMultiBlockPart.get().defaultBlockState())
            fill(1, 0, 2, -1, 0, 1, NTechBlocks.genericMultiBlockPart.get().defaultBlockState())
            place(-1, 1, 1, NTechBlocks.genericMultiBlockPart.get().defaultBlockState())

            place(-1, 1, 0, NTechBlocks.genericMultiBlockPort.get().defaultBlockState())
            place(-1, 0, 0, NTechBlocks.genericMultiBlockPort.get().defaultBlockState())
            place(1, 0, 2, NTechBlocks.genericMultiBlockPort.get().defaultBlockState())
        }
    }
}
