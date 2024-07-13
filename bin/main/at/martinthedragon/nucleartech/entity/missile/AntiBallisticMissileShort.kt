package at.martinthedragon.nucleartech.entity.missile

import at.martinthedragon.nucleartech.api.explosion.ExplosionLargeParams
import at.martinthedragon.nucleartech.entity.EntityTypes
import at.martinthedragon.nucleartech.explosion.ExplosionLarge
import at.martinthedragon.nucleartech.rendering.SpecialModels
import net.minecraft.core.BlockPos
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.boss.enderdragon.EnderDragon
import net.minecraft.world.entity.boss.wither.WitherBoss
import net.minecraft.world.entity.monster.Ghast
import net.minecraft.world.level.Level


class AntiBallisticMissileShort : AbstractAntiBallisticMissile {
    override val detectRange = 500
    override val distanceRange = 600
    override val maxSpeed = 50.0
    override val renderModel = SpecialModels.MISSILE_RIM67B.id
    override val renderScale = .8f
    override fun getAcceleration(velocity: Double) = 0.1
    constructor(entityType: EntityType<AntiBallisticMissileShort>, level: Level) : super(entityType, level)
    constructor(level: Level, startPos: BlockPos, targetPos: BlockPos) : super(EntityTypes.missileAntiBallisticShort.get(), level, startPos, targetPos)
    override fun isTarget(e: Entity): Boolean = e !is AbstractAntiBallisticMissile && (e is AbstractMissile || e is EnderDragon || e is Ghast || e is WitherBoss)
    override fun onImpact() {
        ExplosionLarge.createAndStart(level, position(), 10F, ExplosionLargeParams(cloud = true, rubble = true, shrapnel = true))
        super.onImpact()
    }
}
