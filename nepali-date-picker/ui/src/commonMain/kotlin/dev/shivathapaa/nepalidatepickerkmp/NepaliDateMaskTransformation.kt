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

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import dev.shivathapaa.nepalidatepickerkmp.calendar_model.NepaliDateConverter.localizeDigits
import dev.shivathapaa.nepalidatepickerkmp.data.DigitScript
import dev.shivathapaa.nepalidatepickerkmp.data.NepaliDateFormatter.Pattern

/**
 * Visual transformation that takes the raw 8-digit text-field state (`20820214`)
 * and renders it with the supplied [pattern]'s delimiters and digit script
 * (`2082/02/14` or `२०८२/०२/१४`).
 *
 * Shared between [NepaliDateTextField] / [NepaliDateField] and the legacy
 * `NepaliDateInputContent` used inside `NepaliDatePickerDialog`. Both surfaces
 * keep the same internal-state model (8 ASCII digits) so cursor handling stays
 * predictable across the codebase.
 */
internal class NepaliDateMaskTransformation(
    private val pattern: Pattern,
    private val digitScript: DigitScript,
) : VisualTransformation {

    private val delim1: Int = if (pattern.yearFirst) 4 else 2
    private val delim2: Int = if (pattern.yearFirst) 6 else 4
    private val fullDigitLen = pattern.digitCount

    private val offsetTranslator = object : OffsetMapping {
        override fun originalToTransformed(offset: Int): Int = when {
            offset <= delim1 -> offset
            offset <= delim2 -> offset + 1
            offset <= fullDigitLen -> offset + 2
            else -> fullDigitLen + 2
        }

        override fun transformedToOriginal(offset: Int): Int = when {
            offset <= delim1 -> offset
            offset <= delim2 + 1 -> offset - 1
            offset <= fullDigitLen + 1 -> offset - 2
            else -> fullDigitLen
        }
    }

    override fun filter(text: AnnotatedString): TransformedText {
        val trimmed = if (text.text.length > fullDigitLen) text.text.substring(0, fullDigitLen) else text.text
        val rendered = buildString(trimmed.length + 2) {
            for ((i, ch) in trimmed.withIndex()) {
                append(ch)
                if (i + 1 == delim1 || i + 1 == delim2) append(pattern.delimiter)
            }
        }
        return TransformedText(AnnotatedString(rendered.localizeDigits(digitScript)), offsetTranslator)
    }
}
