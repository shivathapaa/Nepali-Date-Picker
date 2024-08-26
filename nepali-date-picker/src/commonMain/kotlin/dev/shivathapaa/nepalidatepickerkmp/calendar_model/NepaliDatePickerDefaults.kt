package dev.shivathapaa.nepalidatepickerkmp.calendar_model

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.shivathapaa.nepalidatepickerkmp.NepaliSelectableDates
import dev.shivathapaa.nepalidatepickerkmp.data.CustomCalendar
import dev.shivathapaa.nepalidatepickerkmp.data.NameFormat
import dev.shivathapaa.nepalidatepickerkmp.data.NepaliDateFormatStyle
import dev.shivathapaa.nepalidatepickerkmp.data.NepaliDateLocale
import dev.shivathapaa.nepalidatepickerkmp.data.NepaliDatePickerLang
import dev.shivathapaa.nepalidatepickerkmp.data.SimpleDate

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
        dividerColor: Color = Color.Unspecified
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
        dividerColor = dividerColor
    )

    private val defaultDatePickerColors: NepaliDatePickerColors
        @Composable get() {
            return getDefaultNepaliDatePickerColors()
        }

    /** The default first day of the week. */
    const val FIRST_DAY_OF_WEEK: Int = 1

    /** The default tonal elevation used for date picker dialog. */
    val TonalElevation: Dp = 6.0.dp

    /** The default shape for date picker dialogs. */
    val shape: Shape @Composable get() = RoundedCornerShape(28.0.dp)

    /**
     * A default [NepaliSelectableDates] that allows all dates to be selected.
     */
    val AllDates: NepaliSelectableDates = object : NepaliSelectableDates {}

    /**
     * A default [NepaliDateLocale].
     */
    val DefaultLocale: NepaliDateLocale = NepaliDateLocale()

    val DateFormatStyle = NepaliDateFormatStyle.MEDIUM

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    internal fun NepaliDatePickerTitle(
        modifier: Modifier = Modifier, language: NepaliDatePickerLang
    ) {
        Text(
            text = language.datePickerTitle,
            modifier = modifier,
            color = DatePickerDefaults.colors().titleContentColor
        )
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    internal fun NepaliDatePickerHeadline(
        selectedDate: SimpleDate?, modifier: Modifier = Modifier, locale: NepaliDateLocale
    ) {
        val calendarModel = NepaliCalendarModel(locale)

        val formattedDate = selectedDate?.let { date ->
            val monthName =
                calendarModel.getNepaliMonthName(date.month, NameFormat.FULL, locale.language)
            val day = calendarModel.localizeNumber(
                stringToLocalize = date.dayOfMonth.toString(), locale = locale.language
            )
            val year = calendarModel.localizeNumber(
                stringToLocalize = date.year.toString(), locale = locale.language
            )
            "$monthName $day, $year"
        } ?: locale.language.selectDateText

        Text(
            text = formattedDate,
            modifier = modifier,
            maxLines = 1,
            color = DatePickerDefaults.colors().headlineContentColor
        )
    }

    /** The range of years for the Nepali date picker */
    val EnglishYearRange: IntRange = IntRange(1913, 2033)
    val NepaliYearRange: IntRange = IntRange(1970, 2090)

    /** Starting Nepali date */
    internal val startingNepaliCalendar = CustomCalendar(
        year = NepaliYearRange.first,
        month = 1,
        dayOfMonth = 1,
        totalDaysInMonth = 31,
        dayOfWeekInMonth = 1,
        dayOfWeek = 1,
        dayOfYear = 1,
        weekOfMonth = 1,
        era = 2,
        weekOfYear = 1,
        firstDayOfMonth = 1, // Sunday
        lastDayOfMonth = 3   // Tuesday
    )

    /** Starting English date */
    internal val startingEnglishCalendar = CustomCalendar(
        year = EnglishYearRange.first,
        month = 4,
        dayOfMonth = 13,
        totalDaysInMonth = 30,
        dayOfWeekInMonth = 2,
        dayOfWeek = 1,
        dayOfYear = 103,
        weekOfMonth = 3,
        era = 1,
        weekOfYear = 16,
        firstDayOfMonth = 3, // Tuesday
        lastDayOfMonth = 4   // Wednesday
    )
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
    val dividerColor: Color
) {
    /**
     * Returns a copy of this DatePickerColors, optionally overriding some of the values.
     * This uses the Color.Unspecified to mean “use the value from the source”
     * // For `dateTextFieldColors` use null to mean "use the value from source"
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
        dividerColor: Color = this.dividerColor
    ) = NepaliDatePickerColors(containerColor.takeOrElse { this.containerColor },
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
        dividerColor.takeOrElse { this.dividerColor })

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

        return true
    }

    override fun hashCode(): Int {
        var result = containerColor.hashCode()
        result = 31 * result + titleContentColor.hashCode()
        result = 31 * result + headlineContentColor.hashCode()
        result = 31 * result + weekdayContentColor.hashCode()
        result = 31 * result + subheadContentColor.hashCode()
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
        return result
    }
}

@Stable
@Composable
internal fun getDefaultNepaliDatePickerColors(): NepaliDatePickerColors {
    return NepaliDatePickerColors(
        containerColor = MaterialTheme.colorScheme.surface,
        titleContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        headlineContentColor = MaterialTheme.typography.headlineLarge.color,
        weekdayContentColor = MaterialTheme.colorScheme.onSurface,
        subheadContentColor = MaterialTheme.typography.titleSmall.color,
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
        dividerColor = MaterialTheme.colorScheme.outlineVariant
    )
}

internal const val DurationShort2 = 100.0
internal const val DisabledAlpha = 0.38f