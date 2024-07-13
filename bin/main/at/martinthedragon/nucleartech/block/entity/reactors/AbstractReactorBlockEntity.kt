package at.martinthedragon.nucleartech.block.entity.reactors

import at.martinthedragon.nucleartech.NuclearTech
import at.martinthedragon.nucleartech.api.block.entities.TickingServerBlockEntity
import at.martinthedragon.nucleartech.api.explosion.NuclearExplosionMk4Params
import at.martinthedragon.nucleartech.block.entity.BaseMachineBlockEntity
import at.martinthedragon.nucleartech.block.entity.IODelegatedBlockEntity
import at.martinthedragon.nucleartech.explosion.Explosions
import at.martinthedragon.nucleartech.fluid.*
import at.martinthedragon.nucleartech.item.RBMKRodItem
import at.martinthedragon.nucleartech.math.toBlockPos
import at.martinthedragon.nucleartech.math.toVec3Middle
import at.martinthedragon.nucleartech.menu.NTechContainerMenu
import at.martinthedragon.nucleartech.menu.slots.data.FluidStackDataSlot
import at.martinthedragon.nucleartech.networking.BlockEntityUpdateMessage
import at.martinthedragon.nucleartech.networking.NuclearPacketHandler
import net.minecraft.core.BlockPos
import net.minecraft.nbt.CompoundTag
import net.minecraft.sounds.SoundEvent
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.entity.BlockEntityType
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.material.Fluid
import net.minecraft.world.level.material.Fluids
import net.minecraftforge.fluids.FluidStack
import net.minecraftforge.fluids.capability.CapabilityFluidHandler
import net.minecraftforge.fluids.capability.IFluidHandler
import net.minecraftforge.network.PacketDistributor

abstract class AbstractReactorBlockEntity(type: BlockEntityType<*>, pos: BlockPos, state: BlockState) : BaseMachineBlockEntity(type, pos, state),
    TickingServerBlockEntity, IODelegatedBlockEntity, ContainerFluidHandler {
    abstract var outputType: Fluid
    val waterTank = FluidInputTank(2_400_000).apply {
        forceFluid(FluidStack(Fluids.WATER.source, 0))
    }
    val steamTank = FluidOutputTank(2_400_000)
    var coreHeat = .0
    val meltPoint = 3000.0
    override val shouldPlaySoundLoop = false
    override val soundLoopEvent: SoundEvent
        get() = throw IllegalStateException("No sound loop for reactors")
    override fun getUpdateTag(): CompoundTag {
        val tag = CompoundTag()
        saveAdditional(tag)
        return tag
    }
    override fun serverTick(level: Level, pos: BlockPos, state: BlockState) {
        waterTank.forceFluid(FluidStack(Fluids.WATER.source, updateTag.getCompound("Tanks").getInt("Water")))
        if (isRemoved) NuclearTech.LOGGER.warn("BlockEntity at $blockPos requested update packet but was already removed")
        else NuclearPacketHandler.INSTANCE.send(PacketDistributor.ALL.noArg(), BlockEntityUpdateMessage(pos, updateTag))
        if (meltPoint < coreHeat) meltdown()
    }
    override fun handleContinuousUpdatePacket(tag: CompoundTag) {
        coreHeat = tag.getDouble("Heat")
        val tanks = tag.getCompound("Tanks")
        steamTank.readFromNBT(tanks.getCompound("Steam"))
        waterTank.forceFluid(FluidStack(Fluids.WATER.source, tanks.getInt("Water")))
    }
    override fun saveAdditional(tag: CompoundTag) {
        super.saveAdditional(tag)
        tag.putDouble("Heat", coreHeat)
        val tanks = CompoundTag()
        tanks.put("Steam", steamTank.writeToNBT(CompoundTag()))
        tanks.putInt("Water", waterTank.fluidAmount)
        tag.put("Tanks", tanks)
    }
    override fun load(tag: CompoundTag) {
        super.load(tag)
        coreHeat = tag.getDouble("Heat")
        val tanks = tag.getCompound("Tanks")
        steamTank.readFromNBT(tanks.getCompound("Steam"))
        waterTank.forceFluid(FluidStack(Fluids.WATER.source, tanks.getInt("Water")))
    }
    private var latestHeat = coreHeat
    fun createSteam(waterConsume: Int = 100, heatConsume: Int = 20, steamGenerated: Int = 100) {
        val temperatureChange = coreHeat - latestHeat
        val heatEnergy = 4.186 * temperatureChange

        //@TODO 処理

        if (waterConsume > waterTank.fluidAmount || coreHeat - heatConsume < outputType.attributes.temperature - 273 || steamTank.fluidAmount + steamGenerated > steamTank.capacity) return
        waterTank.fluid.amount -= waterConsume
        setHeat(coreHeat - heatConsume)
        if (steamTank.isEmpty) steamTank.fluid = FluidStack(outputType, steamGenerated)
        else steamTank.fluid.amount += steamGenerated
        latestHeat = coreHeat
    }
    protected open fun setHeat(heat: Double) {
        val fuel = mainInventory[0].item
        coreHeat = heat
        if (fuel is RBMKRodItem) {
            val rod = mainInventory[0]
            RBMKRodItem.setCoreHeat(rod, heat)
        }
        sendContinuousUpdatePacket()
    }
    open fun meltdown() {
        if (level == null) return
        onMeltdown()
    }
    protected open fun onMeltdown() {
        level!!.removeBlock(blockPos, false)
        level!!.removeBlockEntity(blockPos)
        level!!.setBlock(blockPos.toVec3Middle().add(.0, 2.0, .0).toBlockPos(), NTechFluids.corium.block.get().defaultBlockState(), 9)
        Explosions.getBuiltinDefault().createAndStart(level!!, blockPos.toVec3Middle(), 1f, NuclearExplosionMk4Params(hasFallout = true, extraFallout = 1))
    }
    override fun getTanks() = 2
    override fun getFluidInTank(tank: Int): FluidStack = if (tank > 1) FluidStack.EMPTY else if (tank == 1) steamTank.fluid else waterTank.fluid
    override fun getTankCapacity(tank: Int): Int = if (tank > 1) 0 else if (tank == 1) steamTank.capacity else waterTank.capacity
    override fun isFluidValid(tank: Int, stack: FluidStack) = if (tank == 1) false else stack.fluid == Fluids.WATER
    override fun fill(resource: FluidStack, action: IFluidHandler.FluidAction) = waterTank.fill(resource, action)
    override fun drain(resource: FluidStack, action: IFluidHandler.FluidAction) = steamTank.drain(resource, action)
    override fun drain(maxDrain: Int, action: IFluidHandler.FluidAction) = steamTank.drain(maxDrain, action)
    override fun trackContainerMenu(menu: NTechContainerMenu<*>) {
        val isClient = isClientSide()
        menu.track(FluidStackDataSlot.create(waterTank, isClient))
        menu.track(FluidStackDataSlot.create(steamTank, isClient))
    }
    init {
        registerCapabilityHandler(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, { this })
    }
}
