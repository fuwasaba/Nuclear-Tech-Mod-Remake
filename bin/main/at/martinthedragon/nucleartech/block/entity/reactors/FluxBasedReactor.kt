package at.martinthedragon.nucleartech.block.entity.reactors

interface FluxBasedReactor {
    var flux: Double
    fun sendContinuousUpdatePacket()
}
