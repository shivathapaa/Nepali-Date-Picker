/*
 * Copyright © 2026 Shiva Thapa (@shivathapaa). All rights reserved.
 *
 * Licensed under the Mozilla Public License, Version 2.0 (the "License").
 */

package dev.shivathapaa.nepalidatepicker

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Star
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import dev.shivathapaa.nepalidatepickerkmp.event.NepaliCalendarEvent
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * Everything this sample knows about an event beyond the day it falls on, carried in
 * [NepaliCalendarEvent.payload] as JSON.
 *
 * The library never looks inside it: a payload is an opaque string, which is what keeps `:core`
 * free of a JSON dependency and lets an app put whatever its own screens need in there. This one
 * carries a description, an icon, a colour, two image links, tags and a venue, which is about as
 * wide as a real product's event record gets.
 */
@Serializable
internal data class SampleEventPayload(
    val description: String,
    val icon: String,
    val accentArgb: Long,
    val bannerUrl: String? = null,
    val iconUrl: String? = null,
    val tags: List<String> = emptyList(),
    val venue: String? = null,
    val organizer: String? = null,
    val link: String? = null
)

/** The payload codec. Compact rather than pretty, since this is what travels on a wire. */
private val payloadJson = Json { ignoreUnknownKeys = true }

/** This payload as the string an event carries. */
internal fun SampleEventPayload.encode(): String = payloadJson.encodeToString(this)

/**
 * The payload an event carries, or `null` when it has none or carries something this sample does
 * not understand. An app owns both ends of this string, so a decode failure means its own data
 * changed shape, never that the library touched it.
 */
internal fun NepaliCalendarEvent.samplePayload(): SampleEventPayload? =
    payload?.let { runCatching { payloadJson.decodeFromString<SampleEventPayload>(it) }.getOrNull() }

/** The accent colour the payload names, for a banner or a chip. */
internal fun SampleEventPayload.accentColor(): Color = Color(accentArgb.toULong() shl 32)

/** The icon the payload names, falling back to a neutral one for anything unrecognized. */
internal fun SampleEventPayload.iconVector(): ImageVector = when (icon) {
    "star" -> Icons.Default.Star
    "place" -> Icons.Default.LocationOn
    "person" -> Icons.Default.AccountCircle
    "reminder" -> Icons.Default.Notifications
    "festival" -> Icons.Default.Favorite
    "shopping" -> Icons.Default.ShoppingCart
    "share" -> Icons.Default.Share
    "date" -> Icons.Default.DateRange
    else -> Icons.Default.Info
}
