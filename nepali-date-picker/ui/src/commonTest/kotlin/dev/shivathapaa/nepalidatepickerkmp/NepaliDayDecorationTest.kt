/*
 * Copyright © 2026 Shiva Thapa (@shivathapaa). All rights reserved.
 *
 * Licensed under the Mozilla Public License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://mozilla.org/MPL/2.0/
 */

@file:OptIn(ExperimentalNepaliDatePickerApi::class)

package dev.shivathapaa.nepalidatepickerkmp

import androidx.compose.ui.graphics.Color
import dev.shivathapaa.nepalidatepickerkmp.annotations.ExperimentalNepaliDatePickerApi
import dev.shivathapaa.nepalidatepickerkmp.calendar_model.DisabledAlpha
import dev.shivathapaa.nepalidatepickerkmp.calendar_model.MaxDayIndicators
import dev.shivathapaa.nepalidatepickerkmp.calendar_model.MaxDualDateDayIndicators
import dev.shivathapaa.nepalidatepickerkmp.calendar_model.NepaliDayDecoration
import dev.shivathapaa.nepalidatepickerkmp.calendar_model.resolveDayVisuals
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * How a decoration and the theme's own day colors are reconciled. Selection and the disabled state
 * have to keep the look they had before decorations existed, so these spell out which side wins in
 * every cell state.
 */
class NepaliDayDecorationTest {

    private val themeContainer = Color(0xFF101010)
    private val themeContent = Color(0xFF202020)
    private val onSelected = Color(0xFF303030)
    private val onRange = Color(0xFF404040)
    private val holiday = Color(0xFFFF0000)
    private val festival = Color(0xFF00FF00)

    private fun resolve(
        decoration: NepaliDayDecoration?,
        isSelected: Boolean = false,
        isInRange: Boolean = false,
        isEnabled: Boolean = true,
        maxIndicators: Int = MaxDayIndicators
    ) = resolveDayVisuals(
        decoration = decoration,
        themeContainerColor = themeContainer,
        themeContentColor = themeContent,
        selectedIndicatorColor = onSelected,
        inRangeIndicatorColor = onRange,
        isSelected = isSelected,
        isInRange = isInRange,
        isEnabled = isEnabled,
        maxIndicators = maxIndicators
    )

    @Test
    fun noDecoration_leavesTheThemeAlone() {
        val visuals = resolve(decoration = null)

        assertEquals(themeContainer, visuals.containerColor)
        assertEquals(themeContent, visuals.contentColor)
        assertTrue(visuals.indicators.isEmpty())
    }

    @Test
    fun plainDay_takesTheDecorationsColors() {
        val visuals = resolve(
            NepaliDayDecoration(
                contentColor = holiday,
                containerColor = festival,
                indicators = listOf(holiday)
            )
        )

        assertEquals(festival, visuals.containerColor)
        assertEquals(holiday, visuals.contentColor)
        assertEquals(listOf(holiday), visuals.indicators)
    }

    @Test
    fun unspecifiedColors_fallThroughToTheTheme() {
        val visuals = resolve(NepaliDayDecoration(indicators = listOf(holiday)))

        assertEquals(themeContainer, visuals.containerColor)
        assertEquals(themeContent, visuals.contentColor)
        assertEquals(listOf(holiday), visuals.indicators)
    }

    @Test
    fun selectedDay_keepsTheThemeAndRepaintsTheDots() {
        val visuals = resolve(
            NepaliDayDecoration(
                contentColor = holiday,
                containerColor = holiday,
                indicators = listOf(holiday, festival)
            ),
            isSelected = true
        )

        assertEquals(themeContainer, visuals.containerColor)
        assertEquals(themeContent, visuals.contentColor)
        assertEquals(listOf(onSelected, onSelected), visuals.indicators)
    }

    @Test
    fun dayInRange_keepsTheShadingAndRepaintsTheDots() {
        val visuals = resolve(
            NepaliDayDecoration(contentColor = holiday, indicators = listOf(holiday)),
            isInRange = true
        )

        assertEquals(themeContainer, visuals.containerColor)
        assertEquals(themeContent, visuals.contentColor)
        assertEquals(listOf(onRange), visuals.indicators)
    }

    @Test
    fun disabledDay_staysGreyAndFadesItsDots() {
        val visuals = resolve(
            NepaliDayDecoration(
                contentColor = holiday,
                containerColor = festival,
                indicators = listOf(holiday)
            ),
            isEnabled = false
        )

        assertEquals(themeContainer, visuals.containerColor)
        assertEquals(themeContent, visuals.contentColor)
        assertEquals(1, visuals.indicators.size)
        assertEquals(holiday.copy(alpha = DisabledAlpha), visuals.indicators.first())
    }

    @Test
    fun indicators_areCappedSoACellNeverOverflows() {
        val many = listOf(holiday, festival, holiday, festival, holiday)

        assertEquals(
            MaxDayIndicators,
            resolve(NepaliDayDecoration(indicators = many)).indicators.size
        )
        assertEquals(
            MaxDualDateDayIndicators,
            resolve(
                NepaliDayDecoration(indicators = many),
                maxIndicators = MaxDualDateDayIndicators
            ).indicators.size
        )
    }

    @Test
    fun aCapOfZero_drawsNoDotsAtAll() {
        val visuals = resolve(
            NepaliDayDecoration(contentColor = holiday, indicators = listOf(holiday)),
            maxIndicators = 0
        )

        assertTrue(visuals.indicators.isEmpty())
        assertEquals(holiday, visuals.contentColor, "the colour survives the cap")
    }

    @Test
    fun exactlyAtTheCap_nothingIsDropped() {
        val three = listOf(holiday, festival, holiday)

        assertEquals(three, resolve(NepaliDayDecoration(indicators = three)).indicators)
    }

    @Test
    fun aTranslucentDotFadesFromWhereItAlreadyWas() {
        // Multiplicative, not a flat overwrite, so a dot an app already dimmed keeps its relative
        // weight. Alpha is stored to eight bits, so the expectation is quantized the same way.
        val half = holiday.copy(alpha = 0.5f)

        val faded = resolve(
            NepaliDayDecoration(indicators = listOf(half)),
            isEnabled = false
        ).indicators.single()

        assertEquals(half.copy(alpha = half.alpha * DisabledAlpha).alpha, faded.alpha)
        assertTrue(faded.alpha < half.alpha, "a disabled day's dot is dimmer than it was")
    }

    @Test
    fun selectedBeatsDisabledForTheDots() {
        val visuals = resolve(
            NepaliDayDecoration(indicators = listOf(holiday)),
            isSelected = true,
            isEnabled = false
        )

        assertEquals(listOf(onSelected), visuals.indicators, "a selected day reads as selected first")
        assertEquals(themeContent, visuals.contentColor)
    }

    @Test
    fun inRangeBeatsDisabledForTheDots() {
        val visuals = resolve(
            NepaliDayDecoration(indicators = listOf(holiday)),
            isInRange = true,
            isEnabled = false
        )

        assertEquals(listOf(onRange), visuals.indicators)
    }

    @Test
    fun aDecorationWithNothingInIt_changesNothing() {
        val visuals = resolve(NepaliDayDecoration())

        assertEquals(themeContainer, visuals.containerColor)
        assertEquals(themeContent, visuals.contentColor)
        assertTrue(visuals.indicators.isEmpty())
    }

    @Test
    fun indicators_keepTheirOrder() {
        val visuals = resolve(NepaliDayDecoration(indicators = listOf(holiday, festival)))

        assertEquals(listOf(holiday, festival), visuals.indicators)
    }
}
