package at.martinthedragon.nucleartech.menu

import at.martinthedragon.nucleartech.block.entity.RadarBlockEntity
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.entity.player.Player
import net.minecraft.world.inventory.AbstractContainerMenu

class RadarMenu (
    windowID: Int,
    playerInventory: Inventory,
    blockEntity: RadarBlockEntity
) : AbstractContainerMenu(MenuTypes.radarMenu.get(), windowID) {
    companion object {
        fun fromNetwork(windowID: Int, playerInventory: Inventory, buffer: FriendlyByteBuf) =
            RadarMenu(windowID, playerInventory, getBlockEntityForContainer(buffer))
    }

    override fun stillValid(player: Player) = true
}
