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

package dev.shivathapaa.nepalidatepickerkmp.embed

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity

/**
 * Lays the content out at its natural height and reports that height in density independent
 * points whenever it changes.
 *
 * A picker hosted inside a foreign view hierarchy (a UIKit controller or an Android view host)
 * has no intrinsic size the host can read, so without this the caller has to guess a frame: too
 * short crops the calendar, too tall leaves dead space below a text field.
 *
 * The measured box is nested on purpose. An Android host view hands the composition its full
 * frame as an exact height; the outer box absorbs that, and the inner box, measured with the
 * minimum loosened as any box child is, wraps the content. Reporting the inner size keeps the
 * report equal to the content even when the frame is taller, so a host sizing itself from the
 * report can tighten around a shorter mode instead of echoing its own frame back forever.
 */
@Composable
internal fun MeasuredContent(
    onHeightChange: (Float) -> Unit,
    content: @Composable () -> Unit
) {
    val density = LocalDensity.current
    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.TopCenter) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .onSizeChanged { size ->
                    onHeightChange(size.height / density.density)
                }
        ) {
            content()
        }
    }
}

/**
 * The initial values the hosted factories start from on every platform, restating the defaults of
 * the underlying composables so the two hosts cannot drift apart.
 */
internal object EmbeddedPickerDefaults {
    const val WHEEL_ITEM_HEIGHT = 44f
    const val WHEEL_VISIBLE_ITEM_COUNT = 5
    const val WHEEL_CORNER_RADIUS = 20f
    const val FIELD_CORNER_RADIUS = 4f
    const val DOCKED_POPUP_ELEVATION = 6f
    const val DIALOG_TONAL_ELEVATION = 6f
    const val DIALOG_CORNER_RADIUS = 28f
    const val DIALOG_CONFIRM_TEXT = "OK"
    const val DIALOG_DISMISS_TEXT = "Cancel"
}
