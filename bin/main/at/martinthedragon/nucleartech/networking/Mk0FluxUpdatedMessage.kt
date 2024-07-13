package at.martinthedragon.nucleartech.networking

import at.martinthedragon.nucleartech.block.entity.reactors.FluxBasedReactor
import net.minecraft.core.BlockPos
import net.minecraft.network.FriendlyByteBuf
import net.minecraftforge.network.NetworkEvent
import java.util.function.Supplier

class Mk0FluxUpdatedMessage(val flux: Double, val pos: BlockPos) : NetworkMessage<Mk0FluxUpdatedMessage> {
    override fun encode(packetBuffer: FriendlyByteBuf) {
        packetBuffer.writeDouble(flux)
        packetBuffer.writeBlockPos(pos)
    }

    override fun handle(context: Supplier<NetworkEvent.Context>) {
        if (context.get().direction.receptionSide.isServer) context.get().enqueueWork {
            val be = context.get().sender?.level?.getBlockEntity(pos)
            if (be is FluxBasedReactor) {
                be.flux = flux
                be.sendContinuousUpdatePacket()
            }
        }
    }
    companion object {
        @JvmStatic
        fun decode(packetBuffer: FriendlyByteBuf) = Mk0FluxUpdatedMessage(packetBuffer.readDouble(), packetBuffer.readBlockPos())
    }
}
