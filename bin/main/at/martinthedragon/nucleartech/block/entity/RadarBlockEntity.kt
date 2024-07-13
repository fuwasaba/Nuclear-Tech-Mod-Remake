package at.martinthedragon.nucleartech.block.entity

import at.martinthedragon.nucleartech.LangKeys
import at.martinthedragon.nucleartech.NuclearTech
import at.martinthedragon.nucleartech.SoundEvents
import at.martinthedragon.nucleartech.api.block.entities.TickingServerBlockEntity
import at.martinthedragon.nucleartech.energy.EnergyStorageExposed
import at.martinthedragon.nucleartech.entity.missile.AbstractMissile
import at.martinthedragon.nucleartech.entity.missile.AntiBallisticMissile
import at.martinthedragon.nucleartech.math.separateWith
import at.martinthedragon.nucleartech.math.toVec3Middle
import at.martinthedragon.nucleartech.menu.NTechContainerMenu
import at.martinthedragon.nucleartech.menu.RadarMenu
import net.minecraft.core.BlockPos
import net.minecraft.core.NonNullList
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerLevel
import net.minecraft.sounds.SoundEvent
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.inventory.AbstractContainerMenu
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.FaceAttachedHorizontalDirectionalBlock
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.state.properties.BlockStateProperties
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.Vec3
import net.minecraftforge.energy.CapabilityEnergy

class RadarBlockEntity(pos: BlockPos, state: BlockState): BaseMachineBlockEntity(BlockEntityTypes.radarBlockEntityType.get(), pos, state), TickingServerBlockEntity {
    private val pingCycle = 40
    private val detectRange = 2000
    private var tickExisted = 0
    private var isDetected = 0.0
    var energyClient = 0
    val energyStorage = EnergyStorageExposed(MAX_ENERGY)
    var energy: Int
        get() = energyStorage.energyStored
        private set(value) { energyStorage.energy = value }
    override val mainInventory: MutableList<ItemStack>
        get() = NonNullList.withSize(0, ItemStack.EMPTY)
    override val defaultName: Component
        get() = LangKeys.CONTAINER_RADAR.get()
    override fun createMenu(windowID: Int, inventory: Inventory): AbstractContainerMenu = RadarMenu(windowID, inventory, this)
    override fun isItemValid(slot: Int, stack: ItemStack) = true
    override fun trackContainerMenu(menu: NTechContainerMenu<*>) {}
    override val shouldPlaySoundLoop: Boolean = false
    override val soundLoopEvent: SoundEvent = SoundEvents.centrifugeOperate.get()
    override fun saveAdditional(tag: CompoundTag) {
        super.saveAdditional(tag)
        tag.putInt("Energy", energy)
    }
    override fun load(tag: CompoundTag) {
        super.load(tag)
        energy = tag.getInt("Energy")
    }
    override fun serverTick(level: Level, pos: BlockPos, state: BlockState) {
        if (tickExisted % pingCycle == 0) {
            if (energy > 0) {
                if ((level as ServerLevel).allEntities.any {
                    pos.toVec3Middle().distanceTo(Vec3(it.x, pos.y.toDouble(), it.z)) <= detectRange && it !is AntiBallisticMissile && it is AbstractMissile
                }) isDetected = 20.0
                energy -= 100
            } else {
                energy = 0
            }
            sendContinuousUpdatePacket()
        }
        if (isDetected > 0) isDetected -= 0.25
        if (isDetected != 0.0) {
            level.setBlock(pos, state.setValue(BlockStateProperties.POWERED, true), 3)
        } else {
            level.setBlock(pos, state.setValue(BlockStateProperties.POWERED, false), 3)
        }
        level.updateNeighborsAt(pos, state.block)
        tickExisted++
    }
    override fun getContinuousUpdateTag() = super.getContinuousUpdateTag().apply {
        putInt("Energy", energy)
    }

    override fun handleContinuousUpdatePacket(tag: CompoundTag) {
        super.handleContinuousUpdatePacket(tag)
        energyClient = tag.getInt("Energy")
    }
    init {
        registerCapabilityHandler(CapabilityEnergy.ENERGY, this::energyStorage)
    }
    companion object {
        const val MAX_ENERGY = 100_000
    }
}
