// Copyright © 2026 Shiva Thapa (@shivathapaa). All rights reserved.
// Use of this source code is governed by the MPL 2.0 license that can be
// found in the LICENSE file.

package dev.shivathapaa.nepali_date_picker_kmp

import android.app.Activity
import dev.shivathapaa.nepalidatepickerkmp.android.NepaliDialogHandle
import dev.shivathapaa.nepalidatepickerkmp.android.NepaliEventInfo
import dev.shivathapaa.nepalidatepickerkmp.android.NepaliEventOptions
import dev.shivathapaa.nepalidatepickerkmp.android.NepaliPickerAppearance
import dev.shivathapaa.nepalidatepickerkmp.android.NepaliPickerBrightness
import dev.shivathapaa.nepalidatepickerkmp.android.showNepaliDatePickerDialog
import dev.shivathapaa.nepalidatepickerkmp.android.showNepaliDatePickerFullScreenDialog
import dev.shivathapaa.nepalidatepickerkmp.data.CustomCalendar
import dev.shivathapaa.nepalidatepickerkmp.event.NepaliEventKind
import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine

/**
 * Presents the native modal pickers over the current activity and drives the
 * Android appearance proxy. Dialog calls complete their Dart future on the
 * user's confirm or dismiss.
 */
internal class PickerHostApiImpl : PickerHostApi {

    /** The foreground activity, tracked by the plugin's ActivityAware hooks. */
    var activity: Activity? = null

    override suspend fun showDatePickerDialog(config: DialogConfigDto): CalendarDto? {
        val host = activity
            ?: throw FlutterError("NO_ACTIVITY", "No foreground activity to present over", null)
        return awaitDialog { onConfirm, onDismiss ->
            showNepaliDatePickerDialog(
                activity = host,
                initialSelectedDate = config.initialSelectedDate?.toCore(),
                locale = config.locale.toCore(),
                yearRange = config.yearRangeStart.toInt()..config.yearRangeEnd.toInt(),
                selectableDates = config.selectable.toCore(),
                showModeToggle = config.showModeToggle,
                showTodayButton = config.showTodayButton,
                showEnglishDate = config.showEnglishDate,
                englishDateLocale = config.englishDateLocale?.toCore(),
                initialCalendarSystem = calendarSystemOf(config.initialCalendarSystemEra),
                showCalendarSystemToggle = config.showCalendarSystemToggle,
                showAdjacentMonthDays = config.showAdjacentMonthDays,
                events = config.eventOptions?.toAndroid(),
                confirmText = config.confirmText,
                dismissText = config.dismissText,
                tonalElevation = config.tonalElevation.toFloat(),
                cornerRadius = config.cornerRadius.toFloat(),
                onConfirm = onConfirm,
                onDismiss = onDismiss
            )
        }
    }

    override suspend fun showFullScreenDatePickerDialog(config: DialogConfigDto): CalendarDto? {
        val host = activity
            ?: throw FlutterError("NO_ACTIVITY", "No foreground activity to present over", null)
        return awaitDialog { onConfirm, onDismiss ->
            showNepaliDatePickerFullScreenDialog(
                activity = host,
                initialSelectedDate = config.initialSelectedDate?.toCore(),
                locale = config.locale.toCore(),
                yearRange = config.yearRangeStart.toInt()..config.yearRangeEnd.toInt(),
                selectableDates = config.selectable.toCore(),
                showModeToggle = config.showModeToggle,
                showTodayButton = config.showTodayButton,
                showEnglishDate = config.showEnglishDate,
                englishDateLocale = config.englishDateLocale?.toCore(),
                initialCalendarSystem = calendarSystemOf(config.initialCalendarSystemEra),
                showCalendarSystemToggle = config.showCalendarSystemToggle,
                showAdjacentMonthDays = config.showAdjacentMonthDays,
                events = config.eventOptions?.toAndroid(),
                title = config.title,
                confirmText = config.confirmText,
                dismissText = config.dismissText,
                onConfirm = onConfirm,
                onDismiss = onDismiss
            )
        }
    }

    /**
     * Suspends until the dialog confirms or dismisses, resuming exactly once.
     * A cancelled call, such as a Dart isolate going away, tears the dialog
     * down silently.
     */
    private suspend fun awaitDialog(
        present: (onConfirm: (CustomCalendar?) -> Unit, onDismiss: () -> Unit) -> NepaliDialogHandle
    ): CalendarDto? = suspendCancellableCoroutine { continuation ->
        var completed = false
        val handle = present(
            { date ->
                if (!completed) {
                    completed = true
                    continuation.resume(date?.toDto())
                }
            },
            {
                if (!completed) {
                    completed = true
                    continuation.resume(null)
                }
            }
        )
        continuation.invokeOnCancellation {
            completed = true
            handle.dismiss()
        }
    }

    override fun applyAppearance(appearance: AppearanceDto) {
        NepaliPickerAppearance.brightness =
            NepaliPickerBrightness.entries[appearance.brightness.ordinal]
        NepaliPickerAppearance.primaryArgb = appearance.primaryArgb.toInt()
        NepaliPickerAppearance.onPrimaryArgb = appearance.onPrimaryArgb.toInt()
        NepaliPickerAppearance.primaryContainerArgb = appearance.primaryContainerArgb.toInt()
        NepaliPickerAppearance.onPrimaryContainerArgb = appearance.onPrimaryContainerArgb.toInt()
        NepaliPickerAppearance.secondaryContainerArgb = appearance.secondaryContainerArgb.toInt()
        NepaliPickerAppearance.onSecondaryContainerArgb =
            appearance.onSecondaryContainerArgb.toInt()
        NepaliPickerAppearance.surfaceArgb = appearance.surfaceArgb.toInt()
        NepaliPickerAppearance.onSurfaceArgb = appearance.onSurfaceArgb.toInt()
        NepaliPickerAppearance.surfaceVariantArgb = appearance.surfaceVariantArgb.toInt()
        NepaliPickerAppearance.onSurfaceVariantArgb = appearance.onSurfaceVariantArgb.toInt()
        NepaliPickerAppearance.outlineArgb = appearance.outlineArgb.toInt()
    }

    override fun resetAppearance() {
        NepaliPickerAppearance.reset()
    }

    private fun calendarSystemOf(era: Long) =
        dev.shivathapaa.nepalidatepickerkmp.data.CalendarSystem.fromEra(era.toInt())
            ?: dev.shivathapaa.nepalidatepickerkmp.data.CalendarSystem.BIKRAM_SAMBAT
}

private fun EventOptionsDto.toAndroid(): NepaliEventOptions = NepaliEventOptions(
    weeklyOffDays = weeklyOffDays.map { it.toInt() },
    events = events.map {
        NepaliEventInfo(
            year = it.year.toInt(),
            month = it.month.toInt(),
            dayOfMonth = it.dayOfMonth.toInt(),
            name = it.name,
            kind = NepaliEventKind.entries[it.kind.ordinal],
            closesOffices = it.closesOffices,
            colorArgb = it.colorArgb.toInt(),
            indicate = it.indicate
        )
    },
    markWeeklyOff = markWeeklyOff,
    markEvents = markEvents,
    tintContainer = tintContainer,
    indicateWeeklyOff = indicateWeeklyOff,
    describeEvents = describeEvents,
    weeklyOffColorArgb = weeklyOffColorArgb.toInt(),
    publicHolidayColorArgb = publicHolidayColorArgb.toInt(),
    religiousColorArgb = religiousColorArgb.toInt(),
    regionalColorArgb = regionalColorArgb.toInt(),
    observanceColorArgb = observanceColorArgb.toInt(),
    markedContainerColorArgb = markedContainerColorArgb.toInt()
)
