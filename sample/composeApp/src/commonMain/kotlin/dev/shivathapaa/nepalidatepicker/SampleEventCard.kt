/*
 * Copyright © 2026 Shiva Thapa (@shivathapaa). All rights reserved.
 *
 * Licensed under the Mozilla Public License, Version 2.0 (the "License").
 */

package dev.shivathapaa.nepalidatepicker

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.shivathapaa.nepalidatepickerkmp.event.NepaliCalendarEvent

private val CardCornerRadius = 12.dp
private val CardPadding = 12.dp
private val BannerHeight = 56.dp
private val IconSize = 24.dp
private val RowGap = 6.dp
private val ChipGap = 6.dp
private val ChipCornerRadius = 8.dp
private val ChipPadding = 6.dp
private const val BannerAlpha = 0.25f

/**
 * A tapped event as a product would draw it, built from the JSON the event carries in its payload.
 *
 * The library hands back the event it was given, id and payload untouched, and everything below the
 * name here, the colour, the icon, the description, the tags, the venue and the links, is this
 * sample reading its own record out of that string.
 *
 * The image links are printed rather than fetched: loading remote images needs an image library,
 * and the point here is the payload, not the network.
 */
@Composable
fun SampleEventCard(event: NepaliCalendarEvent, modifier: Modifier = Modifier) {
    val payload = event.samplePayload()
    val accent = payload?.accentColor() ?: MaterialTheme.colorScheme.primary

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(CardCornerRadius),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Column {
            Box(
                modifier = Modifier.fillMaxWidth()
                    .height(BannerHeight)
                    .background(accent.copy(alpha = BannerAlpha))
            ) {
                Row(
                    modifier = Modifier.align(Alignment.CenterStart).padding(horizontal = CardPadding),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(RowGap)
                ) {
                    if (payload != null) {
                        Icon(
                            imageVector = payload.iconVector(),
                            contentDescription = null,
                            tint = accent,
                            modifier = Modifier.size(IconSize)
                        )
                    }
                    Column {
                        Text(
                            text = event.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "${event.kind.name} · ${event.date.formatted()}",
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }

            Column(
                modifier = Modifier.padding(CardPadding),
                verticalArrangement = Arrangement.spacedBy(RowGap)
            ) {
                if (payload == null) {
                    Text(
                        text = "This event carries no payload, so there is nothing to read out of it.",
                        style = MaterialTheme.typography.bodySmall
                    )
                    return@Column
                }

                Text(text = payload.description, style = MaterialTheme.typography.bodyMedium)
                payload.venue?.let { PayloadLine(label = "Where", value = it) }
                payload.organizer?.let { PayloadLine(label = "Who", value = it) }
                payload.bannerUrl?.let { PayloadLine(label = "Banner", value = it) }
                payload.iconUrl?.let { PayloadLine(label = "Icon", value = it) }
                payload.link?.let { PayloadLine(label = "Link", value = it) }
                PayloadLine(label = "Correlates to", value = event.id ?: "no id")

                if (payload.tags.isNotEmpty()) {
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(ChipGap)) {
                        payload.tags.forEach { tag -> PayloadChip(tag = tag, accent = accent) }
                    }
                }
            }
        }
    }
}

/** One labelled line of the record, kept to a single line so a long URL does not push the card. */
@Composable
private fun PayloadLine(label: String, value: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(RowGap)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = value,
            style = MaterialTheme.typography.labelSmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/** One of the payload's own tags, drawn in the colour the payload asked for. */
@Composable
private fun PayloadChip(tag: String, accent: Color) {
    Surface(
        shape = RoundedCornerShape(ChipCornerRadius),
        color = accent.copy(alpha = BannerAlpha)
    ) {
        Text(
            text = tag,
            modifier = Modifier.padding(horizontal = ChipPadding, vertical = ChipGap / 2),
            style = MaterialTheme.typography.labelSmall
        )
    }
}
