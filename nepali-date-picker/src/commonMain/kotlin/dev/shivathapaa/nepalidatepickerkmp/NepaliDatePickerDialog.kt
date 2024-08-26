package dev.shivathapaa.nepalidatepickerkmp

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEach
import androidx.compose.ui.util.fastForEachIndexed
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import dev.shivathapaa.nepalidatepickerkmp.calendar_model.NepaliDatePickerColors
import dev.shivathapaa.nepalidatepickerkmp.calendar_model.NepaliDatePickerDefaults
import kotlin.math.max

/**
 * A dialog for displaying a [NepaliDatePicker]. Date pickers let people select a date.
 *
 * @param onDismissRequest called when the user tries to dismiss the Dialog by clicking outside
 * or pressing the back button. This is not called when the dismiss button is clicked.
 * @param confirmButton button which is meant to confirm a proposed action, thus resolving what
 * triggered the dialog. The dialog does not set up any events for this button, nor does it control
 * its enablement, so those need to be set up by the caller.
 * @param modifier the [Modifier] to be applied to this dialog's content.
 * @param dismissButton button which is meant to dismiss the dialog. The dialog does not set up any
 * events for this button so they need to be set up by the caller.
 * @param shape defines the dialog's surface shape as well its shadow
 * @param tonalElevation when [NepaliDatePickerColors.containerColor] is [ColorScheme.surface], a higher
 * the elevation will result in a darker color in light theme and lighter color in dark theme
 * @param colors [NepaliDatePickerColors] that will be used to resolve the colors used for this date
 * picker in different states. See [NepaliDatePickerDefaults.colors].
 * @param properties typically platform specific properties to further configure the dialog
 * @param content the content of the dialog (i.e. a [NepaliDatePicker], for example)
 */
@Composable
fun NepaliDatePickerDialog(
    onDismissRequest: () -> Unit,
    confirmButton: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    dismissButton: @Composable (() -> Unit)? = null,
    shape: Shape = NepaliDatePickerDefaults.shape,
    tonalElevation: Dp = NepaliDatePickerDefaults.TonalElevation,
    colors: NepaliDatePickerColors = NepaliDatePickerDefaults.colors(),
    properties: DialogProperties = DialogProperties(usePlatformDefaultWidth = false),
    content: @Composable ColumnScope.() -> Unit
) {
    NepaliDatePickerBasicDialog(
        onDismissRequest = onDismissRequest,
        modifier = modifier.wrapContentHeight(),
        properties = properties
    ) {
        Surface(
            modifier = Modifier
                .requiredWidth(ContainerWidth)
                .heightIn(max = ContainerHeight),
            shape = shape,
            color = colors.containerColor,
            tonalElevation = tonalElevation,
        ) {
            Column(verticalArrangement = Arrangement.SpaceBetween) {
                content()
                // Buttons
                Box(
                    modifier = Modifier
                        .align(Alignment.End)
                        .padding(NepaliDialogButtonsPadding)
                ) {
                    ProvideContentColorTextStyle(
                        contentColor = MaterialTheme.colorScheme.primary,
                        textStyle = MaterialTheme.typography.labelLarge
                    ) {
                        NepaliDatePickerDialogFlowRow {
                            dismissButton?.invoke()
                            confirmButton()
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NepaliDatePickerBasicDialog(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    properties: DialogProperties = DialogProperties(),
    content: @Composable () -> Unit
) {
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = properties,
    ) {
        Box(
            modifier = modifier
                .sizeIn(minWidth = DialogMinWidth, maxWidth = DialogMaxWidth),
            propagateMinConstraints = true
        ) {
            content()
        }
    }
}

/**
 * Clone of FlowRow from M3 that arranges its children in a horizontal flow with limited
 * customization.
 */
@Composable
private fun NepaliDatePickerDialogFlowRow(
    mainAxisSpacing: Dp = NepaliDialogButtonsMainAxisSpacing,
    crossAxisSpacing: Dp = NepaliDialogButtonsCrossAxisSpacing,
    content: @Composable () -> Unit
) {
    Layout(content) { measurables, constraints ->
        val sequences = mutableListOf<List<Placeable>>()
        val crossAxisSizes = mutableListOf<Int>()
        val crossAxisPositions = mutableListOf<Int>()

        var mainAxisSpace = 0
        var crossAxisSpace = 0

        val currentSequence = mutableListOf<Placeable>()
        var currentMainAxisSize = 0
        var currentCrossAxisSize = 0

        // Return whether the placeable can be added to the current sequence.
        fun canAddToCurrentSequence(placeable: Placeable) =
            currentSequence.isEmpty() || currentMainAxisSize + mainAxisSpacing.roundToPx() +
                    placeable.width <= constraints.maxWidth

        // Store current sequence information and start a new sequence.
        fun startNewSequence() {
            if (sequences.isNotEmpty()) {
                crossAxisSpace += crossAxisSpacing.roundToPx()
            }
            // Ensures that confirming actions appear above dismissive actions.
            sequences.add(0, currentSequence.toList())
            crossAxisSizes += currentCrossAxisSize
            crossAxisPositions += crossAxisSpace

            crossAxisSpace += currentCrossAxisSize
            mainAxisSpace = max(mainAxisSpace, currentMainAxisSize)

            currentSequence.clear()
            currentMainAxisSize = 0
            currentCrossAxisSize = 0
        }

        measurables.fastForEach { measurable ->
            // Ask the child for its preferred size.
            val placeable = measurable.measure(constraints)

            // Start a new sequence if there is not enough space.
            if (!canAddToCurrentSequence(placeable)) startNewSequence()

            // Add the child to the current sequence.
            if (currentSequence.isNotEmpty()) {
                currentMainAxisSize += mainAxisSpacing.roundToPx()
            }
            currentSequence.add(placeable)
            currentMainAxisSize += placeable.width
            currentCrossAxisSize = max(currentCrossAxisSize, placeable.height)
        }

        if (currentSequence.isNotEmpty()) startNewSequence()

        val mainAxisLayoutSize = max(mainAxisSpace, constraints.minWidth)

        val crossAxisLayoutSize = max(crossAxisSpace, constraints.minHeight)

        layout(mainAxisLayoutSize, crossAxisLayoutSize) {
            sequences.fastForEachIndexed { i, placeables ->
                val childrenMainAxisSizes = IntArray(placeables.size) { j ->
                    placeables[j].width +
                            if (j < placeables.lastIndex) mainAxisSpacing.roundToPx() else 0
                }
                val arrangement = Arrangement.End
                val mainAxisPositions = IntArray(childrenMainAxisSizes.size) { 0 }
                with(arrangement) {
                    arrange(
                        mainAxisLayoutSize, childrenMainAxisSizes,
                        layoutDirection, mainAxisPositions
                    )
                }
                placeables.fastForEachIndexed { j, placeable ->
                    placeable.place(
                        x = mainAxisPositions[j],
                        y = crossAxisPositions[i]
                    )
                }
            }
        }
    }
}

internal val DialogMinWidth = 280.dp
internal val DialogMaxWidth = 560.dp
private val NepaliDialogButtonsPadding = PaddingValues(bottom = 8.dp, end = 6.dp)
private val NepaliDialogButtonsMainAxisSpacing = 8.dp
private val NepaliDialogButtonsCrossAxisSpacing = 12.dp