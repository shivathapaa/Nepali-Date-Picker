/*
 * Copyright © 2024 Shiva Thapa (@shivathapaa). All rights reserved.
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

package dev.shivathapaa.nepalidatepickerkmp.calendar_model

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.shivathapaa.nepalidatepickerkmp.DisplayMode
import dev.shivathapaa.nepalidatepickerkmp.NepaliSelectableDates
import dev.shivathapaa.nepalidatepickerkmp.annotations.ExperimentalNepaliDatePickerApi
import dev.shivathapaa.nepalidatepickerkmp.data.CalendarSystem
import dev.shivathapaa.nepalidatepickerkmp.data.CustomCalendar
import dev.shivathapaa.nepalidatepickerkmp.data.NameFormat
import dev.shivathapaa.nepalidatepickerkmp.data.NepaliDateFormatStyle
import dev.shivathapaa.nepalidatepickerkmp.data.NepaliDateLocale
import dev.shivathapaa.nepalidatepickerkmp.data.NepaliDatePickerLang
import dev.shivathapaa.nepalidatepickerkmp.data.SimpleDate
import dev.shivathapaa.nepalidatepickerkmp.data.toSimpleDate
import dev.shivathapaa.nepalidatepickerkmp.event.NepaliCalendarPolicy
import dev.shivathapaa.nepalidatepickerkmp.event.NepaliDayStatus
import dev.shivathapaa.nepalidatepickerkmp.event.NepaliEventKind
import dev.shivathapaa.nepalidatepickerkmp.event.NepaliEventProvider
import dev.shivathapaa.nepalidatepickerkmp.event.NepaliWeekend

@Stable
object NepaliDatePickerDefaults {

    @Composable
    fun colors() = defaultDatePickerColors

    /**
     * Creates a [NepaliDatePickerColors] that will potentially animate between the provided colors
     * according to the Material specification.
     *
     * @param containerColor the color used for the date picker's background
     * @param titleContentColor the color used for the date picker's title
     * @param headlineContentColor the color used for the date picker's headline
     * @param weekdayContentColor the color used for the weekday letters
     * @param subheadContentColor the color used for the month and year subhead labels that appear
     * when months are displayed at a `DateRangePicker`.
     * @param navigationContentColor the content color used for the year selection menu button and
     * the months arrow navigation when displayed at a `DatePicker`.
     * @param yearContentColor the color used for a year item content
     * @param disabledYearContentColor the color used for a disabled year item content
     * @param currentYearContentColor the color used for the current year content when selecting a
     * year
     * @param selectedYearContentColor the color used for a selected year item content
     * @param disabledSelectedYearContentColor the color used for a disabled selected year item
     * content
     * @param selectedYearContainerColor the color used for a selected year item container
     * @param disabledSelectedYearContainerColor the color used for a disabled selected year item
     * container
     * @param dayContentColor the color used for days content
     * @param disabledDayContentColor the color used for disabled days content
     * @param selectedDayContentColor the color used for selected days content
     * @param disabledSelectedDayContentColor the color used for disabled selected days content
     * @param selectedDayContainerColor the color used for a selected day container
     * @param disabledSelectedDayContainerColor the color used for a disabled selected day container
     * @param todayContentColor the color used for the day that marks the current date
     * @param todayDateBorderColor the color used for the border of the day that marks the current
     * date
     * @param dayInSelectionRangeContentColor the content color used for days that are within a date
     * range selection (which will be implemented in future updates)
     * @param dayInSelectionRangeContainerColor the container color used for days that are within a
     * date range selection (which will be implemented in future updates)
     * @d
     * @param dividerColor the color used for the dividers used at the date pickers
     */
    @Composable
    fun colors(
        containerColor: Color = Color.Unspecified,
        titleContentColor: Color = Color.Unspecified,
        headlineContentColor: Color = Color.Unspecified,
        weekdayContentColor: Color = Color.Unspecified,
        subheadContentColor: Color = Color.Unspecified,
        navigationContentColor: Color = Color.Unspecified,
        yearContentColor: Color = Color.Unspecified,
        disabledYearContentColor: Color = Color.Unspecified,
        currentYearContentColor: Color = Color.Unspecified,
        selectedYearContentColor: Color = Color.Unspecified,
        disabledSelectedYearContentColor: Color = Color.Unspecified,
        selectedYearContainerColor: Color = Color.Unspecified,
        disabledSelectedYearContainerColor: Color = Color.Unspecified,
        dayContentColor: Color = Color.Unspecified,
        disabledDayContentColor: Color = Color.Unspecified,
        selectedDayContentColor: Color = Color.Unspecified,
        disabledSelectedDayContentColor: Color = Color.Unspecified,
        selectedDayContainerColor: Color = Color.Unspecified,
        disabledSelectedDayContainerColor: Color = Color.Unspecified,
        todayContentColor: Color = Color.Unspecified,
        todayDateBorderColor: Color = Color.Unspecified,
        dayInSelectionRangeContentColor: Color = Color.Unspecified,
        dayInSelectionRangeContainerColor: Color = Color.Unspecified,
        dividerColor: Color = Color.Unspecified,
        dateTextFieldColors: TextFieldColors? = null
    ): NepaliDatePickerColors = defaultDatePickerColors.copy(
        containerColor = containerColor,
        titleContentColor = titleContentColor,
        headlineContentColor = headlineContentColor,
        weekdayContentColor = weekdayContentColor,
        subheadContentColor = subheadContentColor,
        navigationContentColor = navigationContentColor,
        yearContentColor = yearContentColor,
        disabledYearContentColor = disabledYearContentColor,
        currentYearContentColor = currentYearContentColor,
        selectedYearContentColor = selectedYearContentColor,
        disabledSelectedYearContentColor = disabledSelectedYearContentColor,
        selectedYearContainerColor = selectedYearContainerColor,
        disabledSelectedYearContainerColor = disabledSelectedYearContainerColor,
        dayContentColor = dayContentColor,
        disabledDayContentColor = disabledDayContentColor,
        selectedDayContentColor = selectedDayContentColor,
        disabledSelectedDayContentColor = disabledSelectedDayContentColor,
        selectedDayContainerColor = selectedDayContainerColor,
        disabledSelectedDayContainerColor = disabledSelectedDayContainerColor,
        todayContentColor = todayContentColor,
        todayDateBorderColor = todayDateBorderColor,
        dayInSelectionRangeContentColor = dayInSelectionRangeContentColor,
        dayInSelectionRangeContainerColor = dayInSelectionRangeContainerColor,
        dividerColor = dividerColor,
        dateTextFieldColors = dateTextFieldColors
    )

    private val defaultDatePickerColors: NepaliDatePickerColors
        @Composable get() {
            return getDefaultNepaliDatePickerColors()
        }

    /**
     * Creates a [NepaliDayMarkerColors] for the days a [NepaliDayDecorator] marks.
     *
     * Every slot left as [Color.Unspecified] is taken from `MaterialTheme.colorScheme`, so the
     * palette follows the app's theme into dark mode without a second definition.
     *
     * @param weeklyOffColor a day the institution never opens, `colorScheme.error` by default
     * @param publicHolidayColor a day offices close, `colorScheme.error` by default
     * @param religiousColor a religious or cultural festival, `colorScheme.primary` by default
     * @param regionalColor a province- or district-level holiday, `colorScheme.tertiary` by default
     * @param observanceColor a recognized day that keeps offices open, `colorScheme.secondary` by
     * default
     * @param eventColor a general-purpose slot for an app's own categories, `colorScheme.primary`
     * by default
     * @param personalColor a second general-purpose slot, `colorScheme.tertiary` by default
     * @param markedContainerColor the disc behind a day tinted whole rather than dotted,
     * `colorScheme.errorContainer` by default
     */
    @Composable
    fun markerColors(
        weeklyOffColor: Color = Color.Unspecified,
        publicHolidayColor: Color = Color.Unspecified,
        religiousColor: Color = Color.Unspecified,
        regionalColor: Color = Color.Unspecified,
        observanceColor: Color = Color.Unspecified,
        eventColor: Color = Color.Unspecified,
        personalColor: Color = Color.Unspecified,
        markedContainerColor: Color = Color.Unspecified
    ): NepaliDayMarkerColors = getDefaultNepaliDayMarkerColors().copy(
        weeklyOffColor = weeklyOffColor,
        publicHolidayColor = publicHolidayColor,
        religiousColor = religiousColor,
        regionalColor = regionalColor,
        observanceColor = observanceColor,
        eventColor = eventColor,
        personalColor = personalColor,
        markedContainerColor = markedContainerColor
    )

    /**
     * Creates a [NepaliEventDisplayStyle], the switches [eventDecorator] draws a closed day by.
     *
     * The defaults colour the number and stop there, for both the weekly rule and named holidays,
     * which leaves the dots to mean events alone.
     *
     * @param colorWeeklyOff whether a weekly off day is coloured
     * @param colorEvents whether a named holiday is coloured
     * @param tintContainer whether a day with any status also takes a tinted disc
     * @param indicateWeeklyOff whether a weekly off day draws a dot, off by default
     * @param indicateKinds which kinds of named holiday draw a dot, none by default
     * @param describe whether the day's holiday names reach the screen reader
     */
    fun eventDisplayStyle(
        colorWeeklyOff: Boolean = true,
        colorEvents: Boolean = true,
        tintContainer: Boolean = false,
        indicateWeeklyOff: Boolean = false,
        indicateKinds: Set<NepaliEventKind> = emptySet(),
        describe: Boolean = true
    ): NepaliEventDisplayStyle = NepaliEventDisplayStyle(
        colorWeeklyOff = colorWeeklyOff,
        colorEvents = colorEvents,
        tintContainer = tintContainer,
        indicateWeeklyOff = indicateWeeklyOff,
        indicateKinds = indicateKinds,
        describe = describe
    )

    /**
     * A [NepaliDayDecorator] that marks the days [policy] says the institution is closed: its weekly
     * off days, and the holidays its provider reports.
     *
     * A day takes one colour. A named holiday is coloured by the strongest kind on it, a closure
     * before a festival, a festival before a regional holiday, and all three before an observance;
     * a day that is only a weekly off day takes [NepaliDayMarkerColors.weeklyOffColor]. A Saturday
     * that is also Dashain is Dashain, because that is the more specific fact about it, but a
     * Saturday that merely carries an observance stays a Saturday: a kind that does not close the
     * office cannot make a closed day look open.
     *
     * Nothing is dotted unless [style] asks for it, so the dots a grid draws stay available for the
     * app's own events. Compose the two with [then]:
     *
     * ```
     * NepaliDatePicker(
     *     state = state,
     *     dayDecorator = NepaliDatePickerDefaults
     *         .eventDecorator(policy = schoolPolicy)
     *         .then(NepaliDatePickerDefaults.dayDecorator(markers = myEvents))
     * )
     * ```
     *
     * The decorator reads [NepaliEventProvider.events] once per visible cell, so a provider that
     * answers from a memoized source keeps the grid cheap, as the SPI asks. Marking a day never
     * blocks it: pass [NepaliCalendarPolicy.asSelectableDates] to the state when a screen should also
     * refuse the days it marks.
     */
    @Composable
    fun eventDecorator(
        policy: NepaliCalendarPolicy,
        colors: NepaliDayMarkerColors = markerColors(),
        style: NepaliEventDisplayStyle = NepaliEventDisplayStyle.Default
    ): NepaliDayDecorator = remember(policy, colors, style) {
        NepaliDayDecorator { day ->
            NepaliDayStatus(
                isWeeklyOff = policy.isWeeklyOff(day.date.dayOfWeek),
                events = policy.eventsOn(day.date.toSimpleDate())
            ).toDayDecoration(colors, style)
        }
    }

    /**
     * [eventDecorator] for an institution that keeps Nepal's usual week, Saturday off, or none at
     * all. `weeklyOffDays = emptySet()` marks the provider's holidays and leaves every weekday alone.
     */
    @Composable
    fun eventDecorator(
        provider: NepaliEventProvider,
        weeklyOffDays: Set<Int> = NepaliWeekend.Default,
        colors: NepaliDayMarkerColors = markerColors(),
        style: NepaliEventDisplayStyle = NepaliEventDisplayStyle.Default
    ): NepaliDayDecorator = eventDecorator(
        policy = remember(provider, weeklyOffDays) {
            NepaliCalendarPolicy(weeklyOffDays = weeklyOffDays, provider = provider)
        },
        colors = colors,
        style = style
    )

    /**
     * A [NepaliDayDecorator] built from plain maps, for an app that already holds its events by
     * date.
     *
     * A date in [markers] is drawn with one dot per color listed for it, a date in [contentColors]
     * has its day number recolored, and a date in [descriptions] has that text announced after its
     * own. A date in none of the three is left alone.
     *
     * ```
     * dayDecorator = NepaliDatePickerDefaults.dayDecorator(
     *     markers = mapOf(SimpleDate(2082, 6, 3) to listOf(MaterialTheme.colorScheme.primary))
     * )
     * ```
     */
    @Composable
    fun dayDecorator(
        markers: Map<SimpleDate, List<Color>>,
        contentColors: Map<SimpleDate, Color> = emptyMap(),
        descriptions: Map<SimpleDate, String> = emptyMap()
    ): NepaliDayDecorator = remember(markers, contentColors, descriptions) {
        NepaliDayDecorator { day ->
            val date = day.date.toSimpleDate()
            val dots = markers[date].orEmpty()
            val contentColor = contentColors[date] ?: Color.Unspecified
            val description = descriptions[date]
            if (dots.isEmpty() && contentColor == Color.Unspecified && description == null) {
                return@NepaliDayDecorator null
            }
            NepaliDayDecoration(
                contentColor = contentColor,
                indicators = dots,
                contentDescription = description
            )
        }
    }

    /** The default first day of the week. */
    const val FIRST_DAY_OF_WEEK: Int = 1

    /**
     * A default [NepaliDateLocale].
     */
    val DefaultLocale: NepaliDateLocale = NepaliDateLocale()

    /**
     * I will suggest as below considering user's screen width and clarity.
     *
     * ```
     * NepaliDateLocale(dateFormat = NepaliDateFormatStyle.SHORT_YMD)
     * ```
     * */
    val DefaultRangePickerLocale: NepaliDateLocale =
        NepaliDateLocale(monthName = NameFormat.SHORT)

    val DateFormatStyle = NepaliDateFormatStyle.SHORT_YMD

    /** The default tonal elevation used for date picker dialog. */
    val TonalElevation: Dp = 6.0.dp

    /** The default shape for date picker dialogs. */
    val shape: Shape @Composable get() = RoundedCornerShape(28.0.dp)

    /**
     * A default [NepaliSelectableDates] that allows all dates to be selected.
     */
    val AllDates: NepaliSelectableDates = object : NepaliSelectableDates {}

    @Composable
    internal fun NepaliDatePickerTitle(
        modifier: Modifier = Modifier,
        language: NepaliDatePickerLang,
        displayMode: DisplayMode,
        calendarSystem: CalendarSystem = CalendarSystem.BIKRAM_SAMBAT
    ) {
        val bikramSambat = calendarSystem == CalendarSystem.BIKRAM_SAMBAT
        val title = when (displayMode) {
            DisplayMode.Input ->
                if (bikramSambat) language.dateInputTitle else language.englishDateInputTitle

            else ->
                if (bikramSambat) language.datePickerTitle else language.englishDatePickerTitle
        }
        Text(text = title, modifier = modifier, maxLines = 1)
    }

    @Composable
    internal fun NepaliDateRangePickerTitle(
        modifier: Modifier = Modifier,
        language: NepaliDatePickerLang,
        displayMode: DisplayMode,
        calendarSystem: CalendarSystem = CalendarSystem.BIKRAM_SAMBAT
    ) {
        val bikramSambat = calendarSystem == CalendarSystem.BIKRAM_SAMBAT
        val title = when (displayMode) {
            DisplayMode.Input ->
                if (bikramSambat) {
                    language.dateRangeInputTitle
                } else {
                    language.englishDateRangeInputTitle
                }

            else ->
                if (bikramSambat) {
                    language.dateRangePickerTitle
                } else {
                    language.englishDateRangePickerTitle
                }
        }
        Text(text = title, modifier = modifier, maxLines = 1)
    }

    @Composable
    internal fun NepaliDatePickerHeadline(
        selectedDate: CustomCalendar?,
        locale: NepaliDateLocale,
        displayMode: DisplayMode,
        modifier: Modifier = Modifier,
        selectedEnglishDate: CustomCalendar? = null,
        calendarSystem: CalendarSystem = CalendarSystem.BIKRAM_SAMBAT
    ) {
        val calendarModel = NepaliCalendarModel(locale)

        val formattedDate = calendarModel.formatInCalendar(
            calendarSystem = calendarSystem,
            nepaliDate = selectedDate,
            englishDate = selectedEnglishDate,
            locale = locale
        ) ?: if (displayMode == DisplayMode.Input) {
            locale.language.writeDateText
        } else {
            locale.language.selectDateText
        }

        Text(
            text = formattedDate,
            modifier = modifier,
            maxLines = 1
        )
    }

    @Composable
    internal fun NepaliDatePickerHeadlineWithEnglishDate(
        selectedDate: CustomCalendar?,
        selectedEnglishDate: CustomCalendar?,
        locale: NepaliDateLocale,
        englishLocale: NepaliDateLocale,
        displayMode: DisplayMode,
        modifier: Modifier = Modifier,
        calendarSystem: CalendarSystem = CalendarSystem.BIKRAM_SAMBAT
    ) {
        val calendarModel = NepaliCalendarModel(locale)

        // The displayed calendar leads and takes the picker's own locale; the other one follows in
        // the smaller line with the secondary locale.
        val formattedDate = calendarModel.formatInCalendar(
            calendarSystem = calendarSystem,
            nepaliDate = selectedDate,
            englishDate = selectedEnglishDate,
            locale = locale
        ) ?: if (displayMode == DisplayMode.Input) {
            locale.language.writeDateText
        } else {
            locale.language.selectDateText
        }

        val formattedSecondaryDate = calendarModel.formatInCalendar(
            calendarSystem = calendarSystem.opposite(),
            nepaliDate = selectedDate,
            englishDate = selectedEnglishDate,
            locale = englishLocale
        )

        Column(
            modifier = modifier.heightIn(min = 72.dp, max = 92.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically)
        ) {
            Text(
                text = formattedDate,
                modifier = Modifier,
                maxLines = 1
            )

            AnimatedVisibility(!formattedSecondaryDate.isNullOrEmpty()) {
                Text(
                    text = formattedSecondaryDate ?: "", /* // */
                    modifier = Modifier,
                    maxLines = 1,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }

    @Composable
    internal fun NepaliDateRangePickerHeadline(
        selectedStartDate: CustomCalendar?,
        selectedEndDate: CustomCalendar?,
        modifier: Modifier = Modifier,
        locale: NepaliDateLocale,
        selectedStartEnglishDate: CustomCalendar? = null,
        selectedEndEnglishDate: CustomCalendar? = null,
        calendarSystem: CalendarSystem = CalendarSystem.BIKRAM_SAMBAT
    ) {
        val calendarModel = NepaliCalendarModel(locale)

        val formattedStartDate = calendarModel.formatInCalendar(
            calendarSystem = calendarSystem,
            nepaliDate = selectedStartDate,
            englishDate = selectedStartEnglishDate,
            locale = locale
        )

        val formattedEndDate = calendarModel.formatInCalendar(
            calendarSystem = calendarSystem,
            nepaliDate = selectedEndDate,
            englishDate = selectedEndEnglishDate,
            locale = locale
        )

        Row(
            modifier = modifier,
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            if (formattedStartDate != null) {
                Text(text = formattedStartDate)
            } else {
                Text(locale.language.startDate)
            }
            Text("-")
            if (formattedEndDate != null) {
                Text(text = formattedEndDate)
            } else {
                Text(locale.language.endDate)
            }
        }
    }

    /**
     * A Default Headline provider for `NepaliDateRangePickerWithEnglish`
     *
     * @param selectedNepaliStartDate is the selected Nepali start calendar
     * @param selectedNepaliStartDate is the selected Nepali end calendar
     * @param selectedEnglishStartDate is the selected English start calendar
     * @param selectedEnglishEndDate is the selected English end calendar
     * @param locale is the [NepaliDateLocale] for Nepali date formats and language
     * @param englishLocale is the [NepaliDateLocale] for English date formats and language
     * @param modifier is the [Modifier] for the Headline
     * @param isEnglishDateAligned controls the date shown in two ways i.e, takes both English and Nepali
     * date as a whole or separate
     */
    @ExperimentalNepaliDatePickerApi
    @Composable
    fun NepaliDateRangePickerHeadlineWithEnglishDate(
        selectedNepaliStartDate: CustomCalendar?,
        selectedNepaliEndDate: CustomCalendar?,
        selectedEnglishStartDate: CustomCalendar?,
        selectedEnglishEndDate: CustomCalendar?,
        locale: NepaliDateLocale,
        englishLocale: NepaliDateLocale,
        modifier: Modifier = Modifier,
        isEnglishDateAligned: Boolean = false,
        calendarSystem: CalendarSystem = CalendarSystem.BIKRAM_SAMBAT
    ) {
        val calendarModel = NepaliCalendarModel(locale)

        // Whichever calendar is displayed leads with the picker's own locale; the other follows in
        // the smaller line with the secondary locale. The local names keep reading "Nepali" and
        // "English" because that is what they mean when nothing has been switched.
        val secondarySystem = calendarSystem.opposite()

        val formattedNepaliStartDate = calendarModel.formatInCalendar(
            calendarSystem = calendarSystem,
            nepaliDate = selectedNepaliStartDate,
            englishDate = selectedEnglishStartDate,
            locale = locale
        )

        val formattedNepaliEndDate = calendarModel.formatInCalendar(
            calendarSystem = calendarSystem,
            nepaliDate = selectedNepaliEndDate,
            englishDate = selectedEnglishEndDate,
            locale = locale
        )

        val formattedEnglishStartDate = calendarModel.formatInCalendar(
            calendarSystem = secondarySystem,
            nepaliDate = selectedNepaliStartDate,
            englishDate = selectedEnglishStartDate,
            locale = englishLocale
        )

        val formattedEnglishEndDate = calendarModel.formatInCalendar(
            calendarSystem = secondarySystem,
            nepaliDate = selectedNepaliEndDate,
            englishDate = selectedEnglishEndDate,
            locale = englishLocale
        )

        if (isEnglishDateAligned) {
            Row(
                modifier = modifier.heightIn(min = 72.dp, max = 92.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                if (formattedNepaliStartDate != null) {
                    NepaliEnglishDateColumn(
                        nepaliFormattedDate = formattedNepaliStartDate,
                        englishFormattedDate = formattedEnglishStartDate
                    )
                } else {
                    Text(locale.language.startDate)
                }

                Text("-")

                if (formattedNepaliEndDate != null) {
                    NepaliEnglishDateColumn(
                        nepaliFormattedDate = formattedNepaliEndDate,
                        englishFormattedDate = formattedEnglishEndDate
                    )
                } else {
                    Text(locale.language.endDate)
                }
            }
        } else {
            Column(
                modifier = modifier.heightIn(min = 72.dp, max = 92.dp),
                verticalArrangement = Arrangement.Center
            ) {
                NepaliEnglishDateRow(
                    formattedStartDate = formattedNepaliStartDate,
                    formattedEndDate = formattedNepaliEndDate,
                    language = locale.language
                )

                AnimatedVisibility(!formattedNepaliStartDate.isNullOrEmpty()) {
                    NepaliEnglishDateRow(
                        formattedStartDate = formattedEnglishStartDate,
                        formattedEndDate = formattedEnglishEndDate,
                        language = locale.language,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }

    /**
     * A default button to for Nepali date picker dialog. i.e., "Cancel", "OK"
     */
    @Composable
    fun DialogButton(
        text: String,
        onButtonClick: () -> Unit,
        modifier: Modifier = Modifier,
        enabled: Boolean = true
    ) {
        TextButton(onClick = onButtonClick, modifier = modifier, enabled = enabled) {
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
            )
        }
    }

}


/**
 * Represents the colors used by the date picker.
 *
 * @constructor create an instance with arbitrary colors, see [NepaliDatePickerDefaults.colors] for the
 * default implementation that follows Material specifications.
 *
 * @param containerColor the color used for the date picker's background
 * @param titleContentColor the color used for the date picker's title
 * @param headlineContentColor the color used for the date picker's headline
 * @param weekdayContentColor the color used for the weekday letters
 * @param subheadContentColor the color used for the month and year subhead labels that appear
 * when months are displayed at a `DateRangePicker`.
 * @param navigationContentColor the content color used for the year selection menu button and
 * the months arrow navigation when displayed at a `DatePicker`.
 * @param yearContentColor the color used for a year item content
 * @param disabledYearContentColor the color used for a disabled year item content
 * @param currentYearContentColor the color used for the current year content when selecting a
 * year
 * @param selectedYearContentColor the color used for a selected year item content
 * @param disabledSelectedYearContentColor the color used for a disabled selected year item
 * content
 * @param selectedYearContainerColor the color used for a selected year item container
 * @param disabledSelectedYearContainerColor the color used for a disabled selected year item
 * container
 * @param dayContentColor the color used for days content
 * @param disabledDayContentColor the color used for disabled days content
 * @param selectedDayContentColor the color used for selected days content
 * @param disabledSelectedDayContentColor the color used for disabled selected days content
 * @param selectedDayContainerColor the color used for a selected day container
 * @param disabledSelectedDayContainerColor the color used for a disabled selected day container
 * @param todayContentColor the color used for the day that marks the current date
 * @param todayDateBorderColor the color used for the border of the day that marks the current
 * date
 * @param dayInSelectionRangeContentColor the content color used for days that are within a date
 * range selection (which will be implemented in future updates)
 * @param dayInSelectionRangeContainerColor the container color used for days that are within a
 * date range selection (which will be implemented in future updates)
 * @param dividerColor the color used for the dividers used at the date pickers
 */
@Immutable
class NepaliDatePickerColors(
    val containerColor: Color,
    val titleContentColor: Color,
    val headlineContentColor: Color,
    val weekdayContentColor: Color,
    val subheadContentColor: Color,
    val navigationContentColor: Color,
    val yearContentColor: Color,
    val disabledYearContentColor: Color,
    val currentYearContentColor: Color,
    val selectedYearContentColor: Color,
    val disabledSelectedYearContentColor: Color,
    val selectedYearContainerColor: Color,
    val disabledSelectedYearContainerColor: Color,
    val dayContentColor: Color,
    val disabledDayContentColor: Color,
    val selectedDayContentColor: Color,
    val disabledSelectedDayContentColor: Color,
    val selectedDayContainerColor: Color,
    val disabledSelectedDayContainerColor: Color,
    val todayContentColor: Color,
    val todayDateBorderColor: Color,
    val dayInSelectionRangeContainerColor: Color,
    val dayInSelectionRangeContentColor: Color,
    val dividerColor: Color,
    val dateTextFieldColors: TextFieldColors
) {
    /**
     * Returns a copy of this NepaliDatePickerColors, optionally overriding some of the values.
     * This uses the Color.Unspecified to mean “use the value from the source”
     */
    fun copy(
        containerColor: Color = this.containerColor,
        titleContentColor: Color = this.titleContentColor,
        headlineContentColor: Color = this.headlineContentColor,
        weekdayContentColor: Color = this.weekdayContentColor,
        subheadContentColor: Color = this.subheadContentColor,
        navigationContentColor: Color = this.navigationContentColor,
        yearContentColor: Color = this.yearContentColor,
        disabledYearContentColor: Color = this.disabledYearContentColor,
        currentYearContentColor: Color = this.currentYearContentColor,
        selectedYearContentColor: Color = this.selectedYearContentColor,
        disabledSelectedYearContentColor: Color = this.disabledSelectedYearContentColor,
        selectedYearContainerColor: Color = this.selectedYearContainerColor,
        disabledSelectedYearContainerColor: Color = this.disabledSelectedYearContainerColor,
        dayContentColor: Color = this.dayContentColor,
        disabledDayContentColor: Color = this.disabledDayContentColor,
        selectedDayContentColor: Color = this.selectedDayContentColor,
        disabledSelectedDayContentColor: Color = this.disabledSelectedDayContentColor,
        selectedDayContainerColor: Color = this.selectedDayContainerColor,
        disabledSelectedDayContainerColor: Color = this.disabledSelectedDayContainerColor,
        todayContentColor: Color = this.todayContentColor,
        todayDateBorderColor: Color = this.todayDateBorderColor,
        dayInSelectionRangeContainerColor: Color = this.dayInSelectionRangeContainerColor,
        dayInSelectionRangeContentColor: Color = this.dayInSelectionRangeContentColor,
        dividerColor: Color = this.dividerColor,
        dateTextFieldColors: TextFieldColors? = this.dateTextFieldColors
    ) = NepaliDatePickerColors(
        containerColor.takeOrElse { this.containerColor },
        titleContentColor.takeOrElse { this.titleContentColor },
        headlineContentColor.takeOrElse { this.headlineContentColor },
        weekdayContentColor.takeOrElse { this.weekdayContentColor },
        subheadContentColor.takeOrElse { this.subheadContentColor },
        navigationContentColor.takeOrElse { this.navigationContentColor },
        yearContentColor.takeOrElse { this.yearContentColor },
        disabledYearContentColor.takeOrElse { this.disabledYearContentColor },
        currentYearContentColor.takeOrElse { this.currentYearContentColor },
        selectedYearContentColor.takeOrElse { this.selectedYearContentColor },
        disabledSelectedYearContentColor.takeOrElse { this.disabledSelectedYearContentColor },
        selectedYearContainerColor.takeOrElse { this.selectedYearContainerColor },
        disabledSelectedYearContainerColor.takeOrElse { this.disabledSelectedYearContainerColor },
        dayContentColor.takeOrElse { this.dayContentColor },
        disabledDayContentColor.takeOrElse { this.disabledDayContentColor },
        selectedDayContentColor.takeOrElse { this.selectedDayContentColor },
        disabledSelectedDayContentColor.takeOrElse { this.disabledSelectedDayContentColor },
        selectedDayContainerColor.takeOrElse { this.selectedDayContainerColor },
        disabledSelectedDayContainerColor.takeOrElse { this.disabledSelectedDayContainerColor },
        todayContentColor.takeOrElse { this.todayContentColor },
        todayDateBorderColor.takeOrElse { this.todayDateBorderColor },
        dayInSelectionRangeContainerColor.takeOrElse { this.dayInSelectionRangeContainerColor },
        dayInSelectionRangeContentColor.takeOrElse { this.dayInSelectionRangeContentColor },
        dividerColor.takeOrElse { this.dividerColor },
        dateTextFieldColors.takeOrElse { this.dateTextFieldColors })

    internal fun TextFieldColors?.takeOrElse(block: () -> TextFieldColors): TextFieldColors =
        this ?: block()

    /**
     * Represents the content color for a calendar day.
     *
     * @param isToday indicates that the color is for a date that represents today
     * @param selected indicates that the color is for a selected day
     * @param inRange indicates that the day is part of a selection range of days
     * @param enabled indicates that the day is enabled for selection
     */
    @Composable
    internal fun dayContentColor(
        isToday: Boolean, selected: Boolean, inRange: Boolean, enabled: Boolean
    ): State<Color> {
        val target = when {
            selected && enabled -> selectedDayContentColor
            selected && !enabled -> disabledSelectedDayContentColor
            inRange && enabled -> dayInSelectionRangeContentColor
            inRange && !enabled -> disabledDayContentColor
            isToday -> todayContentColor
            enabled -> dayContentColor
            else -> disabledDayContentColor
        }

        return if (inRange) {
            rememberUpdatedState(target)
        } else {
            // Animate the content color only when the day is not in a range.
            animateColorAsState(
                target,
                tween(durationMillis = DurationShort2.toInt()),
                label = "NepaliDayContentColor"
            )
        }
    }

    /**
     * Represents the container color for a calendar day.
     *
     * @param selected indicates that the color is for a selected day
     * @param enabled indicates that the day is enabled for selection
     * @param animate whether or not to animate a container color change
     */
    @Composable
    internal fun dayContainerColor(
        selected: Boolean, enabled: Boolean, animate: Boolean
    ): State<Color> {
        val target = if (selected) {
            if (enabled) selectedDayContainerColor else disabledSelectedDayContainerColor
        } else {
            Color.Transparent
        }
        return if (animate) {
            animateColorAsState(
                target,
                tween(durationMillis = DurationShort2.toInt()),
                label = "NepaliDayContainerColor"
            )
        } else {
            rememberUpdatedState(target)
        }
    }

    /**
     * Represents the content color for a calendar year.
     *
     * @param currentYear indicates that the color is for a year that represents the current year
     * @param selected indicates that the color is for a selected year
     * @param enabled indicates that the year is enabled for selection
     */
    @Composable
    internal fun yearContentColor(
        currentYear: Boolean, selected: Boolean, enabled: Boolean
    ): State<Color> {
        val target = when {
            selected && enabled -> selectedYearContentColor
            selected && !enabled -> disabledSelectedYearContentColor
            currentYear -> currentYearContentColor
            enabled -> yearContentColor
            else -> disabledYearContentColor
        }

        return animateColorAsState(
            target, tween(durationMillis = DurationShort2.toInt()), label = "NepaliYearContentColor"
        )
    }

    /**
     * Represents the container color for a calendar year.
     *
     * @param selected indicates that the color is for a selected day
     * @param enabled indicates that the year is enabled for selection
     */
    @Composable
    internal fun yearContainerColor(selected: Boolean, enabled: Boolean): State<Color> {
        val target = if (selected) {
            if (enabled) selectedYearContainerColor else disabledSelectedYearContainerColor
        } else {
            Color.Transparent
        }
        return animateColorAsState(
            target,
            tween(durationMillis = DurationShort2.toInt()),
            label = "NepaliYearContainerColor"
        )
    }

    override fun equals(other: Any?): Boolean {
        if (other !is NepaliDatePickerColors) return false
        if (containerColor != other.containerColor) return false
        if (titleContentColor != other.titleContentColor) return false
        if (headlineContentColor != other.headlineContentColor) return false
        if (weekdayContentColor != other.weekdayContentColor) return false
        if (subheadContentColor != other.subheadContentColor) return false
        if (navigationContentColor != other.navigationContentColor) return false
        if (yearContentColor != other.yearContentColor) return false
        if (disabledYearContentColor != other.disabledYearContentColor) return false
        if (currentYearContentColor != other.currentYearContentColor) return false
        if (selectedYearContentColor != other.selectedYearContentColor) return false
        if (disabledSelectedYearContentColor != other.disabledSelectedYearContentColor) return false
        if (selectedYearContainerColor != other.selectedYearContainerColor) return false
        if (disabledSelectedYearContainerColor != other.disabledSelectedYearContainerColor) return false
        if (dayContentColor != other.dayContentColor) return false
        if (disabledDayContentColor != other.disabledDayContentColor) return false
        if (selectedDayContentColor != other.selectedDayContentColor) return false
        if (disabledSelectedDayContentColor != other.disabledSelectedDayContentColor) return false
        if (selectedDayContainerColor != other.selectedDayContainerColor) return false
        if (disabledSelectedDayContainerColor != other.disabledSelectedDayContainerColor) {
            return false
        }
        if (todayContentColor != other.todayContentColor) return false
        if (todayDateBorderColor != other.todayDateBorderColor) return false
        if (dayInSelectionRangeContainerColor != other.dayInSelectionRangeContainerColor) {
            return false
        }
        if (dayInSelectionRangeContentColor != other.dayInSelectionRangeContentColor) return false
        if (dividerColor != other.dividerColor) return false
        if (dateTextFieldColors != other.dateTextFieldColors) return false

        return true
    }

    override fun hashCode(): Int {
        var result = containerColor.hashCode()
        result = 31 * result + titleContentColor.hashCode()
        result = 31 * result + headlineContentColor.hashCode()
        result = 31 * result + weekdayContentColor.hashCode()
        result = 31 * result + subheadContentColor.hashCode()
        result = 31 * result + navigationContentColor.hashCode()
        result = 31 * result + yearContentColor.hashCode()
        result = 31 * result + disabledYearContentColor.hashCode()
        result = 31 * result + currentYearContentColor.hashCode()
        result = 31 * result + selectedYearContentColor.hashCode()
        result = 31 * result + disabledSelectedYearContentColor.hashCode()
        result = 31 * result + selectedYearContainerColor.hashCode()
        result = 31 * result + disabledSelectedYearContainerColor.hashCode()
        result = 31 * result + dayContentColor.hashCode()
        result = 31 * result + disabledDayContentColor.hashCode()
        result = 31 * result + selectedDayContentColor.hashCode()
        result = 31 * result + disabledSelectedDayContentColor.hashCode()
        result = 31 * result + selectedDayContainerColor.hashCode()
        result = 31 * result + disabledSelectedDayContainerColor.hashCode()
        result = 31 * result + todayContentColor.hashCode()
        result = 31 * result + todayDateBorderColor.hashCode()
        result = 31 * result + dayInSelectionRangeContainerColor.hashCode()
        result = 31 * result + dayInSelectionRangeContentColor.hashCode()
        result = 31 * result + dividerColor.hashCode()
        result = 31 * result + dateTextFieldColors.hashCode()
        return result
    }
}

@Stable
@Composable
internal fun getDefaultNepaliDatePickerColors(): NepaliDatePickerColors {
    return NepaliDatePickerColors(
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        titleContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        headlineContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        weekdayContentColor = MaterialTheme.colorScheme.onSurface,
        subheadContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        navigationContentColor = MaterialTheme.colorScheme.onSurface,
        yearContentColor = MaterialTheme.colorScheme.onSurface,
        disabledYearContentColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = DisabledAlpha),
        currentYearContentColor = MaterialTheme.colorScheme.primary,
        selectedYearContentColor = MaterialTheme.colorScheme.onPrimary,
        disabledSelectedYearContentColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = DisabledAlpha),
        selectedYearContainerColor = MaterialTheme.colorScheme.primary,
        disabledSelectedYearContainerColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = DisabledAlpha),
        dayContentColor = MaterialTheme.colorScheme.onSurface,
        disabledDayContentColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = DisabledAlpha),
        selectedDayContentColor = MaterialTheme.colorScheme.onPrimary,
        disabledSelectedDayContentColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = DisabledAlpha),
        selectedDayContainerColor = MaterialTheme.colorScheme.primary,
        disabledSelectedDayContainerColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = DisabledAlpha),
        todayContentColor = MaterialTheme.colorScheme.primary,
        todayDateBorderColor = MaterialTheme.colorScheme.primary,
        dayInSelectionRangeContainerColor = MaterialTheme.colorScheme.secondaryContainer,
        dayInSelectionRangeContentColor = MaterialTheme.colorScheme.onSecondaryContainer,
        dividerColor = MaterialTheme.colorScheme.outlineVariant,
        dateTextFieldColors = OutlinedTextFieldDefaults.colors()
    )
}

/**
 * The marker palette drawn from the current theme, which is what makes a marked day read correctly
 * in light and dark without the app restating its colors.
 */
@Composable
internal fun getDefaultNepaliDayMarkerColors(): NepaliDayMarkerColors {
    return NepaliDayMarkerColors(
        weeklyOffColor = MaterialTheme.colorScheme.error,
        publicHolidayColor = MaterialTheme.colorScheme.error,
        religiousColor = MaterialTheme.colorScheme.primary,
        regionalColor = MaterialTheme.colorScheme.tertiary,
        observanceColor = MaterialTheme.colorScheme.secondary,
        eventColor = MaterialTheme.colorScheme.primary,
        personalColor = MaterialTheme.colorScheme.tertiary,
        markedContainerColor = MaterialTheme.colorScheme.errorContainer
    )
}

@Stable
@Composable
private fun NepaliEnglishDateColumn(
    nepaliFormattedDate: String,
    englishFormattedDate: String?,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.heightIn(min = 72.dp, max = 92.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically)
    ) {
        Text(
            text = nepaliFormattedDate,
            modifier = Modifier,
            maxLines = 1
        )

        AnimatedVisibility(!englishFormattedDate.isNullOrEmpty()) {
            Text(
                text = englishFormattedDate ?: "", /* !! */
                modifier = Modifier,
                maxLines = 1,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Stable
@Composable
private fun NepaliEnglishDateRow(
    formattedStartDate: String?,
    formattedEndDate: String?,
    language: NepaliDatePickerLang,
    modifier: Modifier = Modifier,
    style: TextStyle = LocalTextStyle.current
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        if (formattedStartDate != null) {
            Text(text = formattedStartDate, style = style)
        } else {
            Text(text = language.startDate, style = style)
        }
        Text("-")
        if (formattedEndDate != null) {
            Text(text = formattedEndDate, style = style)
        } else {
            Text(text = language.endDate, style = style)
        }
    }
}

/**
 * Formats whichever of the two dates [calendarSystem] names, or `null` when that one is absent.
 *
 * The headlines hold both halves of the same day and only differ in which one leads, so this keeps
 * the "pick a calendar, format its date" step in one place. The docked field is a headline too, in
 * that it reads back the selection, so it formats through here as well.
 */
internal fun NepaliCalendarModel.formatInCalendar(
    calendarSystem: CalendarSystem,
    nepaliDate: CustomCalendar?,
    englishDate: CustomCalendar?,
    locale: NepaliDateLocale
): String? = when (calendarSystem) {
    CalendarSystem.BIKRAM_SAMBAT -> nepaliDate?.let {
        formatNepaliDate(
            year = it.year,
            month = it.month,
            dayOfMonth = it.dayOfMonth,
            dayOfWeek = it.dayOfWeek,
            locale = locale
        )
    }

    CalendarSystem.GREGORIAN -> englishDate?.let {
        formatEnglishDate(
            year = it.year,
            month = it.month,
            dayOfMonth = it.dayOfMonth,
            dayOfWeek = it.dayOfWeek,
            locale = locale
        )
    }
}

internal const val DurationShort2 = 100.0
internal const val DisabledAlpha = 0.38f