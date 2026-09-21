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

package dev.shivathapaa.nepalidatepickerkmp.calendar_model

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/** Diameter of the dot that colours an event line by its kind. */
internal val EventLineDotSize = 8.dp

/** Space between the dot and the text it introduces. */
internal val EventLineDotGap = 8.dp

/** Space between two lines of an event list. */
internal val EventLineGap = 8.dp

/** Space a line keeps from the edges of the container it is drawn in. */
internal val EventLinePadding = 8.dp

/** Width of the leading date column, wide enough for a two-digit day over its weekday name. */
internal val EventLineDateWidth = 44.dp

/**
 * One line of an event list: a coloured dot, the event's name, and an optional second line under it.
 *
 * The dot carries the same colour the grid painted the day in, so a list and the cells above it
 * never disagree about what a kind looks like.
 */
@Composable
internal fun NepaliEventLine(
    label: String,
    dotColor: Color,
    modifier: Modifier = Modifier,
    supportingText: String? = null,
    leadingText: String? = null,
    supportingLeadingText: String? = null
) {
    Row(
        modifier = modifier.fillMaxWidth().padding(vertical = EventLinePadding),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (leadingText != null) {
            Column(
                modifier = Modifier.width(EventLineDateWidth),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = leadingText,
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center
                )
                if (supportingLeadingText != null) {
                    Text(
                        text = supportingLeadingText,
                        style = MaterialTheme.typography.labelSmall,
                        textAlign = TextAlign.Center
                    )
                }
            }
            Spacer(modifier = Modifier.width(EventLineDotGap))
        }
        Spacer(modifier = Modifier.size(EventLineDotSize).background(dotColor, CircleShape))
        Spacer(modifier = Modifier.width(EventLineDotGap))
        Column(verticalArrangement = Arrangement.spacedBy(EventLineGap / 2)) {
            Text(text = label, style = MaterialTheme.typography.bodyMedium)
            if (supportingText != null) {
                Text(text = supportingText, style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}
