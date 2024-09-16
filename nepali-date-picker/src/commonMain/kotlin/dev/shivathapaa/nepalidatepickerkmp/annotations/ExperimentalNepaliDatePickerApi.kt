package dev.shivathapaa.nepalidatepickerkmp.annotations

import kotlin.RequiresOptIn

/**
 * Marks the annotated API as experimental in the Nepali Date Picker library.
 *
 * Experimental APIs might be changed or deprecated in future versions. Deprecated APIs
 * will be marked with the `@Deprecated` annotation and eventually removed, so please
 * plan to migrate away from them.
 *
 * Use experimental APIs with caution and be prepared to adapt your code if necessary.
 *
 * ```
 *@OptIn(NepaliDatePickerExperimentalApi: : class)
 * fun myFunction() {
 * // ... use experimental API here
 * }
 * ```
 */
@RequiresOptIn(message = "This API is experimental and might be changed or removed in the future. Use is carefully!")
@Retention(AnnotationRetention.BINARY)
@Target(
    AnnotationTarget.CLASS,
    AnnotationTarget.FUNCTION,
    AnnotationTarget.PROPERTY,
    AnnotationTarget.FIELD,
    AnnotationTarget.TYPEALIAS
)
annotation class ExperimentalNepaliDatePickerApi