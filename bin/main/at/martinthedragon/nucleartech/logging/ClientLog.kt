package at.martinthedragon.nucleartech.logging

import net.minecraftforge.api.distmarker.Dist
import net.minecraftforge.api.distmarker.OnlyIn

@OnlyIn(Dist.CLIENT)
object ClientLog {
    @JvmStatic
    var logs: MutableList<String> = ArrayList()
}
