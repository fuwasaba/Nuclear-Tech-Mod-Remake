package at.martinthedragon.nucleartech.api.block.reactors

import net.minecraft.world.level.material.Fluid

public class ReactorInfo(type: Fluid) {
    public var outputType: Fluid
    public var steam: Int = 0
    public var water: Int = 0
    public var heat: Double = .0
    init {
        outputType = type
    }
}
