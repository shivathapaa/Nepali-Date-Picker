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

package dev.shivathapaa.nepalidatepickerkmp.data

/**
 * Numeral script used when rendering digits in localized dates / times.
 *
 * Each entry holds the ten code points for digits 0..9 in that script. Use
 * `String.localizeDigits(script)` (in `NepaliDateConverter`) to convert any
 * Latin-digit substring to the chosen script, and `String.toLatinDigits()`
 * to go the other way.
 *
 * Devanagari is shared by Nepali, Hindi, Marathi, Maithili, Bhojpuri, and
 * Newari — picking [DEVANAGARI] is enough for all of those locales.
 */
enum class DigitScript(internal val digits: CharArray) {
    /** ASCII `0123456789`. Default for `NepaliDatePickerLang.ENGLISH`. */
    LATIN(charArrayOf('0', '1', '2', '3', '4', '5', '6', '7', '8', '9')),

    /** Devanagari `०१२३४५६७८९` (U+0966..U+096F). Default for `NepaliDatePickerLang.NEPALI`. */
    DEVANAGARI(charArrayOf('०', '१', '२', '३', '४', '५', '६', '७', '८', '९'));
}

/**
 * Default [DigitScript] for a given [NepaliDatePickerLang].
 *
 * Use this when you want the conventional script for a language. To override
 * (e.g. show Nepali month names with Latin digits), pass `digitScript` to
 * [NepaliDateLocale] explicitly.
 */
fun NepaliDatePickerLang.defaultDigitScript(): DigitScript = when (this) {
    NepaliDatePickerLang.ENGLISH -> DigitScript.LATIN
    NepaliDatePickerLang.NEPALI -> DigitScript.DEVANAGARI
}

/**
 * Reverse lookup. If this char is a digit in any supported non-Latin script
 * (Devanagari today), return the matching ASCII `'0'..'9'`. If it is already
 * an ASCII digit, return it unchanged. Otherwise return `null`.
 *
 * Public because UI text-field code needs to fold input digits to a single
 * canonical form before parsing — calling `String.toLatinDigits()` for one char
 * would allocate.
 */
fun Char.latinDigitOrNull(): Char? {
    if (this in '0'..'9') return this
    // DigitScript.entries[0] = LATIN; skip it.
    for (i in 1 until DigitScript.entries.size) {
        val table = DigitScript.entries[i].digits
        val idx = table.indexOf(this)
        if (idx >= 0) return '0' + idx
    }
    return null
}
