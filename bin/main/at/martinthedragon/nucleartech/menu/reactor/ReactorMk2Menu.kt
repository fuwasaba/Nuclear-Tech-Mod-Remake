package at.martinthedragon.nucleartech.menu.reactor

import at.martinthedragon.nucleartech.block.entity.reactors.ReactorMk2BlockEntity
import at.martinthedragon.nucleartech.menu.MenuTypes
import at.martinthedragon.nucleartech.menu.addPlayerInventory
import at.martinthedragon.nucleartech.menu.getBlockEntityForContainer
import at.martinthedragon.nucleartech.menu.quickMoveStackBoilerplate
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.entity.player.Player
import net.minecraft.world.inventory.AbstractContainerMenu
import net.minecraft.world.item.ItemStack
import net.minecraftforge.items.CapabilityItemHandler
import net.minecraftforge.items.SlotItemHandler

class ReactorMk2Menu(windowID: Int, val playerInventory: Inventory, val blockEntity: ReactorMk2BlockEntity) : AbstractContainerMenu(MenuTypes.reactorMk2Menu.get(), windowID) {
    private val inv = blockEntity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).orElseThrow(::Error)
    init {
        addSlot(SlotItemHandler(inv, 0, 80, 59))
        addPlayerInventory(this::addSlot, playerInventory, 8, 140)
    }
    companion object {
        fun fromNetwork(windowID: Int, playerInventory: Inventory, buf: FriendlyByteBuf) = ReactorMk2Menu(windowID, playerInventory, getBlockEntityForContainer(buf))
    }
    override fun quickMoveStack(player: Player, index: Int): ItemStack = quickMoveStackBoilerplate(player, index, 21, intArrayOf(5, 6, 7, 8, 11, 12, 19, 20)) {
        0..0 check isFuel()
        0..0
    }
    override fun stillValid(player: Player) = true
}
