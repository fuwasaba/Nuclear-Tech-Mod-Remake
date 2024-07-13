package at.martinthedragon.nucleartech.entity.missile

import at.martinthedragon.nucleartech.NuclearTech
import at.martinthedragon.nucleartech.api.explosion.ExplosionLargeParams
import at.martinthedragon.nucleartech.entity.EntityTypes
import at.martinthedragon.nucleartech.explosion.ExplosionLarge
import at.martinthedragon.nucleartech.math.separateWith
import at.martinthedragon.nucleartech.math.toVec3Middle
import at.martinthedragon.nucleartech.particle.ContrailParticleOptions
import at.martinthedragon.nucleartech.particle.sendParticles
import at.martinthedragon.nucleartech.world.DamageSources
import com.mojang.math.Vector3f
import net.minecraft.core.BlockPos
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.MoverType
import net.minecraft.world.entity.boss.enderdragon.EnderDragon
import net.minecraft.world.entity.boss.wither.WitherBoss
import net.minecraft.world.entity.monster.Ghast
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.levelgen.Heightmap
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.Vec3
import kotlin.math.pow
import kotlin.math.sqrt


abstract class AbstractAntiBallisticMissile : AbstractMissile {
    protected open val detectRange = 2000
    protected open val distanceRange = 1000
    protected open val maxSpeed = 30.0
    protected open val steps = 5.0
    protected open var velocityAB: Double = 0.0
    protected open var motionX: Double = 0.0
    protected open var motionY: Double = 0.5
    protected open var motionZ: Double = 0.0
    protected open var prevPosY: Double = 0.0
    protected open var ticksExisted: Int = 0
    override val renderModel = MODEL_MISSILE_RIM67B
    override val renderScale = 1f
    override val renderTexture = missileTexture("missile_rim67b")
    constructor(entityType: EntityType<out AbstractAntiBallisticMissile>, level: Level) : super(entityType, level)
    constructor(entityType: EntityType<out AbstractAntiBallisticMissile>, level: Level, startPos: BlockPos, targetPos: BlockPos) : super(entityType, level, startPos, targetPos) {
        isEntityTick = true
    }
    protected abstract fun getAcceleration(velocity: Double): Double
    override fun tick() {
        val oldPosY: Double = position().y
        super.tick()
        ticksExisted++
        if (level.isClientSide) return
        if (!level.isClientSide) (level as ServerLevel).sendParticles(ContrailParticleOptions(Vector3f.ZERO, .4F), true, x, y, z, 6, 0.0, 0.0, 0.0, 0.0)
        manageForcedChunks()
        val b: Block = level.getBlockState(BlockPos(position().x.toInt(), position().y.toInt(), position().z.toInt())).block
        if (b !== Blocks.AIR && b !== Blocks.WATER || position().y < 1 || position().y > 7000) {
            if (position().y < 1) {
                moveTo(position().x, level.getHeight(Heightmap.Types.WORLD_SURFACE, position().x.toInt(), position().z.toInt()).toDouble(), position().z, 0f, 0f)
            }
            if (!level.isClientSide) {
                onImpact()
            }
            discard()
            return
        }
        if (position().distanceTo(startPos.toVec3Middle()) >= distanceRange) {
            onImpact()
            discard()
            return
        }
        if (ticksExisted < 35) {
            setDeltaMovement(
                0.0,
                0.18,
                0.0
            )
            move(MoverType.SELF, deltaMovement)
            return
        }
        if (velocityAB < maxSpeed) velocityAB += getAcceleration(velocityAB)
        var i = 0
        while (i < steps) {
            val targetVec = targetFlyingObject()
            if (targetVec != null) {
                motionX = targetVec[0] * velocityAB
                motionY = targetVec[1] * velocityAB
                motionZ = targetVec[2] * velocityAB
            }
            setDeltaMovement(
                motionX * velocityAB,
                motionY * velocityAB,
                motionZ * velocityAB
            )
            move(MoverType.SELF, deltaMovement)
            updateRotation()
            explodeIfNearTarget()
            i++
        }
        prevPosY = oldPosY
    }

    private fun targetFlyingObject(): DoubleArray? {
        //Targeting missiles - returns normalized vector pointing towards the closest rocket
        val targets = (level as ServerLevel).allEntities.filter {
            position().distanceTo(Vec3(it.x, position().y, it.z)) <= detectRange && it !is AntiBallisticMissile && it is AbstractMissile
        }
        var target: Entity? = null
        var closest: Double = detectRange * 2.0
        for (e in targets) {
            val dis = sqrt((e.position().x - position().x).pow(2.0) + (e.position().y - position().y).pow(2.0) + (e.position().z - position().z).pow(2.0))
            if (dis < closest) {
                closest = dis
                target = e
            }
        }
        if (target != null) {
            var vec = Vec3(target.position().x - position().x, target.position().y - position().y, target.position().z - position().z)
            vec = vec.normalize()
            return doubleArrayOf(vec.x / steps, vec.y / steps, vec.z / steps)
        }
        return null
    }
    private fun explodeIfNearTarget() {
        val listOfMissilesInExplosionRange: List<Entity> = level.getEntities(null, AABB(position().x - 7.5, position().y - 7.5, position().z - 7.5, position().x + 7.5, position().y + 7.5, position().z + 7.5))
        var hasHits = false
        for (e in listOfMissilesInExplosionRange) {
            if (isTarget(e)) {
                e.hurt(DamageSources.shrapnel, 40f)
                hasHits = true
            }
        }
        if (hasHits) {
            onImpact()
            discard()
            return
        }
    }
    protected open fun isTarget(e: Entity): Boolean = e !is AbstractAntiBallisticMissile && (e is AbstractMissile || e is EnderDragon || e is Ghast || e is WitherBoss)
    override fun onImpact() {
        killMissile()
        discard()
    }
}
