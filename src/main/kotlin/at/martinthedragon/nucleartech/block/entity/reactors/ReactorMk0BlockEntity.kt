package at.martinthedragon.nucleartech.block.entity.reactors

import at.martinthedragon.nucleartech.LangKeys
import at.martinthedragon.nucleartech.NuclearTech
import at.martinthedragon.nucleartech.api.block.reactors.ReactorInfo
import at.martinthedragon.nucleartech.block.entity.BaseMachineBlockEntity
import at.martinthedragon.nucleartech.block.entity.BlockEntityTypes
import at.martinthedragon.nucleartech.block.entity.IOConfiguration
import at.martinthedragon.nucleartech.block.entity.IODelegatedBlockEntity
import at.martinthedragon.nucleartech.fluid.NTechFluids
import at.martinthedragon.nucleartech.item.RBMKRodItem
import at.martinthedragon.nucleartech.menu.NTechContainerMenu
import at.martinthedragon.nucleartech.menu.reactor.ReactorMk0Menu
import at.martinthedragon.nucleartech.networking.BlockEntityUpdateMessage
import at.martinthedragon.nucleartech.networking.NuclearPacketHandler
import com.google.common.collect.Table
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.core.NonNullList
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.TranslatableComponent
import net.minecraft.sounds.SoundEvent
import net.minecraft.sounds.SoundEvents
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.inventory.AbstractContainerMenu
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.material.Fluid
import net.minecraft.world.level.material.Fluids
import net.minecraftforge.fluids.FluidStack
import net.minecraftforge.fluids.capability.IFluidHandler
import net.minecraftforge.network.PacketDistributor

class ReactorMk0BlockEntity(pos: BlockPos, state: BlockState) : ReactorBlockEntity(BlockEntityTypes.reactorMk0BlockEntityType.get(), pos, state) {
    override var outputType: Fluid = NTechFluids.steam.source.get()
    override val mainInventory: MutableList<ItemStack> = NonNullList.withSize(1, ItemStack.EMPTY)
    override val defaultName = LangKeys.CONTAINER_REACTOR_MK0.get()
    override fun createMenu(windowID: Int, inventory: Inventory) = ReactorMk0Menu(windowID, inventory, this)
    override fun isItemValid(slot: Int, stack: ItemStack): Boolean = true
    override val shouldPlaySoundLoop: Boolean = false
    override val soundLoopEvent: SoundEvent
        get() = throw IllegalStateException("No sound loop for mk0")

    override val ioConfigurations = IODelegatedBlockEntity.fromTriples(blockPos, getHorizontalBlockRotation(),
        Triple(BlockPos(0, 1, 0), Direction.SOUTH, listOf(IOConfiguration(IOConfiguration.Mode.BOTH, IODelegatedBlockEntity.DEFAULT_FLUID_ACTION))),
        Triple(BlockPos(-1, 1, 1), Direction.WEST, listOf(IOConfiguration(IOConfiguration.Mode.BOTH, IODelegatedBlockEntity.DEFAULT_FLUID_ACTION))),
        Triple(BlockPos(0, 1, 2), Direction.NORTH, listOf(IOConfiguration(IOConfiguration.Mode.BOTH, IODelegatedBlockEntity.DEFAULT_FLUID_ACTION))),
        Triple(BlockPos(1, 1, 1), Direction.EAST, listOf(IOConfiguration(IOConfiguration.Mode.BOTH, IODelegatedBlockEntity.DEFAULT_FLUID_ACTION)))
    )
    var flux = 0.0

    override fun getUpdateTag(): CompoundTag {
        val tag = super.getUpdateTag()
        tag.putDouble("FluxIn", flux)
        return tag
    }

    override fun handleContinuousUpdatePacket(tag: CompoundTag) {
        if (!tag.contains("FluxIn")) return
        super.handleContinuousUpdatePacket(tag)
        flux = tag.getDouble("FluxIn")
    }
    override fun saveAdditional(tag: CompoundTag) {
        super.saveAdditional(tag)
        tag.putDouble("FluxIn", flux)
    }

    override fun load(tag: CompoundTag) {
        super.load(tag)
        flux = tag.getDouble("FluxIn")
    }
    override fun serverTick(level: Level, pos: BlockPos, state: BlockState) {
        super.serverTick(level, pos, state)
        val rod = mainInventory[0]
        if (rod.item is RBMKRodItem) {
            val fuel = rod.item as RBMKRodItem
            fuel.burn(rod, flux)
            fuel.updateHeat(rod, 1.0)
            setHeat(RBMKRodItem.getCoreHeat(rod))
        }
        if (coreHeat >= 100 || (waterTank.fluidAmount > 0 && steamTank.fluid.fluid == outputType)) createSteam()
    }
}
