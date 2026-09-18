/*
 * Copyright © 2026 Shiva Thapa (@shivathapaa). All rights reserved.
 *
 * Licensed under the Mozilla Public License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://mozilla.org/MPL/2.0/
 */

@file:OptIn(ExperimentalTestApi::class, ExperimentalNepaliDatePickerApi::class)

package dev.shivathapaa.nepalidatepickerkmp

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.runComposeUiTest
import dev.shivathapaa.nepalidatepickerkmp.annotations.ExperimentalNepaliDatePickerApi
import dev.shivathapaa.nepalidatepickerkmp.calendar_model.NepaliCalendarDefaults
import dev.shivathapaa.nepalidatepickerkmp.calendar_model.NepaliCalendarModel
import dev.shivathapaa.nepalidatepickerkmp.calendar_model.NepaliDateConverter
import dev.shivathapaa.nepalidatepickerkmp.data.CalendarSystem
import dev.shivathapaa.nepalidatepickerkmp.data.CustomCalendar
import dev.shivathapaa.nepalidatepickerkmp.data.NepaliDateFormatStyle
import dev.shivathapaa.nepalidatepickerkmp.data.NepaliDateLocale
import dev.shivathapaa.nepalidatepickerkmp.data.NepaliDatePickerLang
import dev.shivathapaa.nepalidatepickerkmp.data.NepaliMonthCalendar
import dev.shivathapaa.nepalidatepickerkmp.data.SimpleDate
import dev.shivathapaa.nepalidatepickerkmp.data.toSimpleDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

private val EnglishLocale = NepaliDateLocale(language = NepaliDatePickerLang.ENGLISH)
private val English = NepaliDatePickerLang.ENGLISH

class NepaliCalendarSystemSwitchTest {

    @Test
    fun switchingCalendar_keepsTheSelectedDate() = runComposeUiTest {
        lateinit var state: NepaliDatePickerState
        setContent {
            state = rememberNepaliDatePickerState(
                initialSelectedDate = SimpleDate(2083, 6, 1),
                locale = EnglishLocale
            )
            NepaliDatePicker(state = state, showCalendarSystemToggle = true)
        }

        val beforeSwitch = runOnIdle { state.selectedDate }
        onNodeWithContentDescription(English.switchToGregorianContentDescription).performClick()

        runOnIdle {
            assertEquals(CalendarSystem.GREGORIAN, state.displayedCalendarSystem)
            assertEquals(beforeSwitch, state.selectedDate, "the selection must survive the switch")
            // BS 2083-06-01 is AD 2026-09-17, so the grid follows the selection into September.
            assertEquals(2026, state.displayedMonthCalendar.year)
            assertEquals(9, state.displayedMonthCalendar.month)
        }

        onNodeWithContentDescription(English.switchToBikramSambatContentDescription).performClick()
        runOnIdle {
            assertEquals(CalendarSystem.BIKRAM_SAMBAT, state.displayedCalendarSystem)
            assertEquals(beforeSwitch, state.selectedDate, "switching back must not change it either")
            assertEquals(2083, state.displayedMonthCalendar.year)
            assertEquals(6, state.displayedMonthCalendar.month)
        }
    }

    @Test
    fun gregorianGrid_selectsTheEquivalentBikramSambatDate() = runComposeUiTest {
        lateinit var state: NepaliDatePickerState
        setContent {
            state = rememberNepaliDatePickerState(
                initialDisplayedMonth = SimpleDate(2083, 6, 1),
                locale = EnglishLocale,
                initialCalendarSystem = CalendarSystem.GREGORIAN
            )
            NepaliDatePicker(state = state)
        }

        // The grid shows September 2026; tapping the 17th must store BS 2083-06-01.
        onNodeWithText("17").performClick()
        runOnIdle {
            val expected = NepaliDateConverter.convertEnglishToNepali(2026, 9, 17)
            assertEquals(expected, state.selectedDate)
            assertEquals(2, state.selectedDate?.era, "selection is always stored in Bikram Sambat")
        }
    }

    @Test
    fun gregorianGrid_readsDisplayedMonthAsTheBikramSambatMonthOfItsFirstDay() = runComposeUiTest {
        lateinit var state: NepaliDatePickerState
        setContent {
            state = rememberNepaliDatePickerState(
                initialDisplayedMonth = SimpleDate(2083, 6, 1),
                locale = EnglishLocale,
                initialCalendarSystem = CalendarSystem.GREGORIAN
            )
            NepaliDatePicker(state = state)
        }

        runOnIdle {
            // September 1 2026 falls in Bhadra 2083, the month before the one that seeded the grid.
            val firstDay = NepaliDateConverter.convertEnglishToNepali(2026, 9, 1)
            assertEquals(firstDay.year, state.displayedMonth.year)
            assertEquals(firstDay.month, state.displayedMonth.month)
            assertEquals(CalendarSystem.GREGORIAN, state.displayedMonthCalendar.calendarSystem)
        }
    }

    @Test
    fun toggleIsHiddenByDefault() = runComposeUiTest {
        setContent {
            NepaliDatePicker(state = rememberNepaliDatePickerState(locale = EnglishLocale))
        }
        onNodeWithContentDescription(English.switchToGregorianContentDescription).assertDoesNotExist()
        onNodeWithContentDescription(English.switchToBikramSambatContentDescription)
            .assertDoesNotExist()
    }

    @Test
    fun toggleIsShownWhenAskedFor() = runComposeUiTest {
        setContent {
            NepaliDatePicker(
                state = rememberNepaliDatePickerState(locale = EnglishLocale),
                showCalendarSystemToggle = true
            )
        }
        onNodeWithContentDescription(English.switchToGregorianContentDescription).assertIsDisplayed()
        onNodeWithContentDescription(English.switchToBikramSambatContentDescription)
            .assertIsDisplayed()
    }

    @Test
    fun englishYearRange_isDerivedFromTheNepaliOne() = runComposeUiTest {
        lateinit var state: NepaliDatePickerState
        setContent {
            state = rememberNepaliDatePickerState(locale = EnglishLocale)
        }
        runOnIdle {
            assertEquals(NepaliCalendarDefaults.EnglishYearRange, state.englishYearRange)
        }
    }

    @Test
    fun rangePicker_switchingCalendar_keepsTheSelectedRange() = runComposeUiTest {
        lateinit var state: NepaliDateRangePickerState
        setContent {
            state = rememberNepaliDateRangePickerState(
                initialSelectedStartNepaliDate = SimpleDate(2083, 6, 1),
                initialSelectedEndNepaliDate = SimpleDate(2083, 6, 10),
                locale = EnglishLocale
            )
            NepaliDateRangePicker(
                state = state,
                showMonthsVertically = false,
                showCalendarSystemToggle = true
            )
        }

        val startBefore = runOnIdle { state.selectedStartNepaliDate }
        val endBefore = runOnIdle { state.selectedEndNepaliDate }

        onNodeWithContentDescription(English.switchToGregorianContentDescription).performClick()
        runOnIdle {
            assertEquals(CalendarSystem.GREGORIAN, state.displayedCalendarSystem)
            assertEquals(startBefore, state.selectedStartNepaliDate)
            assertEquals(endBefore, state.selectedEndNepaliDate)
        }
    }

    @Test
    fun gregorianGrid_daysBeforeTheConversionAnchorAreNotSelectable() = runComposeUiTest {
        lateinit var state: NepaliDatePickerState
        setContent {
            state = rememberNepaliDatePickerState(
                // The Bikram Sambat table starts on AD 1913-04-13, so most of that April is inert.
                initialDisplayedMonth = SimpleDate(1970, 1, 1),
                locale = EnglishLocale,
                initialCalendarSystem = CalendarSystem.GREGORIAN
            )
            NepaliDatePicker(state = state, showTodayButton = false)
        }

        runOnIdle {
            assertEquals(1913, state.displayedMonthCalendar.year)
            assertEquals(4, state.displayedMonthCalendar.month)
        }

        // Day 1 predates the anchor: clicking it must leave the selection untouched.
        onNodeWithText("1").performClick()
        runOnIdle { assertEquals(null, state.selectedDate) }

        // Day 13 is the anchor itself and maps to the first supported Bikram Sambat date.
        onNodeWithText("13").performClick()
        runOnIdle {
            assertEquals(1970, state.selectedDate?.year)
            assertEquals(1, state.selectedDate?.month)
            assertEquals(1, state.selectedDate?.dayOfMonth)
        }
    }

    @Test
    fun gregorianGrid_showsGregorianMonthLengths() = runComposeUiTest {
        lateinit var state: NepaliDatePickerState
        setContent {
            state = rememberNepaliDatePickerState(
                // BS 2081-10-19 is AD 2025-02-01, so the grid opens on a 28-day February.
                initialDisplayedMonth = SimpleDate(2081, 10, 19),
                locale = EnglishLocale,
                initialCalendarSystem = CalendarSystem.GREGORIAN
            )
            NepaliDatePicker(state = state)
        }
        runOnIdle {
            assertEquals(2025, state.displayedMonthCalendar.year)
            assertEquals(2, state.displayedMonthCalendar.month)
            assertEquals(28, state.displayedMonthCalendar.totalDaysInMonth)
            // A Bikram Sambat month never has 28 days, so this could only come from the other grid.
            assertNotEquals(
                state.displayedMonth.totalDaysInMonth,
                state.displayedMonthCalendar.totalDaysInMonth
            )
        }
    }

    @Test
    fun wheelPicker_reportsBikramSambatWhateverItSpins() = runComposeUiTest {
        var reported: SimpleDate? = null
        setContent {
            NepaliWheelDatePicker(
                initialDate = SimpleDate(2083, 6, 1),
                locale = EnglishLocale,
                initialCalendarSystem = CalendarSystem.GREGORIAN,
                onDateChange = { reported = SimpleDate(it.year, it.month, it.dayOfMonth) }
            )
        }
        runOnIdle {
            assertEquals(SimpleDate(2083, 6, 1), reported)
        }
    }

    @Test
    fun textField_typedGregorianDateIsReportedInBikramSambat() = runComposeUiTest {
        var reported: SimpleDate? = null
        setContent {
            NepaliDateTextField(
                value = null,
                onValueChange = { reported = it },
                locale = EnglishLocale,
                calendarSystem = CalendarSystem.GREGORIAN
            )
        }
        onNodeWithText("YYYY/MM/DD").performTextInput("20260917")
        runOnIdle {
            assertEquals(SimpleDate(2083, 6, 1), reported)
        }
    }

    @Test
    fun textField_bikramSambatPathIsUnchanged() = runComposeUiTest {
        var reported: SimpleDate? = null
        setContent {
            NepaliDateTextField(
                value = null,
                onValueChange = { reported = it },
                locale = EnglishLocale
            )
        }
        onNodeWithText("YYYY/MM/DD").performTextInput("20830601")
        runOnIdle {
            assertEquals(SimpleDate(2083, 6, 1), reported)
        }
    }

    @Test
    fun gregorianYearPicker_offersEnglishYears() = runComposeUiTest {
        setContent {
            NepaliDatePicker(
                state = rememberNepaliDatePickerState(
                    initialDisplayedMonth = SimpleDate(2083, 6, 1),
                    locale = EnglishLocale,
                    initialCalendarSystem = CalendarSystem.GREGORIAN
                ),
                showTodayButton = false
            )
        }
        // The year button reads the Gregorian month and year while that calendar is displayed.
        onNodeWithText("September 2026").assertIsDisplayed()
    }

    @Test
    fun bikramSambatGrid_keepsItsOwnYearLabel() = runComposeUiTest {
        setContent {
            NepaliDatePicker(
                state = rememberNepaliDatePickerState(
                    initialDisplayedMonth = SimpleDate(2083, 6, 1),
                    locale = EnglishLocale
                ),
                showTodayButton = false
            )
        }
        onNodeWithText("Asoj 2083").assertIsDisplayed()
    }

    @Test
    fun secondaryLabel_namesTheStraddledMonthsOfTheOtherCalendar() = runComposeUiTest {
        setContent {
            NepaliDatePicker(
                state = rememberNepaliDatePickerState(
                    initialDisplayedMonth = SimpleDate(2083, 6, 1),
                    locale = EnglishLocale
                ),
                secondaryDateLocale = EnglishLocale,
                showTodayButton = false
            )
        }
        // Asoj 2083 runs from mid-September into mid-October 2026.
        onNodeWithText("Sep/Oct 2026").assertIsDisplayed()
    }

    @Test
    fun secondaryLabel_readsTheOtherWayRoundInAGregorianGrid() = runComposeUiTest {
        setContent {
            NepaliDatePicker(
                state = rememberNepaliDatePickerState(
                    initialDisplayedMonth = SimpleDate(2083, 6, 1),
                    locale = EnglishLocale,
                    initialCalendarSystem = CalendarSystem.GREGORIAN
                ),
                secondaryDateLocale = EnglishLocale,
                showTodayButton = false
            )
        }
        // September 2026 runs from mid-Bhadra into mid-Asoj 2083.
        onNodeWithText("Bha/Aso 2083").assertIsDisplayed()
    }

    @Test
    fun title_namesTheCalendarOnScreen() = runComposeUiTest {
        setContent {
            NepaliDatePicker(
                state = rememberNepaliDatePickerState(locale = EnglishLocale),
                showCalendarSystemToggle = true
            )
        }

        onNodeWithText(English.datePickerTitle).assertIsDisplayed()

        onNodeWithContentDescription(English.switchToGregorianContentDescription).performClick()
        onNodeWithText(English.englishDatePickerTitle).assertIsDisplayed()
        onNodeWithText(English.datePickerTitle).assertDoesNotExist()

        onNodeWithContentDescription(English.switchToBikramSambatContentDescription).performClick()
        onNodeWithText(English.datePickerTitle).assertIsDisplayed()
    }

    @Test
    fun title_namesTheCalendarInInputModeToo() = runComposeUiTest {
        setContent {
            NepaliDatePicker(
                state = rememberNepaliDatePickerState(
                    locale = EnglishLocale,
                    initialDisplayMode = DisplayMode.Input,
                    initialCalendarSystem = CalendarSystem.GREGORIAN
                )
            )
        }
        onNodeWithText(English.englishDateInputTitle).assertIsDisplayed()
    }

    @Test
    fun inputMode_typedDateIsRewrittenWhenTheCalendarSwitches() = runComposeUiTest {
        lateinit var state: NepaliDatePickerState
        setContent {
            state = rememberNepaliDatePickerState(
                initialSelectedDate = SimpleDate(2083, 6, 1),
                initialDisplayedMonth = SimpleDate(2083, 6),
                initialDisplayMode = DisplayMode.Input,
                locale = EnglishLocale
            )
            NepaliDatePicker(state = state, showCalendarSystemToggle = true)
        }

        onNodeWithText("2083/06/01").assertIsDisplayed()

        onNodeWithContentDescription(English.switchToGregorianContentDescription).performClick()

        // BS 2083-06-01 is AD 2026-09-17; the field types the calendar on screen.
        onNodeWithText("2026/09/17").assertIsDisplayed()
        onNodeWithText("2083/06/01").assertDoesNotExist()
        runOnIdle { assertEquals(SimpleDate(2083, 6, 1), state.selectedDate?.toSimpleDate()) }

        onNodeWithContentDescription(English.switchToBikramSambatContentDescription).performClick()

        onNodeWithText("2083/06/01").assertIsDisplayed()
        runOnIdle { assertEquals(SimpleDate(2083, 6, 1), state.selectedDate?.toSimpleDate()) }
    }

    @Test
    fun rangeInputMode_bothTypedDatesAreRewrittenWhenTheCalendarSwitches() = runComposeUiTest {
        lateinit var state: NepaliDateRangePickerState
        setContent {
            state = rememberNepaliDateRangePickerState(
                initialSelectedStartNepaliDate = SimpleDate(2083, 6, 1),
                initialSelectedEndNepaliDate = SimpleDate(2083, 6, 10),
                initialDisplayedMonth = SimpleDate(2083, 6),
                initialDisplayMode = DisplayMode.Input,
                locale = EnglishLocale
            )
            NepaliDateRangePicker(
                state = state,
                showMonthsVertically = false,
                showCalendarSystemToggle = true
            )
        }

        onNodeWithText("2083/06/01").assertIsDisplayed()
        onNodeWithText("2083/06/10").assertIsDisplayed()

        onNodeWithContentDescription(English.switchToGregorianContentDescription).performClick()

        // BS 2083-06-01 is AD 2026-09-17 and BS 2083-06-10 is AD 2026-09-26.
        onNodeWithText("2026/09/17").assertIsDisplayed()
        onNodeWithText("2026/09/26").assertIsDisplayed()
        runOnIdle {
            assertEquals(SimpleDate(2083, 6, 1), state.selectedStartNepaliDate?.toSimpleDate())
            assertEquals(SimpleDate(2083, 6, 10), state.selectedEndNepaliDate?.toSimpleDate())
        }
    }

    @Test
    fun inputMode_typingIsNotDisturbedWhileTheDateIsIncomplete() = runComposeUiTest {
        lateinit var state: NepaliDatePickerState
        setContent {
            state = rememberNepaliDatePickerState(
                initialSelectedDate = SimpleDate(2083, 6, 1),
                initialDisplayMode = DisplayMode.Input,
                locale = EnglishLocale
            )
            NepaliDatePicker(state = state, showCalendarSystemToggle = true)
        }

        // Clearing to a partial entry drops the selection; the field must keep the keystrokes.
        onNode(hasSetTextAction()).performTextClearance()
        onNode(hasSetTextAction()).performTextInput("2083")
        runOnIdle { assertEquals(null, state.selectedDate) }
        onNodeWithText("2083").assertIsDisplayed()
    }

    @Test
    fun inputMode_switchingAwayFromAPartialEntryFallsBackToNoDate() = runComposeUiTest {
        lateinit var state: NepaliDatePickerState
        setContent {
            state = rememberNepaliDatePickerState(
                initialDisplayMode = DisplayMode.Input,
                locale = EnglishLocale
            )
            NepaliDatePicker(state = state, showCalendarSystemToggle = true)
        }

        onNode(hasSetTextAction()).performTextInput("2083")

        onNodeWithContentDescription(English.switchToGregorianContentDescription).performClick()

        // Nothing was selected, so there is no day to rewrite: the field starts clean rather than
        // keeping Bikram Sambat digits under a Gregorian label.
        onNodeWithText("2083").assertDoesNotExist()
        onNodeWithText(English.englishDate).assertIsDisplayed()
        runOnIdle { assertEquals(null, state.selectedDate) }
    }

    @Test
    fun inputMode_switchingClearsAnErrorAboutWhatWasTyped() = runComposeUiTest {
        setContent {
            NepaliDatePicker(
                state = rememberNepaliDatePickerState(
                    initialDisplayMode = DisplayMode.Input,
                    locale = EnglishLocale
                ),
                showCalendarSystemToggle = true
            )
        }

        // Baisakh has no 32nd day in 2083, so this reports an invalid day.
        onNode(hasSetTextAction()).performTextInput("20830132")
        onNodeWithText(English.errorInvalidDay).assertIsDisplayed()

        onNodeWithContentDescription(English.switchToGregorianContentDescription).performClick()

        onNodeWithText(English.errorInvalidDay).assertDoesNotExist()
    }

    @Test
    fun dockedField_followsTheCalendarItsDropdownShows() = runComposeUiTest {
        lateinit var state: NepaliDatePickerState
        setContent {
            state = rememberNepaliDatePickerState(
                initialSelectedDate = SimpleDate(2083, 6, 1),
                locale = EnglishLocale
            )
            NepaliDatePickerDocked(state = state, showCalendarSystemToggle = true)
        }

        val model = NepaliCalendarModel(EnglishLocale.copy(dateFormat = NepaliDateFormatStyle.MEDIUM))
        val nepaliText = model.formatNepaliDate(
            NepaliDateConverter.getNepaliCalendar(2083, 6, 1),
            EnglishLocale.copy(dateFormat = NepaliDateFormatStyle.MEDIUM)
        )
        onNodeWithText(nepaliText).assertIsDisplayed()

        // The dropdown has to be open for the switch to be reachable.
        onNodeWithContentDescription(English.switchToCalendarModeContentDescription).performClick()
        onNodeWithContentDescription(English.switchToGregorianContentDescription).performClick()

        // BS 2083-06-01 is AD 2026-09-17; the field is this variant's headline, so it follows.
        val englishText = model.formatEnglishDate(
            year = 2026, month = 9, dayOfMonth = 17,
            dayOfWeek = NepaliDateConverter.getEnglishCalendar(2026, 9, 17).dayOfWeek,
            locale = EnglishLocale.copy(dateFormat = NepaliDateFormatStyle.MEDIUM)
        )
        onNodeWithText(englishText).assertIsDisplayed()
        onNodeWithText(nepaliText).assertDoesNotExist()
        runOnIdle { assertEquals(SimpleDate(2083, 6, 1), state.selectedDate?.toSimpleDate()) }
    }

    @Test
    fun rangeTitle_namesTheCalendarOnScreen() = runComposeUiTest {
        setContent {
            NepaliDateRangePicker(
                state = rememberNepaliDateRangePickerState(
                    locale = EnglishLocale,
                    initialCalendarSystem = CalendarSystem.GREGORIAN
                ),
                showMonthsVertically = false
            )
        }
        onNodeWithText(English.englishDateRangePickerTitle).assertIsDisplayed()
        onNodeWithText(English.dateRangePickerTitle).assertDoesNotExist()
    }

    @Test
    fun headline_followsTheCalendarWhileTheSelectionStays() = runComposeUiTest {
        lateinit var state: NepaliDatePickerState
        setContent {
            state = rememberNepaliDatePickerState(
                initialSelectedDate = SimpleDate(2083, 6, 1),
                locale = EnglishLocale
            )
            NepaliDatePicker(state = state, showCalendarSystemToggle = true)
        }

        // BS 2083-06-01 is AD 2026-09-17; the default LONG format writes them like this.
        onNodeWithText("Asoj 1, 2083").assertIsDisplayed()

        onNodeWithContentDescription(English.switchToGregorianContentDescription).performClick()
        onNodeWithText("September 17, 2026").assertIsDisplayed()
        runOnIdle {
            assertEquals(2083, state.selectedDate?.year)
            assertEquals(6, state.selectedDate?.month)
            assertEquals(1, state.selectedDate?.dayOfMonth)
        }

        onNodeWithContentDescription(English.switchToBikramSambatContentDescription).performClick()
        onNodeWithText("Asoj 1, 2083").assertIsDisplayed()
    }

    /** The two-line headline is what NepaliDatePickerWithEnglishDate brings over the plain picker. */
    @Test
    fun dualDateHeadline_swapsWhichCalendarLeads() = runComposeUiTest {
        setContent {
            NepaliDatePickerWithEnglishDate(
                state = rememberNepaliDatePickerState(
                    initialSelectedDate = SimpleDate(2083, 6, 1),
                    locale = EnglishLocale,
                    initialCalendarSystem = CalendarSystem.GREGORIAN
                ),
                englishDateLocale = EnglishLocale
            )
        }
        // Both halves are on screen; the Gregorian one now leads.
        onNodeWithText("September 17, 2026").assertIsDisplayed()
        onNodeWithText("Asoj 1, 2083").assertIsDisplayed()
    }

    @Test
    fun theSwitchRidesInTheHeaderNotTheMonthNavigation() = runComposeUiTest {
        setContent {
            NepaliDatePicker(
                state = rememberNepaliDatePickerState(locale = EnglishLocale),
                showCalendarSystemToggle = true
            )
        }
        // The switch shares the title's row, so it sits above the year button rather than below it.
        val titleTop = onNodeWithText(English.datePickerTitle)
            .fetchSemanticsNode().boundsInRoot.top
        val switchTop = onNodeWithContentDescription(English.switchToGregorianContentDescription)
            .fetchSemanticsNode().boundsInRoot.top
        val yearButtonTop = onNodeWithText("Asoj 2083", substring = true)
            .fetchSemanticsNode().boundsInRoot.top

        assertTrue(
            switchTop < yearButtonTop,
            "the switch should sit in the header, above the month navigation"
        )
        assertTrue(
            switchTop >= titleTop - TitleRowSlackPx,
            "the switch should share the title's row rather than owning one of its own"
        )
    }

    /**
     * Guards the regression that a Material3 `Surface(selected, onClick)` reintroduces: it applies
     * `minimumInteractiveComponentSize()`, which grows the segment's layout box to 48dp and pads the
     * header out with dead space. Compared against a day cell rather than a hardcoded pixel count so
     * the assertion holds at any density.
     */
    @Test
    fun theSwitchIsShorterThanADayCell() = runComposeUiTest {
        setContent {
            NepaliDatePicker(
                state = rememberNepaliDatePickerState(
                    initialDisplayedMonth = SimpleDate(2081, 5),
                    locale = EnglishLocale
                ),
                showCalendarSystemToggle = true
            )
        }
        val dayCellHeight = onNodeWithText("15").fetchSemanticsNode().size.height
        val segmentHeight = onNodeWithContentDescription(English.switchToGregorianContentDescription)
            .fetchSemanticsNode().size.height

        assertTrue(
            segmentHeight < dayCellHeight,
            "the switch segment ($segmentHeight) should lay out smaller than a day cell " +
                    "($dayCellHeight); a 48dp minimum touch target would make it taller"
        )
    }

    @Test
    fun dockedPicker_stillOffersTheSwitchWithoutATitle() = runComposeUiTest {
        setContent {
            NepaliDatePickerDocked(
                state = rememberNepaliDatePickerState(locale = EnglishLocale),
                showCalendarSystemToggle = true
            )
        }
        onNodeWithContentDescription(English.switchToCalendarModeContentDescription).performClick()
        onNodeWithContentDescription(English.switchToGregorianContentDescription).assertIsDisplayed()
    }

    /**
     * A Gregorian month spans two Bikram Sambat months, so projecting it back onto "the Bikram
     * Sambat month of its first day" loses a month. Re-deriving the switch anchor from that
     * projection made every round trip walk one month backwards.
     */
    @Test
    fun repeatedSwitchingWithoutASelection_staysOnTheSameMonth() = runComposeUiTest {
        lateinit var state: NepaliDatePickerState
        setContent {
            state = rememberNepaliDatePickerState(
                initialDisplayedMonth = SimpleDate(2083, 6, 1),
                locale = EnglishLocale
            )
            NepaliDatePicker(state = state, showCalendarSystemToggle = true)
        }

        val startNepaliMonth = runOnIdle { state.displayedMonth.year to state.displayedMonth.month }
        var gregorianMonth: Pair<Int, Int>? = null

        repeat(3) { round ->
            onNodeWithContentDescription(English.switchToGregorianContentDescription).performClick()
            waitForIdle()
            runOnIdle {
                val shown = state.displayedMonthCalendar.year to state.displayedMonthCalendar.month
                if (gregorianMonth == null) gregorianMonth = shown
                assertEquals(gregorianMonth, shown, "Gregorian month drifted on round ${round + 1}")
            }

            onNodeWithContentDescription(English.switchToBikramSambatContentDescription)
                .performClick()
            waitForIdle()
            runOnIdle {
                assertEquals(
                    startNepaliMonth,
                    state.displayedMonth.year to state.displayedMonth.month,
                    "Bikram Sambat month drifted on round ${round + 1}"
                )
            }
        }
    }

    @Test
    fun repeatedSwitchingWithASelection_staysOnTheSameMonth() = runComposeUiTest {
        lateinit var state: NepaliDatePickerState
        setContent {
            state = rememberNepaliDatePickerState(
                initialSelectedDate = SimpleDate(2083, 6, 1),
                locale = EnglishLocale
            )
            NepaliDatePicker(state = state, showCalendarSystemToggle = true)
        }

        val startNepaliMonth = runOnIdle { state.displayedMonth.year to state.displayedMonth.month }

        repeat(3) { round ->
            onNodeWithContentDescription(English.switchToGregorianContentDescription).performClick()
            waitForIdle()
            onNodeWithContentDescription(English.switchToBikramSambatContentDescription)
                .performClick()
            waitForIdle()
            runOnIdle {
                assertEquals(
                    startNepaliMonth,
                    state.displayedMonth.year to state.displayedMonth.month,
                    "month drifted on round ${round + 1}"
                )
                assertEquals(SimpleDate(2083, 6, 1), state.selectedDate?.toSimpleDate())
            }
        }
    }

    @Test
    fun repeatedSwitchingInTheRangePicker_staysOnTheSameMonth() = runComposeUiTest {
        lateinit var state: NepaliDateRangePickerState
        setContent {
            state = rememberNepaliDateRangePickerState(
                initialDisplayedMonth = SimpleDate(2083, 6, 1),
                locale = EnglishLocale
            )
            NepaliDateRangePicker(
                state = state,
                showMonthsVertically = false,
                showCalendarSystemToggle = true
            )
        }

        val startNepaliMonth = runOnIdle { state.displayedMonth.year to state.displayedMonth.month }

        repeat(3) { round ->
            onNodeWithContentDescription(English.switchToGregorianContentDescription).performClick()
            waitForIdle()
            onNodeWithContentDescription(English.switchToBikramSambatContentDescription)
                .performClick()
            waitForIdle()
            runOnIdle {
                assertEquals(
                    startNepaliMonth,
                    state.displayedMonth.year to state.displayedMonth.month,
                    "range picker month drifted on round ${round + 1}"
                )
            }
        }
    }

    @Test
    fun switchingAfterPagingKeepsTheMonthThatWasPagedTo() = runComposeUiTest {
        lateinit var state: NepaliDatePickerState
        setContent {
            state = rememberNepaliDatePickerState(
                initialDisplayedMonth = SimpleDate(2083, 6, 1),
                locale = EnglishLocale
            )
            NepaliDatePicker(state = state, showCalendarSystemToggle = true)
        }

        repeat(2) {
            onNodeWithContentDescription(English.nextMonthContentDescription).performClick()
            waitForIdle()
        }
        val pagedTo = runOnIdle { state.displayedMonth.year to state.displayedMonth.month }
        assertEquals(2083 to 8, pagedTo, "two taps forward from Asoj should reach Mangsir")

        onNodeWithContentDescription(English.switchToGregorianContentDescription).performClick()
        waitForIdle()
        onNodeWithContentDescription(English.switchToBikramSambatContentDescription).performClick()
        waitForIdle()

        runOnIdle {
            assertEquals(
                pagedTo,
                state.displayedMonth.year to state.displayedMonth.month,
                "a switch round trip must not undo the paging"
            )
        }
    }

    @Test
    fun switchingToInputModeKeepsTheMonthHoldingTheSelection() = runComposeUiTest {
        lateinit var state: NepaliDatePickerState
        setContent {
            // BS 2083-06-20 is AD 2026-10-06, a different Gregorian month from BS 2083-06-01.
            state = rememberNepaliDatePickerState(
                initialSelectedDate = SimpleDate(2083, 6, 20),
                locale = EnglishLocale,
                initialCalendarSystem = CalendarSystem.GREGORIAN
            )
            NepaliDatePicker(state = state)
        }

        runOnIdle {
            assertEquals(2026 to 10, state.displayedMonthCalendar.year to state.displayedMonthCalendar.month)
            state.displayMode = DisplayMode.Input
        }
        waitForIdle()
        runOnIdle {
            assertEquals(
                2026 to 10,
                state.displayedMonthCalendar.year to state.displayedMonthCalendar.month,
                "the mode switch must stay on the month the selection falls in"
            )
        }
    }

    @Test
    fun switchingAtTheStartOfTheSupportedRangeStaysPut() = runComposeUiTest {
        lateinit var state: NepaliDatePickerState
        setContent {
            state = rememberNepaliDatePickerState(
                initialDisplayedMonth = SimpleDate(1970, 1, 1),
                locale = EnglishLocale
            )
            NepaliDatePicker(state = state, showCalendarSystemToggle = true)
        }

        repeat(2) {
            onNodeWithContentDescription(English.switchToGregorianContentDescription).performClick()
            waitForIdle()
            runOnIdle {
                // BS 1970-01-01 is AD 1913-04-13, so the Gregorian grid opens on that April.
                assertEquals(
                    1913 to 4,
                    state.displayedMonthCalendar.year to state.displayedMonthCalendar.month
                )
            }
            onNodeWithContentDescription(English.switchToBikramSambatContentDescription)
                .performClick()
            waitForIdle()
            runOnIdle {
                assertEquals(1970 to 1, state.displayedMonth.year to state.displayedMonth.month)
            }
        }
    }

    @Test
    fun switchingAtTheEndOfTheSupportedRangeStaysInRange() = runComposeUiTest {
        lateinit var state: NepaliDatePickerState
        setContent {
            // The Bikram Sambat table outlives the convertible Gregorian range, so this month has
            // no Gregorian counterpart the pager can show. It must clamp, not throw or escape.
            state = rememberNepaliDatePickerState(
                initialDisplayedMonth = SimpleDate(2100, 12, 1),
                locale = EnglishLocale
            )
            NepaliDatePicker(state = state, showCalendarSystemToggle = true)
        }

        repeat(2) {
            onNodeWithContentDescription(English.switchToGregorianContentDescription).performClick()
            waitForIdle()
            runOnIdle {
                assertTrue(
                    state.displayedMonthCalendar.year in state.englishYearRange,
                    "Gregorian month ${state.displayedMonthCalendar.year} escaped ${state.englishYearRange}"
                )
            }
            onNodeWithContentDescription(English.switchToBikramSambatContentDescription)
                .performClick()
            waitForIdle()
            runOnIdle {
                assertTrue(
                    state.displayedMonth.year in NepaliCalendarDefaults.NepaliYearRange,
                    "Bikram Sambat month ${state.displayedMonth.year} escaped the supported range"
                )
            }
        }
    }

    /**
     * The grid pickers coerce a caller's out-of-range date through their state; the wheel has no
     * state holder, so the adapter has to survive one reaching it directly.
     */
    @Test
    fun wheelPicker_outOfRangeInitialDateInGregorianDoesNotCrash() = runComposeUiTest {
        var reported: SimpleDate? = null
        setContent {
            NepaliWheelDatePicker(
                initialDate = SimpleDate(1500, 1, 1),
                locale = EnglishLocale,
                initialCalendarSystem = CalendarSystem.GREGORIAN,
                onDateChange = { reported = SimpleDate(it.year, it.month, it.dayOfMonth) }
            )
        }
        runOnIdle {
            val seen = reported
            assertTrue(
                seen == null || seen.year in NepaliCalendarDefaults.NepaliYearRange,
                "the wheel reported $seen, outside the supported Bikram Sambat range"
            )
        }
    }

    @Test
    fun wheelPicker_outOfRangeInitialDateInBikramSambatDoesNotCrash() = runComposeUiTest {
        var reported: SimpleDate? = null
        setContent {
            NepaliWheelDatePicker(
                initialDate = SimpleDate(3000, 13, 40),
                locale = EnglishLocale,
                onDateChange = { reported = SimpleDate(it.year, it.month, it.dayOfMonth) }
            )
        }
        runOnIdle {
            val seen = reported
            assertTrue(
                seen == null || seen.year in NepaliCalendarDefaults.NepaliYearRange,
                "the wheel reported $seen, outside the supported Bikram Sambat range"
            )
        }
    }

    @Test
    fun wheelPicker_neverReportsADateOutsideTheTable() = runComposeUiTest {
        val reported = mutableListOf<SimpleDate>()
        setContent {
            NepaliWheelDatePicker(
                // AD 1913-04-13 is the earliest convertible day, so this wheel starts on the very
                // edge, where a step backwards has no Bikram Sambat date behind it.
                initialDate = SimpleDate(1970, 1, 1),
                locale = EnglishLocale,
                initialCalendarSystem = CalendarSystem.GREGORIAN,
                showCalendarSystemToggle = true,
                onDateChange = { reported += SimpleDate(it.year, it.month, it.dayOfMonth) }
            )
        }
        waitForIdle()
        onNodeWithContentDescription(English.switchToBikramSambatContentDescription).performClick()
        waitForIdle()

        runOnIdle {
            assertTrue(reported.isNotEmpty(), "the wheel should have reported at least one date")
            reported.forEach { date ->
                assertTrue(
                    date.year in NepaliCalendarDefaults.NepaliYearRange,
                    "the wheel reported $date, outside the supported Bikram Sambat range"
                )
                assertEquals(
                    date,
                    NepaliDateConverter.getNepaliCalendar(date.year, date.month, date.dayOfMonth)
                        .toSimpleDate(),
                    "the wheel reported $date, which is not a real Bikram Sambat day"
                )
            }
        }
    }

    /**
     * The pager builds one item per month of the configured range and the year picker one cell per
     * year, so a range reaching past the conversion table used to throw the moment either was laid
     * out. The adapter clamps it instead.
     */
    @Test
    fun outOfRangeYearRangeDoesNotCrashEitherCalendar() = runComposeUiTest {
        lateinit var state: NepaliDatePickerState
        setContent {
            state = rememberNepaliDatePickerState(
                yearRange = IntRange(1500, 3000),
                locale = EnglishLocale
            )
            NepaliDatePicker(state = state, showCalendarSystemToggle = true)
        }
        waitForIdle()
        onNodeWithContentDescription(English.switchToGregorianContentDescription).performClick()
        waitForIdle()
        onNodeWithContentDescription(English.switchToBikramSambatContentDescription).performClick()
        waitForIdle()

        runOnIdle {
            assertTrue(state.displayedMonth.year in NepaliCalendarDefaults.NepaliYearRange)
        }
    }

    @Test
    fun wheelPicker_outOfRangeYearRangeDoesNotCrash() = runComposeUiTest {
        var reported: SimpleDate? = null
        setContent {
            NepaliWheelDatePicker(
                yearRange = IntRange(1500, 3000),
                locale = EnglishLocale,
                initialCalendarSystem = CalendarSystem.GREGORIAN,
                onDateChange = { reported = SimpleDate(it.year, it.month, it.dayOfMonth) }
            )
        }
        runOnIdle {
            val seen = reported
            assertTrue(seen == null || seen.year in NepaliCalendarDefaults.NepaliYearRange)
        }
    }

    /**
     * Bikram Sambat months run to 32 days and Gregorian ones never do, so the last day of a long
     * month is the one most likely to be clamped away by a round trip.
     */
    @Test
    fun theLastDayOfA32DayMonthSurvivesSwitching() = runComposeUiTest {
        val longMonth = SimpleDate(2083, 3, 32)
        assertEquals(
            32,
            NepaliDateConverter.getTotalDaysInNepaliMonth(longMonth.year, longMonth.month),
            "this test needs a 32-day month to be meaningful"
        )

        lateinit var state: NepaliDatePickerState
        setContent {
            state = rememberNepaliDatePickerState(
                initialSelectedDate = longMonth,
                locale = EnglishLocale
            )
            NepaliDatePicker(state = state, showCalendarSystemToggle = true)
        }

        repeat(2) { round ->
            onNodeWithContentDescription(English.switchToGregorianContentDescription).performClick()
            waitForIdle()
            runOnIdle {
                assertEquals(
                    longMonth,
                    state.selectedDate?.toSimpleDate(),
                    "day 32 was lost going to Gregorian on round ${round + 1}"
                )
            }
            onNodeWithContentDescription(English.switchToBikramSambatContentDescription)
                .performClick()
            waitForIdle()
            runOnIdle {
                assertEquals(longMonth, state.selectedDate?.toSimpleDate())
                assertEquals(longMonth.year to longMonth.month, state.displayedMonth.year to state.displayedMonth.month)
            }
        }
    }

    @Test
    fun wheelPicker_reportsDayThirtyTwoUnchangedAcrossASwitch() = runComposeUiTest {
        val longMonth = SimpleDate(2083, 3, 32)
        var reported: SimpleDate? = null
        setContent {
            NepaliWheelDatePicker(
                initialDate = longMonth,
                locale = EnglishLocale,
                showCalendarSystemToggle = true,
                onDateChange = { reported = SimpleDate(it.year, it.month, it.dayOfMonth) }
            )
        }
        waitForIdle()
        runOnIdle { assertEquals(longMonth, reported) }

        onNodeWithContentDescription(English.switchToGregorianContentDescription).performClick()
        waitForIdle()
        runOnIdle {
            assertEquals(longMonth, reported, "the Gregorian wheel lost day 32 of the month")
        }

        onNodeWithContentDescription(English.switchToBikramSambatContentDescription).performClick()
        waitForIdle()
        runOnIdle { assertEquals(longMonth, reported) }
    }

    @Test
    fun aRangeSpanningAGregorianMonthBoundarySurvivesSwitching() = runComposeUiTest {
        // BS 2083-06-01 is AD 2026-09-17 and BS 2083-06-20 is AD 2026-10-06, so in a Gregorian grid
        // this range starts mid-September and runs off the end of the month.
        val start = SimpleDate(2083, 6, 1)
        val end = SimpleDate(2083, 6, 20)

        lateinit var state: NepaliDateRangePickerState
        setContent {
            state = rememberNepaliDateRangePickerState(
                initialSelectedStartNepaliDate = start,
                initialSelectedEndNepaliDate = end,
                locale = EnglishLocale
            )
            NepaliDateRangePicker(
                state = state,
                showMonthsVertically = false,
                showCalendarSystemToggle = true
            )
        }

        repeat(2) {
            onNodeWithContentDescription(English.switchToGregorianContentDescription).performClick()
            waitForIdle()
            runOnIdle {
                assertEquals(start, state.selectedStartNepaliDate?.toSimpleDate())
                assertEquals(end, state.selectedEndNepaliDate?.toSimpleDate())
            }
            onNodeWithContentDescription(English.switchToBikramSambatContentDescription)
                .performClick()
            waitForIdle()
            runOnIdle {
                assertEquals(start, state.selectedStartNepaliDate?.toSimpleDate())
                assertEquals(end, state.selectedEndNepaliDate?.toSimpleDate())
            }
        }
    }

    @Test
    fun aSelectionPastTheConvertibleGregorianRangeSurvivesSwitching() = runComposeUiTest {
        // BS 2100-12-30 lands in AD 2044, past what the converter accepts, so the grid has to clamp
        // the month it shows without disturbing the selection.
        val nearTableEnd = SimpleDate(2100, 12, 30)

        lateinit var state: NepaliDatePickerState
        setContent {
            state = rememberNepaliDatePickerState(
                initialSelectedDate = nearTableEnd,
                locale = EnglishLocale
            )
            NepaliDatePicker(state = state, showCalendarSystemToggle = true)
        }

        onNodeWithContentDescription(English.switchToGregorianContentDescription).performClick()
        waitForIdle()
        runOnIdle {
            assertEquals(nearTableEnd, state.selectedDate?.toSimpleDate())
            assertTrue(state.displayedMonthCalendar.year in state.englishYearRange)
        }

        onNodeWithContentDescription(English.switchToBikramSambatContentDescription).performClick()
        waitForIdle()
        runOnIdle {
            assertEquals(nearTableEnd, state.selectedDate?.toSimpleDate())
            assertTrue(state.displayedMonth.year in NepaliCalendarDefaults.NepaliYearRange)
        }
    }

    @Test
    fun customStateImplementation_keepsWorkingWithoutOverridingTheNewMembers() {
        // The interface defaults exist so a hand-rolled state stays source compatible; it simply
        // does not respond to the switch.
        val state = object : NepaliDatePickerState {
            override var selectedDate: CustomCalendar? =
                NepaliDateConverter.getNepaliCalendar(2083, 6, 1)
            override var displayedMonth: NepaliMonthCalendar =
                NepaliDateConverter.getNepaliMonthCalendar(2083, 6)
            override val selectedEnglishDate: CustomCalendar? = null
            override var displayMode: DisplayMode = DisplayMode.Picker
            override val yearRange: IntRange = NepaliCalendarDefaults.NepaliYearRange
            override val nepaliSelectableDates: NepaliSelectableDates = AllDatesForTest
            override val locale: NepaliDateLocale = EnglishLocale
        }

        assertEquals(CalendarSystem.BIKRAM_SAMBAT, state.displayedCalendarSystem)
        assertEquals(2083, state.displayedMonthCalendar.year)
        assertEquals(NepaliCalendarDefaults.EnglishYearRange, state.englishYearRange)

        state.displayedCalendarSystem = CalendarSystem.GREGORIAN
        assertEquals(
            CalendarSystem.BIKRAM_SAMBAT,
            state.displayedCalendarSystem,
            "the default implementation is inert, not broken"
        )
        assertTrue(state.selectedDate != null)
    }
}

private val AllDatesForTest: NepaliSelectableDates = object : NepaliSelectableDates {}

// The title carries a top inset the switch is bottom-aligned against, so the two tops differ by a
// few pixels even while sharing one row. Anything beyond this would mean a row of its own.
private const val TitleRowSlackPx = 48f
