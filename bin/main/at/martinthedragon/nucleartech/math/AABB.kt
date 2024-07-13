package at.martinthedragon.nucleartech.math

import at.martinthedragon.nucleartech.NuclearTech
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.Vec3
import kotlin.math.abs
import kotlin.math.floor

const val separateX = 1000.0
const val separateY = 1000.0
const val separateZ = 1000.0

fun AABB.separateWith(x: Double = separateX, y: Double = separateY, z: Double = separateZ): List<AABB> {
    val height = maxY - minY
    val widthX = maxX - minX
    val widthZ = maxZ - minZ
    val xOvers = abs(floor(widthX / x)).toInt()
    val yOvers = abs(floor(height / y)).toInt()
    val zOvers = abs(floor(widthZ / z)).toInt()
    if (xOvers == 0 || yOvers == 0 || zOvers == 0) return listOf(this)
    val minCoords = arrayListOf<ArrayList<Double>>(arrayListOf(), arrayListOf(), arrayListOf())
    val maxCoords = arrayListOf<ArrayList<Double>>(arrayListOf(), arrayListOf(), arrayListOf())
    var xTemp = minX
    var yTemp = minY
    var zTemp = minZ

    for (i in 0..xOvers) {
        minCoords[0].add(xTemp)
        maxCoords[0].add(xTemp + x)
        xTemp += x
    }
    minCoords[0].add(xTemp)
    maxCoords[0].add(xTemp + widthX % x)

    for (i in 0..yOvers) {
        minCoords[1].add(yTemp)
        maxCoords[1].add(yTemp + y)
        yTemp += y
    }
    minCoords[1].add(yTemp)
    maxCoords[1].add(yTemp + height % y)

    for (i in 0..zOvers) {
        minCoords[2].add(zTemp)
        maxCoords[2].add(zTemp + z)
        zTemp += z
    }
    minCoords[2].add(zTemp)
    maxCoords[2].add(zTemp + widthZ % z)
    val result = arrayListOf<AABB>()
    for (i in 0 until minCoords[0].size) {
        result.add(AABB(minCoords[0][i], minCoords[1][i], minCoords[2][i], maxCoords[0][i], maxCoords[1][i], maxCoords[2][i]))
    }
    return result
}
