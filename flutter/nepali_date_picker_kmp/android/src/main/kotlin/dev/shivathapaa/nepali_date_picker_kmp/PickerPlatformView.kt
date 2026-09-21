// Copyright © 2026 Shiva Thapa (@shivathapaa). All rights reserved.
// Use of this source code is governed by the MPL 2.0 license that can be
// found in the LICENSE file.

package dev.shivathapaa.nepali_date_picker_kmp

import android.content.Context
import android.view.View
import dev.shivathapaa.nepalidatepickerkmp.android.NepaliCalendarSystemToggleView
import dev.shivathapaa.nepalidatepickerkmp.android.NepaliCalendarView
import dev.shivathapaa.nepalidatepickerkmp.android.NepaliDateFieldView
import dev.shivathapaa.nepalidatepickerkmp.android.NepaliDatePickerDockedView
import dev.shivathapaa.nepalidatepickerkmp.android.NepaliDatePickerView
import dev.shivathapaa.nepalidatepickerkmp.android.NepaliDateRangeFieldView
import dev.shivathapaa.nepalidatepickerkmp.android.NepaliDateRangePickerView
import dev.shivathapaa.nepalidatepickerkmp.android.NepaliWheelDatePickerView
import io.flutter.plugin.common.StandardMessageCodec
import io.flutter.plugin.platform.PlatformView
import io.flutter.plugin.platform.PlatformViewFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

/** Creates one embedded picker per platform view, keyed by [viewId] for callbacks. */
internal class PickerPlatformViewFactory(
    private val flutterApi: () -> PickerViewFlutterApi
) : PlatformViewFactory(StandardMessageCodec.INSTANCE) {

    override fun create(context: Context, viewId: Int, args: Any?): PlatformView {
        val params = PickerParams(args as? Map<*, *> ?: emptyMap<Any?, Any?>())
        return PickerPlatformView(context, viewId.toLong(), params, flutterApi())
    }
}

/**
 * Hosts one of the library's Android View factories and forwards its
 * callbacks over the Flutter API, tagged with this view's id.
 */
internal class PickerPlatformView(
    context: Context,
    viewId: Long,
    params: PickerParams,
    private val api: PickerViewFlutterApi
) : PlatformView {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    // Failures are swallowed: a view may already be gone when a late event
    // lands, and there is nothing to do about a delivery failure.
    private fun emit(block: suspend PickerViewFlutterApi.() -> Unit) {
        scope.launch { runCatching { api.block() } }
    }

    private val view: View = when (params.variant) {
        "docked" -> NepaliDatePickerDockedView(
            context = context,
            initialSelectedDate = params.initialDate,
            locale = params.locale,
            yearRange = params.yearRange,
            selectableDates = params.selectableDates,
            dateFormatStyle = params.dateFormatStyle,
            showTodayButton = params.bool("showTodayButton", true),
            label = params.string("label"),
            placeholder = params.string("placeholder"),
            initialCalendarSystem = params.calendarSystem,
            showCalendarSystemToggle = params.bool("showCalendarSystemToggle", false),
            showAdjacentMonthDays = params.bool("showAdjacentMonthDays", false),
            events = params.events,
            onHeightChange = { height -> emit { onHeightChanged(viewId, height.toDouble()) } },
            onDateSelected = { date -> emit { onDateSelected(viewId, date?.toDto()) } }
        )

        "wheel" -> NepaliWheelDatePickerView(
            context = context,
            initialDate = params.initialDate,
            locale = params.locale,
            yearRange = params.yearRange,
            selectableDates = params.selectableDates,
            itemHeight = params.float("itemHeight", 44f),
            visibleItemCount = params.int("visibleItemCount", 5),
            initialCalendarSystem = params.calendarSystem,
            showCalendarSystemToggle = params.bool("showCalendarSystemToggle", false),
            onHeightChange = { height -> emit { onHeightChanged(viewId, height.toDouble()) } },
            onDateChange = { date -> emit { onDateSelected(viewId, date.toDto()) } }
        )

        "rangePicker" -> NepaliDateRangePickerView(
            context = context,
            initialSelectedStartDate = params.initialDate,
            initialSelectedEndDate = params.initialEndDate,
            locale = params.locale,
            yearRange = params.yearRange,
            selectableDates = params.selectableDates,
            showModeToggle = params.bool("showModeToggle", true),
            showTodayButton = params.bool("showTodayButton", true),
            showMonthsVertically = params.bool("showMonthsVertically", true),
            showYearPickerAndMonthNavigation =
                params.bool("showYearPickerAndMonthNavigation", true),
            showEnglishDate = params.bool("showEnglishDate", false),
            englishDateLocale = params.englishLocale,
            initialCalendarSystem = params.calendarSystem,
            showCalendarSystemToggle = params.bool("showCalendarSystemToggle", false),
            showAdjacentMonthDays = params.bool("showAdjacentMonthDays", false),
            events = params.events,
            onHeightChange = { height -> emit { onHeightChanged(viewId, height.toDouble()) } },
            onRangeSelected = { start, end ->
                emit { onRangeSelected(viewId, start?.toDto(), end?.toDto()) }
            }
        )

        "dateField" -> NepaliDateFieldView(
            context = context,
            initialValue = params.initialDate,
            locale = params.locale,
            dateFormat = params.dateFormat,
            yearRange = params.yearRange,
            selectableDates = params.selectableDates,
            outlined = params.bool("outlined", true),
            label = params.string("label"),
            placeholder = params.string("placeholder"),
            supportingText = params.string("supportingText"),
            isError = params.bool("isError", false),
            enabled = params.bool("enabled", true),
            readOnly = params.bool("readOnly", false),
            confirmButtonText = params.string("confirmButtonText"),
            dismissButtonText = params.string("dismissButtonText"),
            initialCalendarSystem = params.calendarSystem,
            showCalendarSystemToggle = params.bool("showCalendarSystemToggle", false),
            showAdjacentMonthDays = params.bool("showAdjacentMonthDays", false),
            events = params.events,
            onHeightChange = { height -> emit { onHeightChanged(viewId, height.toDouble()) } },
            onValueChange = { value ->
                emit {
                    onValueChanged(viewId, value?.let {
                        DateDto(it.year.toLong(), it.month.toLong(), it.dayOfMonth.toLong())
                    })
                }
            }
        )

        "rangeField" -> NepaliDateRangeFieldView(
            context = context,
            initialStartValue = params.initialDate,
            initialEndValue = params.initialEndDate,
            locale = params.locale,
            dateFormat = params.dateFormat,
            yearRange = params.yearRange,
            selectableDates = params.selectableDates,
            outlined = params.bool("outlined", true),
            startLabel = params.string("startLabel"),
            endLabel = params.string("endLabel"),
            supportingText = params.string("supportingText"),
            isStartError = params.bool("isStartError", false),
            isEndError = params.bool("isEndError", false),
            enabled = params.bool("enabled", true),
            readOnly = params.bool("readOnly", false),
            confirmButtonText = params.string("confirmButtonText"),
            dismissButtonText = params.string("dismissButtonText"),
            initialCalendarSystem = params.calendarSystem,
            showCalendarSystemToggle = params.bool("showCalendarSystemToggle", false),
            showAdjacentMonthDays = params.bool("showAdjacentMonthDays", false),
            events = params.events,
            onHeightChange = { height -> emit { onHeightChanged(viewId, height.toDouble()) } },
            onRangeChange = { start, end ->
                emit {
                    onRangeValueChanged(
                        viewId,
                        start?.let { DateDto(it.year.toLong(), it.month.toLong(), it.dayOfMonth.toLong()) },
                        end?.let { DateDto(it.year.toLong(), it.month.toLong(), it.dayOfMonth.toLong()) }
                    )
                }
            }
        )

        "calendar" -> NepaliCalendarView(
            context = context,
            initialSelectedDate = params.initialDate,
            locale = params.locale,
            yearRange = params.yearRange,
            showTodayButton = params.bool("showTodayButton", true),
            showCalendarSystemToggle = params.bool("showCalendarSystemToggle", false),
            showAdjacentMonthDays = params.bool("showAdjacentMonthDays", true),
            showSecondaryDates = params.bool("showSecondaryDates", true),
            secondaryDateLocale = params.englishLocale,
            initialCalendarSystem = params.calendarSystem,
            showDaySummary = params.bool("showDaySummary", false),
            showMonthEvents = params.bool("showMonthEvents", false),
            events = params.events,
            onHeightChange = { height -> emit { onHeightChanged(viewId, height.toDouble()) } },
            onEventTapped = { event -> emit { onEventTapped(viewId, event.toDto()) } },
            onDaySelected = { date, _ -> emit { onDateSelected(viewId, date.toDto()) } }
        )

        "calendarSystemToggle" -> NepaliCalendarSystemToggleView(
            context = context,
            initialCalendarSystem = params.calendarSystem,
            language = params.language,
            onHeightChange = { height -> emit { onHeightChanged(viewId, height.toDouble()) } },
            onCalendarSystemChange = { system ->
                emit { onCalendarSystemChanged(viewId, system.era.toLong()) }
            }
        )

        else -> NepaliDatePickerView(
            context = context,
            initialSelectedDate = params.initialDate,
            locale = params.locale,
            yearRange = params.yearRange,
            selectableDates = params.selectableDates,
            showModeToggle = params.bool("showModeToggle", true),
            showTodayButton = params.bool("showTodayButton", true),
            showEnglishDate = params.bool("showEnglishDate", false),
            englishDateLocale = params.englishLocale,
            initialCalendarSystem = params.calendarSystem,
            showCalendarSystemToggle = params.bool("showCalendarSystemToggle", false),
            showAdjacentMonthDays = params.bool("showAdjacentMonthDays", false),
            events = params.events,
            onHeightChange = { height -> emit { onHeightChanged(viewId, height.toDouble()) } },
            onDateSelected = { date -> emit { onDateSelected(viewId, date?.toDto()) } }
        )
    }

    override fun getView(): View = view

    override fun dispose() {
        scope.cancel()
    }
}
