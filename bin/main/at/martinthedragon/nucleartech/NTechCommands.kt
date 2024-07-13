package at.martinthedragon.nucleartech

import at.martinthedragon.nucleartech.api.explosion.ExplosionLargeParams
import at.martinthedragon.nucleartech.api.explosion.NuclearExplosionMk4Params
import at.martinthedragon.nucleartech.explosion.ExplosionLarge
import at.martinthedragon.nucleartech.explosion.Explosions
import at.martinthedragon.nucleartech.fallout.FalloutTransformation
import at.martinthedragon.nucleartech.logging.debugY
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.FloatArgumentType
import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.exceptions.CommandSyntaxException
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.core.BlockPos
import net.minecraft.network.chat.TextComponent
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.phys.AABB
import java.util.function.Function

class NTechCommands {
    companion object {
        private val explosionCommand: LiteralArgumentBuilder<CommandSourceStack> = Commands.literal("explode")
            .then(Commands.argument("power", FloatArgumentType.floatArg(1f, 1000f))
                .then(Commands.argument("nuke", IntegerArgumentType.integer(0, 1))
                    .then(Commands.argument("rad", IntegerArgumentType.integer())
                    .executes {
                        if (!it.source.hasPermission(2)) {
                            try {
                                it.source.playerOrException.sendMessage(TextComponent("You must have permission level \"2\""), it.source.playerOrException.uuid);
                            } catch (ignored: CommandSyntaxException) {}
                        } else if (IntegerArgumentType.getInteger(it, "nuke") == 0) {
                            ExplosionLarge.createAndStart(it.source.level, it.source.position, FloatArgumentType.getFloat(it, "power"), ExplosionLargeParams(cloud = true, rubble = true, shrapnel = true))
                        } else if (IntegerArgumentType.getInteger(it, "nuke") == 1) {
                            Explosions.getBuiltinDefault().createAndStart(it.source.level, it.source.position, FloatArgumentType.getFloat(it, "power"), NuclearExplosionMk4Params(hasFallout = true, extraFallout = IntegerArgumentType.getInteger(it, "rad")))
                        }
                        1
                    })))
        private val decontaminationCommand: LiteralArgumentBuilder<CommandSourceStack> = Commands.literal("decontaminate")
            .executes {
                val source = it.source
                val level = source.level
                if (!source.hasPermission(2)) {
                    try {
                        source.playerOrException.sendMessage(TextComponent("You must have permission level \"2\""), it.source.playerOrException.uuid);
                    } catch (ignored: CommandSyntaxException) {}
                }
                val pos = source.position
                BlockPos.betweenClosedStream(
                    AABB(pos.x - 50, pos.y - 50, pos.z - 50, pos.x + 50, pos.y + 50, pos.z + 50)
                ).forEach { blockPos: BlockPos ->
                    run {
                        val blockState = level.getBlockState(blockPos)
                        if (FalloutTransformation.isDecontamiTarget(blockState)) {
                            level.setBlock(blockPos, FalloutTransformation.convertion(blockState), 3)
                        }
                    }
                }
                return@executes 1
            }
        fun register(dispatcher: CommandDispatcher<CommandSourceStack>) {
            dispatcher.register(explosionCommand)
            dispatcher.register(decontaminationCommand)
        }
    }
}
