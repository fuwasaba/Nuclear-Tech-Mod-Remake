package at.martinthedragon.nucleartech.block.entity.reactors

import at.martinthedragon.nucleartech.NuclearTech
import at.martinthedragon.nucleartech.extensions.getOrNull
import at.martinthedragon.nucleartech.fluid.FluidInputTank
import at.martinthedragon.nucleartech.fluid.NTechFluids
import at.martinthedragon.nucleartech.menu.NTechContainerMenu
import at.martinthedragon.nucleartech.menu.slots.data.FluidStackDataSlot
import net.minecraft.core.BlockPos
import net.minecraft.nbt.CompoundTag
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.entity.BlockEntityType
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.material.Fluid
import net.minecraft.world.level.material.Fluids
import net.minecraftforge.common.util.LazyOptional
import net.minecraftforge.fluids.FluidStack
import net.minecraftforge.fluids.capability.IFluidHandler

abstract class AbstractCoolantBasedReactorBlockEntity(type: BlockEntityType<*>, pos: BlockPos, state: BlockState) : AbstractReactorBlockEntity(type, pos, state) {
    abstract val coolFluid: Fluid
    private val coolantConsumeRate = 20
    private val coolRate = 10
    var coolantTank: LazyOptional<FluidInputTank> = LazyOptional.of {
        FluidInputTank(2_400_000).apply {
            forceFluid(FluidStack(coolFluid, 0))
        }
    }

    override fun getUpdateTag(): CompoundTag {
        val tag = super.getUpdateTag()
        tag.putInt("Coolant", coolantTank.getOrNull()!!.fluidAmount)
        return tag
    }

    override fun handleContinuousUpdatePacket(tag: CompoundTag) {
        super.handleContinuousUpdatePacket(tag)
        coolantTank.getOrNull()!!.forceFluid(FluidStack(coolFluid, tag.getInt("Coolant")))
    }

    override fun saveAdditional(tag: CompoundTag) {
        super.saveAdditional(tag)
        tag.putInt("Coolant", coolantTank.getOrNull()!!.fluidAmount)
    }

    override fun load(tag: CompoundTag) {
        super.load(tag)
        coolantTank.getOrNull()!!.forceFluid(FluidStack(coolFluid, tag.getInt("Coolant")))
    }

    override fun serverTick(level: Level, pos: BlockPos, state: BlockState) {
        if ((coolantTank.getOrNull()!!.fluidAmount > coolantConsumeRate) && (coreHeat - coolRate > outputType.attributes.temperature - 273)) {
            NuclearTech.LOGGER.debug((coolantTank.getOrNull()!!.fluidAmount > coolantConsumeRate).toString() + " & " + (coreHeat - coolRate > outputType.attributes.temperature - 273))
            coolantTank.getOrNull()!!.fluid.amount -= coolantConsumeRate
            setHeat(coreHeat - coolRate)
        }
        super.serverTick(level, pos, state)
    }

    override fun getTanks() = 3
    override fun getFluidInTank(tank: Int): FluidStack = if (tank > 2) FluidStack.EMPTY else if (tank == 1) steamTank.fluid else if(tank == 2) coolantTank.getOrNull()!!.fluid else waterTank.fluid
    override fun getTankCapacity(tank: Int): Int = if (tank > 2) 0 else if (tank == 1) steamTank.capacity else if (tank == 2) coolantTank.getOrNull()!!.capacity else waterTank.capacity
    override fun isFluidValid(tank: Int, stack: FluidStack) = if (tank == 1) false else if (tank == 2) stack.fluid == coolFluid else stack.fluid == Fluids.WATER
    override fun fill(resource: FluidStack, action: IFluidHandler.FluidAction) = if (resource.fluid.isSame(Fluids.WATER.source)) waterTank.fill(resource, action)
    else if (resource.fluid.isSame(coolFluid)) coolantTank.getOrNull()!!.fill(resource, action)
    else 0
    override fun drain(resource: FluidStack, action: IFluidHandler.FluidAction) = steamTank.drain(resource, action)
    override fun drain(maxDrain: Int, action: IFluidHandler.FluidAction) = steamTank.drain(maxDrain, action)
    override fun trackContainerMenu(menu: NTechContainerMenu<*>) {
        val isClient = isClientSide()
        menu.track(FluidStackDataSlot.create(coolantTank.getOrNull()!!, isClient))
        super.trackContainerMenu(menu)
    }
}
