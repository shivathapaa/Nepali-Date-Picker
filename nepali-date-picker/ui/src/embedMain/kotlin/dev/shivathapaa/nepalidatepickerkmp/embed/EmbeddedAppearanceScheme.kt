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

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp

/**
 * One appearance proxy's ARGB slots, as both platform proxies hold them: `0` in any slot means
 * "use Material's own value for this role".
 */
internal class EmbeddedAppearanceSlots(
    val primaryArgb: Int,
    val onPrimaryArgb: Int,
    val primaryContainerArgb: Int,
    val onPrimaryContainerArgb: Int,
    val secondaryContainerArgb: Int,
    val onSecondaryContainerArgb: Int,
    val surfaceArgb: Int,
    val onSurfaceArgb: Int,
    val surfaceVariantArgb: Int,
    val onSurfaceVariantArgb: Int,
    val outlineArgb: Int
)

/**
 * The scheme a hosted picker renders with, for one resolved brightness and one set of slots.
 *
 * Every non-zero slot lands on the role it names; the accent also drives the surface tint and the
 * surface pair also drives the background pair. When both the surface and the surface variant are
 * supplied, the surface container tones Material uses for dialogs, menus and cards are
 * interpolated between them. Without that a themed app keeps Material's own neutral behind its
 * dialogs, which reads as a different product from the calendar inside it.
 */
internal fun derivedPickerColorScheme(
    dark: Boolean,
    slots: EmbeddedAppearanceSlots
): ColorScheme {
    val base = if (dark) darkColorScheme() else lightColorScheme()

    val surface = slots.surfaceArgb.orElse(base.surface)
    val surfaceVariant = slots.surfaceVariantArgb.orElse(base.surfaceVariant)
    val themedSurfaces = slots.surfaceArgb != 0 && slots.surfaceVariantArgb != 0

    val scheme = base.copy(
        primary = slots.primaryArgb.orElse(base.primary),
        onPrimary = slots.onPrimaryArgb.orElse(base.onPrimary),
        primaryContainer = slots.primaryContainerArgb.orElse(base.primaryContainer),
        onPrimaryContainer = slots.onPrimaryContainerArgb.orElse(base.onPrimaryContainer),
        secondaryContainer = slots.secondaryContainerArgb.orElse(base.secondaryContainer),
        onSecondaryContainer = slots.onSecondaryContainerArgb.orElse(base.onSecondaryContainer),
        surface = surface,
        onSurface = slots.onSurfaceArgb.orElse(base.onSurface),
        surfaceVariant = surfaceVariant,
        onSurfaceVariant = slots.onSurfaceVariantArgb.orElse(base.onSurfaceVariant),
        background = surface,
        onBackground = slots.onSurfaceArgb.orElse(base.onBackground),
        surfaceTint = slots.primaryArgb.orElse(base.surfaceTint),
        outline = slots.outlineArgb.orElse(base.outline)
    )

    return if (themedSurfaces) scheme.withDerivedContainers(surface, surfaceVariant, dark) else scheme
}

/**
 * Fills the surface container family from the two surfaces, on the same neutral ramp Material
 * places those tones on.
 */
private fun ColorScheme.withDerivedContainers(
    surface: Color,
    surfaceVariant: Color,
    dark: Boolean
): ColorScheme = if (dark) {
    copy(
        surfaceBright = lerp(surface, surfaceVariant, 0.75f),
        surfaceDim = surface,
        surfaceContainerLowest = lerp(surface, Color.Black, 0.3f),
        surfaceContainerLow = lerp(surface, surfaceVariant, 0.17f),
        surfaceContainer = lerp(surface, surfaceVariant, 0.25f),
        surfaceContainerHigh = lerp(surface, surfaceVariant, 0.46f),
        surfaceContainerHighest = lerp(surface, surfaceVariant, 0.67f)
    )
} else {
    copy(
        surfaceBright = surface,
        surfaceDim = lerp(surface, surfaceVariant, 0.85f),
        surfaceContainerLowest = Color.White,
        surfaceContainerLow = lerp(surface, surfaceVariant, 0.25f),
        surfaceContainer = lerp(surface, surfaceVariant, 0.45f),
        surfaceContainerHigh = lerp(surface, surfaceVariant, 0.65f),
        surfaceContainerHighest = surfaceVariant
    )
}

/** The ARGB value as a colour, or [fallback] when the slot was left at zero. */
private fun Int.orElse(fallback: Color): Color = if (this == 0) fallback else Color(this)
