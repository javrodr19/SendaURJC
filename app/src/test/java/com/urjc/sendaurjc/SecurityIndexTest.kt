package com.urjc.sendaurjc

import com.urjc.sendaurjc.domain.model.DataQuality
import com.urjc.sendaurjc.domain.model.GeoPoint
import com.urjc.sendaurjc.domain.model.RouteSegment
import org.junit.Assert.*
import org.junit.Test

/**
 * Tests for RF31 (security index), RF32 (composition), RF33 (lighting),
 * RF34 (traffic), RF35 (obstacles), RF36 (safest route identification)
 */
class SecurityIndexTest {

    private fun buildSegment(
        hasLight: Boolean,
        traffic: Int,
        hasObstacles: Boolean,
        quality: DataQuality = DataQuality.HIGH
    ) = RouteSegment(
        start = GeoPoint(0.0, 0.0),
        end = GeoPoint(0.0, 0.001),
        zoneId = "z1",
        hasActiveLight = hasLight,
        trafficIndex = traffic,
        hasObstacles = hasObstacles,
        dataQuality = quality.name
    )

    private val computeIndex = com.urjc.sendaurjc.domain.usecase.route.CalculateSecurityIndexUseCase()

    private fun computeIndex(segments: List<RouteSegment>): Double {
        return computeIndex.invoke(segments)
    }

    @Test
    fun `RF33 - lit street gets higher score than unlit`() {
        val lit = computeIndex(listOf(buildSegment(hasLight = true, traffic = 50, hasObstacles = false)))
        val unlit = computeIndex(listOf(buildSegment(hasLight = false, traffic = 50, hasObstacles = false)))
        assertTrue("Lit route should score higher", lit > unlit)
    }

    @Test
    fun `RF34 - higher traffic improves score`() {
        val highTraffic = computeIndex(listOf(buildSegment(hasLight = true, traffic = 100, hasObstacles = false)))
        val lowTraffic = computeIndex(listOf(buildSegment(hasLight = true, traffic = 0, hasObstacles = false)))
        assertTrue("High traffic route should score higher", highTraffic > lowTraffic)
    }

    @Test
    fun `RF35 - obstacle penalises route heavily`() {
        val clean = computeIndex(listOf(buildSegment(hasLight = true, traffic = 80, hasObstacles = false)))
        val blocked = computeIndex(listOf(buildSegment(hasLight = true, traffic = 80, hasObstacles = true)))
        assertTrue("Route with obstacle should score lower", blocked < clean)
    }

    @Test
    fun `RF31 - index stays in 0-100 range`() {
        val worstCase = computeIndex(listOf(buildSegment(false, 0, true, DataQuality.LOW)))
        val bestCase = computeIndex(listOf(buildSegment(true, 100, false, DataQuality.HIGH)))
        assertTrue(worstCase >= 0.0)
        assertTrue(bestCase <= 100.0)
    }

    @Test
    fun `RF36 - safest route has highest security index`() {
        val routes = listOf(
            computeIndex(listOf(buildSegment(true, 90, false))),
            computeIndex(listOf(buildSegment(false, 40, true))),
            computeIndex(listOf(buildSegment(true, 60, false)))
        )
        val maxIndex = routes.max()
        assertEquals("First route (best conditions) should be safest", routes[0], maxIndex, 0.001)
    }

    @Test
    fun `RF23 - low quality data reduces route score`() {
        val highQuality = computeIndex(listOf(buildSegment(true, 50, false, DataQuality.HIGH)))
        val lowQuality = computeIndex(listOf(buildSegment(true, 50, false, DataQuality.LOW)))
        assertTrue("Low quality data should reduce score", lowQuality < highQuality)
    }
}
