/*
 * Copyright © 2026 Shiva Thapa (@shivathapaa). All rights reserved.
 *
 * Licensed under the Mozilla Public License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://mozilla.org/MPL/2.0/
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.shivathapaa.nepalidatepickerkmp.embed

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

/**
 * Pins the shared slot-to-role mapping on the Android host, with the same expectations the iOS
 * appearance test pins through the composable path, so the two hosts cannot drift apart.
 */
class EmbeddedAppearanceSchemeTest {

    private val primary = 0xFFB1D18A.toInt()
    private val onPrimary = 0xFF1F3701.toInt()
    private val primaryContainer = 0xFF354E16.toInt()
    private val onPrimaryContainer = 0xFFCDEDA3.toInt()
    private val secondaryContainer = 0xFF3A4A34.toInt()
    private val onSecondaryContainer = 0xFFD6E8C8.toInt()
    private val surface = 0xFF12140E.toInt()
    private val onSurface = 0xFFE2E3D8.toInt()
    private val surfaceVariant = 0xFF44483D.toInt()
    private val onSurfaceVariant = 0xFFC5C8BA.toInt()
    private val outline = 0xFF8E9285.toInt()

    private fun emptySlots() = EmbeddedAppearanceSlots(
        primaryArgb = 0,
        onPrimaryArgb = 0,
        primaryContainerArgb = 0,
        onPrimaryContainerArgb = 0,
        secondaryContainerArgb = 0,
        onSecondaryContainerArgb = 0,
        surfaceArgb = 0,
        onSurfaceArgb = 0,
        surfaceVariantArgb = 0,
        onSurfaceVariantArgb = 0,
        outlineArgb = 0
    )

    private fun everySlotSet() = EmbeddedAppearanceSlots(
        primaryArgb = primary,
        onPrimaryArgb = onPrimary,
        primaryContainerArgb = primaryContainer,
        onPrimaryContainerArgb = onPrimaryContainer,
        secondaryContainerArgb = secondaryContainer,
        onSecondaryContainerArgb = onSecondaryContainer,
        surfaceArgb = surface,
        onSurfaceArgb = onSurface,
        surfaceVariantArgb = surfaceVariant,
        onSurfaceVariantArgb = onSurfaceVariant,
        outlineArgb = outline
    )

    @Test
    fun zeroSlots_keepMaterialsOwnSchemes() {
        val dark = derivedPickerColorScheme(dark = true, slots = emptySlots())
        assertEquals(darkColorScheme().primary, dark.primary)
        assertEquals(darkColorScheme().surface, dark.surface)
        assertEquals(darkColorScheme().onSurface, dark.onSurface)

        val light = derivedPickerColorScheme(dark = false, slots = emptySlots())
        assertEquals(lightColorScheme().primary, light.primary)
        assertEquals(lightColorScheme().surface, light.surface)
        assertEquals(lightColorScheme().onSurface, light.onSurface)
    }

    @Test
    fun everySlotReachesTheRoleItNames() {
        val scheme = derivedPickerColorScheme(dark = true, slots = everySlotSet())
        assertEquals(Color(primary), scheme.primary)
        assertEquals(Color(onPrimary), scheme.onPrimary)
        assertEquals(Color(primaryContainer), scheme.primaryContainer)
        assertEquals(Color(onPrimaryContainer), scheme.onPrimaryContainer)
        assertEquals(Color(secondaryContainer), scheme.secondaryContainer)
        assertEquals(Color(onSecondaryContainer), scheme.onSecondaryContainer)
        assertEquals(Color(surface), scheme.surface)
        assertEquals(Color(onSurface), scheme.onSurface)
        assertEquals(Color(surfaceVariant), scheme.surfaceVariant)
        assertEquals(Color(onSurfaceVariant), scheme.onSurfaceVariant)
        assertEquals(Color(outline), scheme.outline)
    }

    @Test
    fun theSurfaceAndAccentAlsoDriveTheRolesDerivedFromThem() {
        val scheme = derivedPickerColorScheme(dark = true, slots = everySlotSet())
        assertEquals(Color(surface), scheme.background, "background follows the surface")
        assertEquals(Color(onSurface), scheme.onBackground, "onBackground follows onSurface")
        assertEquals(Color(primary), scheme.surfaceTint, "the tint follows the accent")
    }

    @Test
    fun aZeroSlotKeepsMaterialsOwnValue() {
        val slots = emptySlots().copyWith(primaryArgb = primary)
        val scheme = derivedPickerColorScheme(dark = true, slots = slots)
        assertEquals(Color(primary), scheme.primary)
        assertEquals(darkColorScheme().onPrimary, scheme.onPrimary)
        assertEquals(darkColorScheme().surface, scheme.surface)
        assertEquals(darkColorScheme().onSurface, scheme.onSurface)
        assertEquals(darkColorScheme().outline, scheme.outline)
    }

    @Test
    fun oneSurfaceAlone_leavesTheContainerTonesToMaterial() {
        val slots = emptySlots().copyWith(surfaceArgb = surface)
        val scheme = derivedPickerColorScheme(dark = true, slots = slots)
        assertEquals(darkColorScheme().surfaceContainer, scheme.surfaceContainer)
        assertEquals(darkColorScheme().surfaceContainerHighest, scheme.surfaceContainerHighest)
        assertEquals(darkColorScheme().surfaceBright, scheme.surfaceBright)
    }

    @Test
    fun bothSurfaces_deriveTheContainerTonesFromThem() {
        val slots = emptySlots().copyWith(surfaceArgb = surface, surfaceVariantArgb = surfaceVariant)
        val scheme = derivedPickerColorScheme(dark = true, slots = slots)
        assertNotEquals(darkColorScheme().surfaceContainer, scheme.surfaceContainer)
        assertEquals(Color(surface), scheme.surfaceDim, "the dim end is the surface itself")

        val climb = listOf(
            scheme.surfaceContainerLow,
            scheme.surfaceContainer,
            scheme.surfaceContainerHigh,
            scheme.surfaceContainerHighest
        ).map { it.distanceFrom(Color(surface)) }
        assertEquals(climb.sorted(), climb, "the container ramp climbs away from the surface")
    }

    @Test
    fun bothSurfacesInLight_pinTheEndsOfTheRamp() {
        val slots = emptySlots().copyWith(surfaceArgb = surface, surfaceVariantArgb = surfaceVariant)
        val scheme = derivedPickerColorScheme(dark = false, slots = slots)
        assertEquals(Color(surface), scheme.surfaceBright, "the bright end is the surface itself")
        assertEquals(Color.White, scheme.surfaceContainerLowest)
        assertEquals(
            Color(surfaceVariant),
            scheme.surfaceContainerHighest,
            "the highest tone is the surface variant itself"
        )
    }

    @Test
    fun rampStaysInsideTheTwoSurfacesInDark() {
        val slots = emptySlots().copyWith(surfaceArgb = surface, surfaceVariantArgb = surfaceVariant)
        val scheme = derivedPickerColorScheme(dark = true, slots = slots)
        val span = Color(surfaceVariant).distanceFrom(Color(surface))
        assertTrue(scheme.surfaceContainerHighest.distanceFrom(Color(surface)) <= span)
    }

    private fun EmbeddedAppearanceSlots.copyWith(
        primaryArgb: Int = this.primaryArgb,
        surfaceArgb: Int = this.surfaceArgb,
        surfaceVariantArgb: Int = this.surfaceVariantArgb
    ) = EmbeddedAppearanceSlots(
        primaryArgb = primaryArgb,
        onPrimaryArgb = onPrimaryArgb,
        primaryContainerArgb = primaryContainerArgb,
        onPrimaryContainerArgb = onPrimaryContainerArgb,
        secondaryContainerArgb = secondaryContainerArgb,
        onSecondaryContainerArgb = onSecondaryContainerArgb,
        surfaceArgb = surfaceArgb,
        onSurfaceArgb = onSurfaceArgb,
        surfaceVariantArgb = surfaceVariantArgb,
        onSurfaceVariantArgb = onSurfaceVariantArgb,
        outlineArgb = outlineArgb
    )

    private fun Color.distanceFrom(other: Color): Float {
        val dr = red - other.red
        val dg = green - other.green
        val db = blue - other.blue
        return dr * dr + dg * dg + db * db
    }
}
