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

import dev.shivathapaa.nepalidatepickerkmp.calendar_model.NepaliDateConverter.localizeDigits
import dev.shivathapaa.nepalidatepickerkmp.calendar_model.NepaliDateConverter.toLatinDigits
import dev.shivathapaa.nepalidatepickerkmp.data.DigitScript
import dev.shivathapaa.nepalidatepickerkmp.data.NepaliDateLocale
import dev.shivathapaa.nepalidatepickerkmp.data.NepaliDatePickerLang
import dev.shivathapaa.nepalidatepickerkmp.data.defaultDigitScript
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertSame

class DigitScriptTests {

    @Test
    fun localizeDigits_latinIsIdentity_returnsSameInstance() {
        val input = "2082/02/14"
        assertSame(input, input.localizeDigits(DigitScript.LATIN))
    }

    @Test
    fun localizeDigits_devanagariConvertsAsciiDigitsOnly() {
        assertEquals("२०८२/०२/१४", "2082/02/14".localizeDigits(DigitScript.DEVANAGARI))
    }

    @Test
    fun localizeDigits_devanagariKeepsNonDigitsVerbatim() {
        assertEquals("Bai २०८२", "Bai 2082".localizeDigits(DigitScript.DEVANAGARI))
    }

    @Test
    fun localizeDigits_devanagariEmptyString() {
        assertEquals("", "".localizeDigits(DigitScript.DEVANAGARI))
    }

    @Test
    fun localizeDigits_devanagariStringWithoutDigits_unchanged() {
        assertEquals("Baisakh", "Baisakh".localizeDigits(DigitScript.DEVANAGARI))
    }

    @Test
    fun localizeDigits_devanagariStringAlreadyDevanagari_unchanged() {
        assertEquals("२०८२", "२०८२".localizeDigits(DigitScript.DEVANAGARI))
    }

    @Test
    fun toLatinDigits_devanagariDigitsConverted() {
        assertEquals("2082/02/14", "२०८२/०२/१४".toLatinDigits())
    }

    @Test
    fun toLatinDigits_alreadyLatin_returnsSameInstance() {
        val input = "2082/02/14"
        assertSame(input, input.toLatinDigits())
    }

    @Test
    fun toLatinDigits_mixedScriptsBothConverted() {
        assertEquals("v2.0.2082", "v2.0.२०८२".toLatinDigits())
    }

    @Test
    fun toLatinDigits_emptyString() {
        assertEquals("", "".toLatinDigits())
    }

    @Test
    fun localizeDigits_thenToLatin_roundTrip() {
        val original = "Bai 2082/02/14"
        val devanagari = original.localizeDigits(DigitScript.DEVANAGARI)
        assertEquals(original, devanagari.toLatinDigits())
    }

    @Test
    fun localizeDigitsByLocale_usesResolvedScript() {
        val nepali = NepaliDateLocale(language = NepaliDatePickerLang.NEPALI)
        val english = NepaliDateLocale(language = NepaliDatePickerLang.ENGLISH)
        assertEquals("२०८२", "2082".localizeDigits(nepali))
        assertEquals("2082", "2082".localizeDigits(english))
    }

    @Test
    fun localizeDigitsByLocale_explicitOverrideWins() {
        val nepaliWithLatinDigits = NepaliDateLocale(
            language = NepaliDatePickerLang.NEPALI,
            digitScript = DigitScript.LATIN
        )
        assertEquals("2082", "2082".localizeDigits(nepaliWithLatinDigits))
    }

    @Test
    fun nepaliDateLocale_resolvedDigitScript_defaultsByLanguage() {
        assertEquals(DigitScript.LATIN, NepaliDateLocale().resolvedDigitScript)
        assertEquals(
            DigitScript.DEVANAGARI,
            NepaliDateLocale(language = NepaliDatePickerLang.NEPALI).resolvedDigitScript
        )
    }

    @Test
    fun nepaliDateLocale_copyChangesLanguage_digitScriptFollows() {
        // .copy() of an auto-script locale must pick up the new language's default.
        val original = NepaliDateLocale(language = NepaliDatePickerLang.NEPALI)
        assertEquals(DigitScript.DEVANAGARI, original.resolvedDigitScript)
        val switched = original.copy(language = NepaliDatePickerLang.ENGLISH)
        assertEquals(DigitScript.LATIN, switched.resolvedDigitScript)
        assertNull(switched.digitScript)
    }

    @Test
    fun nepaliDateLocale_copyChangesLanguage_explicitOverridePersists() {
        // If the consumer pinned digitScript explicitly, .copy(language=...) must NOT silently flip it.
        val pinned = NepaliDateLocale(
            language = NepaliDatePickerLang.NEPALI,
            digitScript = DigitScript.LATIN
        )
        val switched = pinned.copy(language = NepaliDatePickerLang.ENGLISH)
        assertEquals(DigitScript.LATIN, switched.resolvedDigitScript)
        assertEquals(DigitScript.LATIN, switched.digitScript)
    }

    @Test
    fun defaultDigitScript_englishIsLatin() {
        assertEquals(DigitScript.LATIN, NepaliDatePickerLang.ENGLISH.defaultDigitScript())
    }

    @Test
    fun defaultDigitScript_nepaliIsDevanagari() {
        assertEquals(DigitScript.DEVANAGARI, NepaliDatePickerLang.NEPALI.defaultDigitScript())
    }
}
