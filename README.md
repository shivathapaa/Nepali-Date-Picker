# Nepali-Date-Picker (Android and/or iOS) - KMP

<p align="center">
  <img src=".github/assets/nepaliDatePickerBanner.png" alt="" width="100%">
</p>

KMP Nepali Date Picker for both Android and/or iOS which aligns with the Material3 Date Picker. This library provides UI and various utilities to work with Nepali Dates, and acts as a bridge between Nepali Calendar and Gregorian Calendar.

<br>

<p align="center">
  <a href="https://github.com/shivathapaa/Nepali-Date-Picker/releases">
    <img alt="version" src="https://img.shields.io/github/v/release/shivathapaa/nepali-date-picker" /></a>&nbsp;
  <a href="https://github.com/shivathapaa/Nepali-Date-Picker/blob/main/LICENSE">
    <img alt="license" src="https://img.shields.io/github/license/shivathapaa/nepali-date-picker?labelColor=F5DDD7&color=E0BFB7"/></a>&nbsp;
  <a href="https://medium.com/@shivathapaa/nepali-date-picker-for-android-and-ios-kotlin-multiplatform-a739ea0caf47">
    <img src="https://img.shields.io/badge/Read%20on-Medium-12100E?logo=medium" alt="Medium"/></a>
</p>
<br>
<p align="center">
<!--     <a href="https://github.com">
    <img alt="Made for community" src="https://img.shields.io/badge/Made%20for%20community-F3FBF7" /></a>&nbsp; -->
  <a href="https://github.com/shivathapaa/Nepali-Date-Picker/releases/download/2.1.0-alpha01/NepaliDatePickerMultiTheme-v2.1.0-alpha01.apk">
    <img alt="Download sample android app" src="https://img.shields.io/badge/download-%20Sample%20Android%20App-3DDC84?logo=android&labelColor=E2E3D8&color=4C662B"></a>
</p>
<br>

<details>
  <summary><b>Table of Contents</b></summary>

* [Design overview](#design-overview)
* [Types/features](#typesfeatures)
* [Operations highlight](#operations-highlight)
* [Using in your projects](#using-in-your-projects)
    * [Common Gradle](#common-gradle)
    * [Android](#android)
    * [iOS](#ios)
    * [Desktop/Web](#desktopweb)
* [License](#license)
* [Support](#support)
* [Screenshots](#screenshots)
* [Brief simple example usage](#brief-simple-example-usage)
* [Detailed examples to explore more](#detailed-examples-to-explore-more)
</details>

## Design overview

This library strictly follows `Material` (Material3) design principles. Considering UI/UX, `nepali-date-picker` aligns with new `androidx.compose.material3.DatePicker`.

If you are familiar with the Material3 Date Picker then you will find it very similar, and you can adapt it with little to no time.

This library puts Nepali Calendar in light as OpenSource for developers involved in both Android and/or iOS and/or KMP.

You can use this library independent to any platform or in common Kotlin Multiplatform code.

## Types/Features

This library provides variety of features for working with date picker. It is not only limited to date picker but many utilities that serves its purpose with extended support for date and time.
Some of them are listed below:

- `CustomCalendar` - Calendar which represents both English and Nepali dates.
- `SimpleDate` and  `SimpleTime` - Simple representation of date and time.
- `NepaliMonthCalendar` - Nepali Month Calendar which consists of the month details.
- `NepaliDateLocale` - To control language, dateFormat, weekDayName, and monthName.
- `NepaliDatePickerLang` - Set of supported language (English & Nepali for now).
- `NepaliDateConverter` - Provides utilities for date conversions (english to nepali and vice versa), get formatted date(6), get time, get date-time in ISO 8601 format, calculate days in between two date, and many more.
- `rememberNepaliDatePickerState()` - To read, write, and manage state of the date picker.
- `NepaliSelectableDates` - To control selectable dates i.e. enable/disable certain dates.
- `NepaliDatePickerColors` - Takes `Material3` ?: **Material** colors by **default**. All the colors it uses are taken from your app colors if you've defined Material colors in your project. Also, there's always `.copy()` to modify the color.

## Operations highlight

Using this library you can get following things done.

#### Getting today's date and current time

```kotlin
NepaliDateConverter.todayNepaliDate

NepaliDateConverter.todayEnglishDate

NepaliDateConverter.currentTime
```

#### Converting English date to Nepali date

```kotlin
NepaliDateConverter.convertEnglishToNepali(2024, 6, 21)
```

#### Converting Nepali date to English date

```kotlin
NepaliDateConverter.convertNepaliToEnglish(2081, 3, 21)
```

#### Other utilities

"This library uses a `1-based index` where 1 represents Sunday or January/Baisakh, 7 represents Saturday or July/Kartik, and 12 represents December/Chaitra."

```kotlin
NepaliDateConverter.getTotalDaysInNepaliMonth(2081, 3)

NepaliDateConverter.getNepaliMonthCalendar(2081, 3)

NepaliDateConverter.getWeekdayName(1)  // Sunday or आईतबार

NepaliDateConverter.getMonthName(1) // Baisakh or बैशाख

NepaliDateConverter.getEnglishMonthName(1) // January

"year 2024 month 06 day 21".localizeNumber(NepaliDatePickerLang.NEPALI) // year २०२४ month ०६ day २१

"year 2024".convertToNepaliNumber() // year २०२४

"year २०२४".convertToEnglishNumber() // year 2024

```

```kotlin
// Convert date and time in ISO 8061 format
val date = SimpleDate(2024, 9, 9)
val time = SimpleTime(10, 30, 45, 0)
val isoDateTime = formatEnglishDateNepaliTimeToIsoFormat(date, time)
println(isoDateTime) // Output: 2024-09-09T10:30:45Z

// Or, use current time or current date
val date = SimpleDate(2024, 5, 24)
val isoDateTime = formatNepaliDateTimeToIsoFormat(date)
println(isoDateTime) // Output: 2024-09-09T23:22:21Z

// Calculate number of day in between two dates
val englishStartDate = SimpleDate(1998, 4, 12)
val englishEndDate = SimpleDate(2024, 9, 21)
val nepaliStartDate = SimpleDate(2054, 12, 30)
val nepaliEndDate = SimpleDate(2081, 6, 5)

val nepaliDaysBetween = NepaliDateConverter.getNepaliDaysInBetween(nepaliStartDate, nepaliEndDate) // Output: 9659
val englishDaysBetween = NepaliDateConverter.getEnglishDaysInBetween(englishStartDate, englishEndDate) // Output: 9659
```

```kotlin
// Date formatter

val customCalendar = CustomCalendar(year = 2080, month = 3, day = 11, weekday = 2, ....)
val locale = NepaliDateLocale(
    language = NepaliDatePickerLang.ENGLISH,
    dateFormat = NepaliDateFormatStyle.FULL, // other 6 options
    weekDayName = NameFormat.FULL,
    monthName = NameFormat.FULL
)
val formattedDate = NepaliDateConverter.formatNepaliDate(customCalendar, locale)
// formattedDate: "Monday, Asar 11, 2080"


// Time formatter

NepaliDateConverter.getFormattedTimeInNepali(NepaliDateConverter.currentTime) // Output: "साँझ ४ : ३०"

NepaliDateConverter.getFormattedTimeInNepali(NepaliDateConverter.currentTime, use12HourFormat = false) // Output: "१६ : ३०"

NepaliDateConverter.getFormattedTimeInEnglish(NepaliDateConverter.currentTime) // Output: "4:30 PM"

NepaliDateConverter.getFormattedTimeInEnglish(NepaliDateConverter.currentTime, use12HourFormat = false) // Output: "16:30"
```

[See all examples to get started](#detailed-examples-to-explore-more)

## Using in your projects

The library is published to [Maven Central. You can find all artifacts here.](https://central.sonatype.com/namespace/io.github.shivathapaa)

This library is almost stable with the release of version `2.0.0-rc01`. However, it currently depends on JetBrains Compose `1.7.0-beta01 or later`.
I expect to release a fully stable version once JetBrains Compose reaches a stable release. In the meantime, `2.0.0-rc01` is considered `stable` and highly reliable for production use.

> If you encounter version conflicts using this library, you can solve this in two ways:
> - Use an earlier version of the Nepali-Date-Picker library (`2.0.0-beta06 or before`) if stability is required and your project is using a lower version of JetBrains Compose or Android Compose.
> - Alternatively, you can update your JetBrains Compose or Android Compose version to `1.7.0-beta01 or later` to resolve the conflict.
    For more details on this release, check [this release](https://github.com/shivathapaa/Nepali-Date-Picker/releases/tag/2.0.0-rc01).

### Common Gradle

In multiplatform projects, add a dependency to the commonMain source set dependencies

```kotlin
kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation("io.github.shivathapaa:nepali-date-picker:2.0.0-rc01")
            }
        }
    }
}
```

### Android

To add the nepali-date-picker library to your Android project, include the following dependency in your module/app-level build.gradle file:

```kotlin
// For app using Kotlin version before 2.0.0

dependencies {
    implementation("io.github.shivathapaa:nepali-date-picker-android:2.0.0-rc01")
}

// Or use version catalog like below
```


```kotlin
// For app using Kotlin version after 2.0.0

// Add the Compose compiler Gradle plugin to the Gradle version catalog
[versions]
# ...
kotlin = "2.0.20"
nepaliDatePickerAndroid = "2.0.0-rc01"

[libraries]
nepali-date-picker-android = { module = "io.github.shivathapaa:nepali-date-picker-android", version.ref = "nepaliDatePickerAndroid" }

[plugins]
# ...
compose-compiler = { id = "org.jetbrains.kotlin.plugin.compose", version.ref = "kotlin" }


// Add the Gradle plugin to the root/project level build.gradle.kts file
plugins {
    // ...
    alias(libs.plugins.compose.compiler) apply false
}


// Apply the plugin and dependency to app level build.gradle.kts file
plugins {
    // ...
    alias(libs.plugins.compose.compiler)
}

dependencies {
    // ...
    implementation(libs.nepali.date.picker.android)
}
```

### iOS

The library supports various iOS architectures, including `iosarm64`, `iossimulatorarm64`, and `iosx64`.

To integrate this library into your iOS project using CocoaPods:

> Will be updated very soon....

> ### Desktop/Web
> Will work on it if it will be helpful to community. [Let me know](https://github.com/shivathapaa/Nepali-Date-Picker/issues/new/choose) if I should!

## License

This project is licensed under [Mozilla Public License 2.0 (MPL 2.0)](https://github.com/shivathapaa/Nepali-Date-Picker/blob/main/LICENSE)
```
Mozilla Public License 2.0 (MPL 2.0)

This project is licensed under the Mozilla Public License 2.0 (MPL 2.0).
MPL 2.0 is a permissive open-source license that allows you to use, modify,
and distribute the code, provided that any modifications to the MPL-licensed
files are also made available under the same license and shared with the community.
This license ensures that improvements to the code remain open and accessible to the community.
```

```
Additional Modification and Distribution Terms

To ensure that improvements to the core library benefit the community,
I would like to emphasize the following:

1. Library Modifications:
Any modifications made to the files of this Library (the "Covered Software") are
subject to the terms of this License. If you modify the Library, you must make the
source code of your modifications available to all recipients of the modified Library
under the terms of this License.

2. Larger Works:
Larger works that incorporate this Library may be licensed under different terms,
provided that modifications to the Library files themselves remain subject to this
License and are made available to all recipients of the modified Library.
```
For more details, see the [LICENSE](https://github.com/shivathapaa/Nepali-Date-Picker/blob/main/LICENSE) file.


## Support

You can contribute to this project in several ways:

- Have an idea for an improvement or a new feature? I'm open to suggestions! Feel free to suggest changes, request enhancements, or report issues [here](https://github.com/shivathapaa/Nepali-Date-Picker/issues/new/choose).
- Share the project with your network to help others discover it.
- Want to contribute directly? You're welcome to open a pull request! Be sure to review the [CONTRIBUTING.md](https://github.com/shivathapaa/Nepali-Date-Picker/blob/main/CONTRIBUTING.md) guide before getting started.
- Show your support by giving this repository a Star⭐. It means a lot! 😊

> Thanks to Google and KMP developers for Material3, Jetpack Compose, kotlinx-datetime and other different Apis. :)

## Screenshots

<p align="center">
  <img src=".github/assets/screenshots/lightGreenInitial.png" alt="Screenshot of Nepali Date Picker" width="20%">&nbsp;
  <img src=".github/assets/screenshots/orangeDarkLongNepali.png" alt="" width="20%">&nbsp;
  <img src=".github/assets/screenshots/redLightShort.png" alt="" width="20%">&nbsp;
  <img src=".github/assets/screenshots/yellowDarkFullNepali.png" alt="" width="20%">
</p>
<br>
<p align="center">
  <img src=".github/assets/screenshots/orangeLightDaySelectableNepali.png"  alt="Screenshot of Selectable Nepali Date Picker" width="20%">&nbsp;
  <img src=".github/assets/screenshots/yellowDarkYearRangeSelectable.png" alt="" width="20%">&nbsp;
  <img src=".github/assets/screenshots/orangeLightYearFull.png" alt="" width="20%">&nbsp;
  <img src=".github/assets/screenshots/greenDarkYearSelectableNepali.png" alt="" width="20%">
</p>
<br>
<p align="center">
  <img src=".github/assets/screenshots/lightGreenCompact.png" alt="Screenshots of Nepali Date Picker" width="11%">&nbsp;
  <img src=".github/assets/screenshots/orangeLightFullNepali.png" alt="" width="11%">&nbsp;
  <img src=".github/assets/screenshots/neoDarkShort.png" alt="" width="11%">&nbsp;
  <img src=".github/assets/screenshots/redLightFull.png" alt="" width="11%">&nbsp;
  <img src=".github/assets/screenshots/blueDarkLong.png" alt="" width="11%">&nbsp;
  <img src=".github/assets/screenshots/redLightShortNepali.png" alt="" width="11%">&nbsp;
  <img src=".github/assets/screenshots/yellowLightNepali.png" alt="" width="11%">
</p>
<br>
<p align="center">
  <img src=".github/assets/screenshots/androidEmulatorLight.png" alt="Screenshots of Nepali Date Picker in simulator" width="9%">&nbsp;
  <img src=".github/assets/screenshots/iosSimulatorLightNepali.png" alt="" width="9%">&nbsp;
  <img src=".github/assets/screenshots/lightGreenCompactNepali.png" alt="" width="11%">&nbsp;
  <img src=".github/assets/screenshots/yellowLight.png" alt="" width="11%">&nbsp;
  <img src=".github/assets/screenshots/iosSimulatorDark.png" alt="" width="9%">&nbsp;
  <img src=".github/assets/screenshots/androidEmulatorDarkNepali.png" alt="" width="9%">
</p>

### In alpha
<p align="center">
  <img src=".github/assets/screenshots/englishDarkWithEnglishAndNepali.png" alt="Screenshots of Nepali Date Picker in simulator" width="18%">&nbsp;
  <img src=".github/assets/screenshots/englishLightWithBothEnglishNepali.png" alt="" width="18%">&nbsp;
  <img src=".github/assets/screenshots/nepaliDarkWithEnglishAndNepali.png" alt="" width="18%">
</p>

##  Brief simple example usage

Don't forget about the **DateRange** before using.

You can check this inside library under `NepaliDatePickerDefaults.NepaliYearRange` and `NepaliDatePickerDefaults.EnglishYearRange`

```
// This will be changed overtime to support wider dates.
EnglishYearRange: IntRange = IntRange(1913, 2043)
NepaliYearRange: IntRange = IntRange(1970, 2100) 
```

#### NepaliDatePicker
```kotlin
val defaultNepaliDatePickerState = rememberNepaliDatePickerState()

NepaliDatePicker(state = defaultNepaliDatePickerState)
```

#### NepaliDatePickerDialog

```kotlin
var showNepaliDatePickerDialog by remember { mutableStateOf(false) }
val defaultNepaliDatePickerState = rememberNepaliDatePickerState()

// You can use provided template or use any composable you like for both confirm and dismiss button
if (showNepaliDatePickerDialog) {
    NepaliDatePickerDialog(
        confirmButton = {
            NepaliDatePickerDefaults.DialogButton(
                text = "OK",
                onButtonClick = { showNepaliDatePickerDialog = false }
            )
        },
        dismissButton = {
            NepaliDatePickerDefaults.DialogButton(
                text = "Cancel",
                onButtonClick = { showNepaliDatePickerDialog = false }
            )
        },
        onDismissRequest = { showNepaliDatePickerDialog = false }
    ) {
        NepaliDatePicker(state = defaultNepaliDatePickerState)
    }
}
```

#### Using rememberNepaliDatePickerState()

```kotlin
val defaultNepaliDatePickerState = rememberNepaliDatePickerState()
val customizedDatePickerState =
    rememberNepaliDatePickerState(
        locale = NepaliDateLocale(language = NepaliDatePickerLang.NEPALI),
        nepaliSelectableDates = object : NepaliSelectableDates {
            override fun isSelectableDate(customCalendar: CustomCalendar)
                    : Boolean {
                return customCalendar.dayOfWeek != 7
                        || customCalendar.dayOfMonth != 12
            }

            override fun isSelectableYear(year: Int): Boolean {
                return (year % 5 != 0)
            }
        }
    )

// Or you can utilize helper function (BeforeDate or AfterDate) to disable and enable dates
val datePickerStateWithDateLimiter =
    rememberNepaliDatePickerState(
        nepaliSelectableDates = NepaliDatePickerDefaults.BeforeDate(SimpleDate(2081, 3, 21))
    )

// For Range, minDate and maxDate should make sense i.e., minDate should be less than or equal to maxDate
val nepaliDatePickerStateWithRangeSelectable = rememberNepaliDatePickerState(
    nepaliSelectableDates = DateRangeSelectableDates(
        SimpleDate(2081, 2, 11),
        SimpleDate(2082, 1, 29)
    )
)

NepaliDatePicker(state = defaultNepaliDatePickerState)

NepaliDatePicker(
    state = customizedDatePickerState,
    colors = NepaliDatePickerDefaults.colors().copy(
        containerColor = MaterialTheme.colorScheme.surface
    )
)

NepaliDatePicker(state = datePickerStateWithDateLimiter)

NepaliDatePicker(state = nepaliDatePickerStateWithRangeSelectable)
```

## Detailed examples to explore more
Here are some examples to help you get started. The library's documentation provides further, detailed explanations.

Don't worry, it's not too complex! In the examples below, I've utilized various customization options to showcase multiple use cases, which might seem overwhelming. However, your specific needs may not require all of these options.

For basic use cases, [refer to the section above](#brief-simple-example-usage).

#### Nepali Date Picker without dialog
```kotlin
// Simple use without dialog
NepaliDatePicker(rememberNepaliDatePickerState())

// Defining state in variable
val datePickerState = rememberNepaliDatePickerState()

NepaliDatePicker(datePickerState)

// Customizing color
NepaliDatePicker(
    state = rememberNepaliDatePickerState(),
    colors = NepaliDatePickerDefaults.colors()
        .copy(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
            dayContentColor = MaterialTheme.colorScheme.onSurface
        )
)
```

#### Using with dialog
```kotlin
// Using with dialog
var showNepaliDatePickerDialog by remember { mutableStateOf(false) }
val nepaliDatePickerState = rememberNepaliDatePickerState(locale = NepaliDateLocale(language = NepaliDatePickerLang.NEPALI))

Button(onClick = { showNepaliDatePickerDialog = true }) { Text(text = "Show Dialog") }

// You can use provided template or use any composable you like for both confirm and dismiss button
if (showNepaliDatePickerDialog) {
    NepaliDatePickerDialog(
        onDismissRequest = { showNepaliDatePickerDialog = false },
        confirmButton = {
            NepaliDatePickerDefaults.DialogButton(
                text = "OK",
                onButtonClick = { showNepaliDatePickerDialog = false }
            )
        },
        dismissButton = {
            NepaliDatePickerDefaults.DialogButton(
                text = "Cancel",
                onButtonClick = { showNepaliDatePickerDialog = false }
            )
        }
    ) {
        NepaliDatePicker(state = nepaliDatePickerState)
    }
}
```

#### Using rememberNepaliDatePickerState() for different cases
```kotlin
// Using rememberNepaliDatePickerState() for different cases 
val todayNepaliDate = NepaliDateConverter.todayNepaliDate

// Remember that, "BeforeSelectable", "AfterSelectable", and "RangeSelectable" are helper Selectables
// that helps with enabling and disabling dates before or after today, or before or
// after certain dates, or in between certain dates, and many other use cases.
// You have full control of the dates that you want to enable or disable.

// Using Before Selectable
val customDatePickerStateWithBeforeSelectable = rememberNepaliDatePickerState(
    initialSelectedDate = SimpleDate(2080, 3, 21),
    initialDisplayedMonth = SimpleDate(2081, 1, 1),
    yearRange = IntRange(1998, 2100),
    nepaliSelectableDates = NepaliDateConverter.BeforeDateSelectable(
        simpleDate = SimpleDate(
            todayNepaliDate.year,
            todayNepaliDate.month,
            todayNepaliDate.dayOfMonth
        ), includeDate = true
    ),
    locale = NepaliDateLocale(
        language = NepaliDatePickerLang.NEPALI,
        dateFormat = NepaliDateFormatStyle.SHORT_YMD,
        weekDayName = NameFormat.MEDIUM,
        monthName = NameFormat.FULL
    )
)

// Using After selectable
val customDatePickerStateWithAfterSelectable = rememberNepaliDatePickerState(
    yearRange = IntRange(1979, 2094),
    nepaliSelectableDates = NepaliDateConverter.AfterDateSelectable(
        simpleDate = SimpleDate(
            todayNepaliDate.year, todayNepaliDate.month, todayNepaliDate.dayOfMonth
        ),
        includeDate = false
    ),
    locale = NepaliDateLocale(
        language = NepaliDatePickerLang.ENGLISH,
        dateFormat = NepaliDateFormatStyle.COMPACT_YMD,
        weekDayName = NameFormat.FULL
    )
)

// Using Range selectable
val customDatePickerStateWithRangeSelectable = rememberNepaliDatePickerState(
    initialDisplayedMonth = SimpleDate(2081, 12, 12),
    yearRange = IntRange(2079, 2090),
    nepaliSelectableDates = NepaliDateConverter.DateRangeSelectable(
        minDate = SimpleDate(
            todayNepaliDate.year, todayNepaliDate.month, todayNepaliDate.dayOfMonth
        ),
        maxDate = SimpleDate(2090, 1, 1),
        includeMinDate = false,
        includeMaxDate = true
    ),
    locale = NepaliDateLocale(
        language = NepaliDatePickerLang.ENGLISH,
        dateFormat = NepaliDateFormatStyle.COMPACT_YMD,
        weekDayName = NameFormat.FULL
    )
)

// Using your own selectable preferences
val customSelectableDatePickerState = rememberNepaliDatePickerState(
    nepaliSelectableDates = object : NepaliSelectableDates {
        override fun isSelectableDate(customCalendar: CustomCalendar): Boolean {
            return customCalendar.month != 3 || (customCalendar.dayOfWeek != 7 && customCalendar.dayOfMonth != 15)
        }

        override fun isSelectableYear(year: Int): Boolean {
            return year >= 2054
        }
    }
)
```

#### Get today's date
```kotlin
// Get today's date
val todayNepaliDate = NepaliDateConverter.todayNepaliDate

val todayEnglishDate = NepaliDateConverter.todayEnglishDate
```

#### Get current time
```kotlin
// Get current time
val currentTime = NepaliDateConverter.currentTime
```

#### Date conversions
```kotlin
// Date conversions
val convertedNepaliDate = NepaliDateConverter.convertEnglishToNepali(2021, 6, 21)

val convertedEnglishDate = NepaliDateConverter.convertNepaliToEnglish(2081, 3, 21)
```

#### Get month details
```kotlin
// Get month details
val totalDaysInMagh2081 = NepaliDateConverter.getTotalDaysInNepaliMonth(2081, 10)

val getCompleteDetailsOfAsar2078Month = NepaliDateConverter.getNepaliMonthCalendar(2078, 3)
```

#### Date comparison
```kotlin
// Date comparison
val compareDate = NepaliDateConverter.compareDates(convertedNepaliDate, SimpleDate(2090, 2, 12))
```

#### Get number of days between two days
```kotlin
// Get number of days between two days
val noOfDaysBetweenTwoNepaliDates = NepaliDateConverter.getNepaliDaysInBetween(SimpleDate(1998, 11, 23), SimpleDate(2098, 4, 21))

val noOfDaysBetweenTwoEnglishDates = NepaliDateConverter.getEnglishDaysInBetween(SimpleDate(2009, 6, 21), SimpleDate(2500, 3, 23))
```

#### Format date time into ISO 2601 UTC to save date in database or have reference
```kotlin
// Format date time into ISO 2601 UTC to save date in database or have reference for other timezone calculations
val currentTime = NepaliDateConverter.currentTime
val todayEnglishDate = NepaliDateConverter.todayEnglishDate
val todayNepaliDate = NepaliDateConverter.todayNepaliDate

val formattedEnglishDate = NepaliDateConverter.formatEnglishDateNepaliTimeToIsoFormat(todayEnglishDate, currentTime)
val formattedNepaliDate = NepaliDateConverter.formatNepaliDateTimeToIsoFormat(todayNepaliDate.toSimpleDate(), currentTime)
```

#### Get names of the weekdays, and month according to your choice
```kotlin
// Get names of the weekdays, and month according to your choice
val weekday = NepaliDateConverter.getWeekdayName(2, NameFormat.FULL, NepaliDatePickerLang.NEPALI)
val weekdayEnglish = NepaliDateConverter.getWeekdayName(5, NameFormat.SHORT, NepaliDatePickerLang.ENGLISH)

val nepaliMonthName = NepaliDateConverter.getMonthName(12, NameFormat.FULL, NepaliDatePickerLang.NEPALI)
val nepaliMonthNameInEnglish = NepaliDateConverter.getMonthName(3, NameFormat.SHORT, NepaliDatePickerLang.ENGLISH)

val englishMonthName = NepaliDateConverter.getMonthName(6, NameFormat.FULL)
```

#### Format date to make ready for UI
```kotlin
// Format date to make ready for UI
val currentTime = NepaliDateConverter.currentTime
val todayEnglishDate = NepaliDateConverter.todayEnglishDate
val todayNepaliDate = NepaliDateConverter.todayNepaliDate

val customFormatLocale = NepaliDateLocale(
    language = NepaliDatePickerLang.ENGLISH,
    dateFormat = NepaliDateFormatStyle.MEDIUM,
    weekDayName = NameFormat.MEDIUM,
    monthName = NameFormat.FULL
)

val nepaliFormattedDate = NepaliDateConverter.formatNepaliDate(todayNepaliDate, customFormatLocale)
val nepaliDefaultFormattedDate = NepaliDateConverter.formatNepaliDate(todayNepaliDate, NepaliDatePickerDefaults.DefaultLocale) // or simply, NepaliDateConverter.formatNepaliDate(todayNepaliDate)
val formattedNepaliDate = NepaliDateConverter.formatNepaliDate(2081, 3, 21, 5, NepaliDatePickerDefaults.DefaultLocale) // or simply, NepaliDateConverter.formatNepaliDate(todayNepaliDate)
//    val englishFormattedDate = NepaliDateConverter.formatEnglishDate(todayEnglishDate, customFormatLocale)
```

#### Format time to make ready for UI
```kotlin
// Format time to make ready for UI
val formattedNepaliTime = NepaliDateConverter.getFormattedTimeInNepali(simpleTime = currentTime, use12HourFormat = false)
val formattedEnglishTime = NepaliDateConverter.getFormattedTimeInEnglish(simpleTime = currentTime, use12HourFormat = true)
```

#### Localize strings to English or Nepali
```kotlin
// Localize strings to English or Nepali
val nepaliString = "Today is 2024".convertToNepaliNumber() // Today is २०२४
val nepaliStringOnlyDigits = "2024".convertToNepaliNumber() // २०२४
val englishString = "२०२४ सोमबार".convertToEnglishNumber() // 2024 सोमबार

val localizeString = "Today is 2024".localizeNumber(NepaliDatePickerLang.NEPALI) // Today is २०२४
```

#### Replace delimiter for displaying or saving as you prefer
```kotlin
// Replace delimiter for displaying or saving as you prefer
val originalDate = "2024/06/21"
val newDelimiter = "-"
val formattedDate = NepaliDateConverter.replaceDelimiter(originalDate, newDelimiter)
// formattedDate: "2024-06-21"

val originalDate = "२०२४/०६/२१"
val newDelimiter = "-"
val formattedDate = NepaliDateConverter.replaceDelimiter(originalDate, newDelimiter)
// formattedDate: "२०२४-०६-२१"

val originalTime = "09:45 AM"
val newDelimiterSpace = " "
val oldDelimiter = ":"
val formattedTimeWithSpace = NepaliDateConverter.replaceDelimiter(originalTime, newDelimiterSpace, oldDelimiter)
// formattedTimeWithSpace: "09 45 AM"
```


And there is always more to explore... ;)

---

Thank you for star! 😉
