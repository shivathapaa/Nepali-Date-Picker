/*
 * Copyright © 2026 Shiva Thapa (@shivathapaa). All rights reserved.
 *
 * Licensed under the Mozilla Public License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://mozilla.org/MPL/2.0/
 */

@file:OptIn(ExperimentalTestApi::class)

package dev.shivathapaa.nepalidatepickerkmp.ios

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.runComposeUiTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

/**
 * A Swift caller cannot install a Compose theme around a hosted controller, so the appearance proxy
 * is the only route its colours take. These pin that route: which brightness is drawn, that an ARGB
 * integer reaches the role it names, that a zero leaves Material's own value alone, and that the
 * proxy goes back to its defaults when reset.
 */
class NepaliPickerAppearanceTest {

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

    @BeforeTest
    fun clearBefore() = NepaliPickerAppearance.reset()

    /** The proxy outlives a test, so a slot left set would theme every case after this one. */
    @AfterTest
    fun clearAfter() = NepaliPickerAppearance.reset()

    private fun everySlotSet() {
        NepaliPickerAppearance.primaryArgb = primary
        NepaliPickerAppearance.onPrimaryArgb = onPrimary
        NepaliPickerAppearance.primaryContainerArgb = primaryContainer
        NepaliPickerAppearance.onPrimaryContainerArgb = onPrimaryContainer
        NepaliPickerAppearance.secondaryContainerArgb = secondaryContainer
        NepaliPickerAppearance.onSecondaryContainerArgb = onSecondaryContainer
        NepaliPickerAppearance.surfaceArgb = surface
        NepaliPickerAppearance.onSurfaceArgb = onSurface
        NepaliPickerAppearance.surfaceVariantArgb = surfaceVariant
        NepaliPickerAppearance.onSurfaceVariantArgb = onSurfaceVariant
        NepaliPickerAppearance.outlineArgb = outline
    }

    @Test
    fun defaults_leaveEveryRoleToMaterial() {
        assertEquals(NepaliPickerBrightness.System, NepaliPickerAppearance.brightness)
        assertEquals(0, NepaliPickerAppearance.primaryArgb)
        assertEquals(0, NepaliPickerAppearance.onPrimaryArgb)
        assertEquals(0, NepaliPickerAppearance.primaryContainerArgb)
        assertEquals(0, NepaliPickerAppearance.onPrimaryContainerArgb)
        assertEquals(0, NepaliPickerAppearance.secondaryContainerArgb)
        assertEquals(0, NepaliPickerAppearance.onSecondaryContainerArgb)
        assertEquals(0, NepaliPickerAppearance.surfaceArgb)
        assertEquals(0, NepaliPickerAppearance.onSurfaceArgb)
        assertEquals(0, NepaliPickerAppearance.surfaceVariantArgb)
        assertEquals(0, NepaliPickerAppearance.onSurfaceVariantArgb)
        assertEquals(0, NepaliPickerAppearance.outlineArgb)
    }

    @Test
    fun reset_clearsEverySlotAndGoesBackToTheDevice() {
        everySlotSet()
        NepaliPickerAppearance.brightness = NepaliPickerBrightness.Dark

        NepaliPickerAppearance.reset()

        defaults_leaveEveryRoleToMaterial()
    }

    @Test
    fun light_pinsTheLightSchemeWhateverTheDeviceIsSetTo() = runComposeUiTest {
        NepaliPickerAppearance.brightness = NepaliPickerBrightness.Light
        lateinit var scheme: ColorScheme
        setContent { scheme = appearanceColorScheme() }
        waitForIdle()

        val expected = lightColorScheme()
        assertEquals(expected.primary, scheme.primary)
        assertEquals(expected.surface, scheme.surface)
        assertEquals(expected.onSurface, scheme.onSurface)
    }

    @Test
    fun dark_pinsTheDarkSchemeWhateverTheDeviceIsSetTo() = runComposeUiTest {
        NepaliPickerAppearance.brightness = NepaliPickerBrightness.Dark
        lateinit var scheme: ColorScheme
        setContent { scheme = appearanceColorScheme() }
        waitForIdle()

        val expected = darkColorScheme()
        assertEquals(expected.primary, scheme.primary)
        assertEquals(expected.surface, scheme.surface)
        assertEquals(expected.onSurface, scheme.onSurface)
    }

    @Test
    fun system_followsTheDevice() = runComposeUiTest {
        NepaliPickerAppearance.brightness = NepaliPickerBrightness.System
        lateinit var scheme: ColorScheme
        var systemDark = false
        setContent {
            systemDark = isSystemInDarkTheme()
            scheme = appearanceColorScheme()
        }
        waitForIdle()

        val expected = if (systemDark) darkColorScheme() else lightColorScheme()
        assertEquals(expected.primary, scheme.primary)
        assertEquals(expected.surface, scheme.surface)
    }

    @Test
    fun everySlotReachesTheRoleItNames() = runComposeUiTest {
        NepaliPickerAppearance.brightness = NepaliPickerBrightness.Dark
        everySlotSet()
        lateinit var scheme: ColorScheme
        setContent { scheme = appearanceColorScheme() }
        waitForIdle()

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
    fun theSurfaceAndAccentAlsoDriveTheRolesDerivedFromThem() = runComposeUiTest {
        NepaliPickerAppearance.brightness = NepaliPickerBrightness.Dark
        everySlotSet()
        lateinit var scheme: ColorScheme
        setContent { scheme = appearanceColorScheme() }
        waitForIdle()

        assertEquals(Color(surface), scheme.background, "background follows the surface")
        assertEquals(Color(onSurface), scheme.onBackground, "onBackground follows onSurface")
        assertEquals(Color(primary), scheme.surfaceTint, "the tint follows the accent")
    }

    @Test
    fun aZeroSlotKeepsMaterialsOwnValue() = runComposeUiTest {
        NepaliPickerAppearance.brightness = NepaliPickerBrightness.Dark
        NepaliPickerAppearance.primaryArgb = primary
        lateinit var scheme: ColorScheme
        setContent { scheme = appearanceColorScheme() }
        waitForIdle()

        val base = darkColorScheme()
        assertEquals(Color(primary), scheme.primary, "the one slot set is honoured")
        assertEquals(base.onPrimary, scheme.onPrimary)
        assertEquals(base.surface, scheme.surface)
        assertEquals(base.onSurface, scheme.onSurface)
        assertEquals(base.outline, scheme.outline)
    }

    @Test
    fun oneSurfaceAlone_leavesTheContainerTonesToMaterial() = runComposeUiTest {
        NepaliPickerAppearance.brightness = NepaliPickerBrightness.Dark
        NepaliPickerAppearance.surfaceArgb = surface
        lateinit var scheme: ColorScheme
        setContent { scheme = appearanceColorScheme() }
        waitForIdle()

        val base = darkColorScheme()
        assertEquals(base.surfaceContainer, scheme.surfaceContainer)
        assertEquals(base.surfaceContainerHighest, scheme.surfaceContainerHighest)
        assertEquals(base.surfaceBright, scheme.surfaceBright)
    }

    @Test
    fun bothSurfaces_derivesTheContainerTonesFromThem() = runComposeUiTest {
        NepaliPickerAppearance.brightness = NepaliPickerBrightness.Dark
        NepaliPickerAppearance.surfaceArgb = surface
        NepaliPickerAppearance.surfaceVariantArgb = surfaceVariant
        lateinit var scheme: ColorScheme
        setContent { scheme = appearanceColorScheme() }
        waitForIdle()

        val base = darkColorScheme()
        assertNotEquals(base.surfaceContainer, scheme.surfaceContainer)
        assertEquals(Color(surface), scheme.surfaceDim, "the dim end is the surface itself")

        // The container family sits on one ramp from the surface to the surface variant, so each
        // step is further along it than the one below.
        val ramp = listOf(
            scheme.surfaceContainerLow,
            scheme.surfaceContainer,
            scheme.surfaceContainerHigh,
            scheme.surfaceContainerHighest
        ).map { it.distanceFrom(Color(surface)) }
        assertEquals(ramp.sorted(), ramp, "the tones climb away from the surface in order")
    }

    /** How far apart two colours are, for checking the order of a ramp rather than its exact stops. */
    private fun Color.distanceFrom(other: Color): Float =
        (red - other.red) * (red - other.red) +
            (green - other.green) * (green - other.green) +
            (blue - other.blue) * (blue - other.blue)

    @Test
    fun bothSurfacesInLight_pinTheEndsOfTheRamp() = runComposeUiTest {
        NepaliPickerAppearance.brightness = NepaliPickerBrightness.Light
        NepaliPickerAppearance.surfaceArgb = surface
        NepaliPickerAppearance.surfaceVariantArgb = surfaceVariant
        lateinit var scheme: ColorScheme
        setContent { scheme = appearanceColorScheme() }
        waitForIdle()

        assertEquals(Color(surface), scheme.surfaceBright, "the bright end is the surface itself")
        assertEquals(Color.White, scheme.surfaceContainerLowest)
        assertEquals(
            Color(surfaceVariant),
            scheme.surfaceContainerHighest,
            "the highest tone is the surface variant itself"
        )
    }
}
