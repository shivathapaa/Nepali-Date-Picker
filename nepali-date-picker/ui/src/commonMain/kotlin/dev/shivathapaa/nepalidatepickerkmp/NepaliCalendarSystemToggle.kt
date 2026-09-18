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

package dev.shivathapaa.nepalidatepickerkmp

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import dev.shivathapaa.nepalidatepickerkmp.annotations.ExperimentalNepaliDatePickerApi
import dev.shivathapaa.nepalidatepickerkmp.calendar_model.NepaliDatePickerColors
import dev.shivathapaa.nepalidatepickerkmp.calendar_model.NepaliDatePickerDefaults
import dev.shivathapaa.nepalidatepickerkmp.data.CalendarSystem
import dev.shivathapaa.nepalidatepickerkmp.data.NepaliDatePickerLang

/**
 * A two-segment switch between the Bikram Sambat and Gregorian calendars.
 *
 * The pickers compose this themselves when `showCalendarSystemToggle` is on, so reach for it
 * directly only when you are driving [NepaliDatePickerState.displayedCalendarSystem] from your own
 * chrome (an app bar, a settings row) instead of from inside the picker.
 *
 * Switching the calendar never changes what is selected: a date picked in one calendar stays picked
 * in the other, because selection is always stored in Bikram Sambat.
 *
 * @param calendarSystem the calendar currently shown.
 * @param onCalendarSystemChange invoked with the tapped calendar. Fires only on an actual change.
 * @param modifier the [Modifier] applied to the switch.
 * @param language the language the segment labels and their content descriptions are written in.
 * @param colors the [NepaliDatePickerColors] the switch themes itself from; the selected segment
 *   uses the selected-day colors so it reads as part of the picker.
 *
 * Example usage:
 * ```
 * val state = rememberNepaliDatePickerState()
 *
 * NepaliCalendarSystemToggle(
 *     calendarSystem = state.displayedCalendarSystem,
 *     onCalendarSystemChange = { state.displayedCalendarSystem = it }
 * )
 * ```
 *
 * @see NepaliDatePicker
 */
@ExperimentalNepaliDatePickerApi
@Composable
fun NepaliCalendarSystemToggle(
    calendarSystem: CalendarSystem,
    onCalendarSystemChange: (CalendarSystem) -> Unit,
    modifier: Modifier = Modifier,
    language: NepaliDatePickerLang = NepaliDatePickerLang.ENGLISH,
    colors: NepaliDatePickerColors = NepaliDatePickerDefaults.colors()
) {
    val gregorianSelected = calendarSystem == CalendarSystem.GREGORIAN

    // One 0..1 progress drives both the indicator's travel and its width morph, and it is read
    // inside the measure lambda below so every frame stays in the layout phase. Animating the
    // indicator's measured width instead would write snapshot state from layout, which a
    // LazyLayout in the same pass cannot survive.
    val indicatorProgress by animateFloatAsState(
        targetValue = if (gregorianSelected) 1f else 0f,
        animationSpec = IndicatorSpring,
        label = "NepaliCalendarSystemIndicatorProgress"
    )

    Surface(
        modifier = modifier,
        shape = CircleShape,
        color = colors.dayInSelectionRangeContainerColor.copy(alpha = ToggleTrackAlpha)
    ) {
        Layout(
            modifier = Modifier.padding(ToggleTrackPadding).selectableGroup(),
            content = {
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(colors.selectedDayContainerColor)
                )
                CalendarSystemSegment(
                    label = language.bikramSambatShort,
                    segmentContentDescription = language.switchToBikramSambatContentDescription,
                    selected = !gregorianSelected,
                    onClick = { onCalendarSystemChange(CalendarSystem.BIKRAM_SAMBAT) },
                    colors = colors
                )
                CalendarSystemSegment(
                    label = language.gregorianShort,
                    segmentContentDescription = language.switchToGregorianContentDescription,
                    selected = gregorianSelected,
                    onClick = { onCalendarSystemChange(CalendarSystem.GREGORIAN) },
                    colors = colors
                )
            }
        ) { measurables, constraints ->
            val spacing = ToggleSegmentSpacing.roundToPx()
            val segmentConstraints = constraints.copy(minWidth = 0, minHeight = 0)
            val bikramSambat = measurables[BikramSambatSegmentIndex].measure(segmentConstraints)
            val gregorian = measurables[GregorianSegmentIndex].measure(segmentConstraints)

            val height = maxOf(bikramSambat.height, gregorian.height)
            val secondSegmentX = bikramSambat.width + spacing
            val indicator = measurables[IndicatorIndex].measure(
                Constraints.fixed(
                    width = lerp(bikramSambat.width, gregorian.width, indicatorProgress),
                    height = height
                )
            )

            layout(width = secondSegmentX + gregorian.width, height = height) {
                indicator.placeRelative(x = lerp(0, secondSegmentX, indicatorProgress), y = 0)
                bikramSambat.placeRelative(x = 0, y = (height - bikramSambat.height) / 2)
                gregorian.placeRelative(x = secondSegmentX, y = (height - gregorian.height) / 2)
            }
        }
    }
}

@Composable
private fun CalendarSystemSegment(
    label: String,
    segmentContentDescription: String,
    selected: Boolean,
    onClick: () -> Unit,
    colors: NepaliDatePickerColors
) {
    val contentColor by animateColorAsState(
        targetValue = if (selected) {
            colors.selectedDayContentColor
        } else {
            colors.navigationContentColor
        },
        animationSpec = tween(durationMillis = ToggleColorDurationMillis),
        label = "NepaliCalendarSystemSegmentContentColor"
    )

    Box(
        modifier = Modifier
            .defaultMinSize(minWidth = ToggleSegmentMinWidth, minHeight = ToggleSegmentHeight)
            .clip(CircleShape)
            .selectable(
                selected = selected,
                role = Role.RadioButton,
                onClick = { if (!selected) onClick() }
            )
            .semantics { contentDescription = segmentContentDescription }
            .padding(horizontal = ToggleSegmentHorizontalPadding),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = contentColor,
            textAlign = TextAlign.Center,
            maxLines = 1,
            style = MaterialTheme.typography.labelSmall
        )
    }
}

// Slot order inside the switch's Layout: the indicator is first so it paints behind the labels.
private const val IndicatorIndex = 0
private const val BikramSambatSegmentIndex = 1
private const val GregorianSegmentIndex = 2

private const val ToggleTrackAlpha = 0.5f
private const val ToggleColorDurationMillis = 150

private val IndicatorSpring: SpringSpec<Float> = spring(
    dampingRatio = Spring.DampingRatioLowBouncy,
    stiffness = Spring.StiffnessMediumLow
)

private val ToggleTrackPadding = 3.dp
private val ToggleSegmentSpacing = 2.dp

private val ToggleSegmentMinWidth: Dp = 36.dp
private val ToggleSegmentHeight: Dp = 24.dp
private val ToggleSegmentHorizontalPadding = 6.dp
