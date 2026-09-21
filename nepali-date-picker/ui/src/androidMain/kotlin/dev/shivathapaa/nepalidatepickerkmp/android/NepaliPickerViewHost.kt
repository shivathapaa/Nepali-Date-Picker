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

package dev.shivathapaa.nepalidatepickerkmp.android

import android.app.Activity
import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Recomposer
import androidx.compose.ui.platform.AndroidUiDispatcher
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.findViewTreeSavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import dev.shivathapaa.nepalidatepickerkmp.NepaliSelectableDates
import dev.shivathapaa.nepalidatepickerkmp.calendar_model.NepaliDatePickerDefaults
import dev.shivathapaa.nepalidatepickerkmp.embed.MeasuredContent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

/**
 * Wraps a picker in the Material theme and a surface, hosted in a [ComposeView] that works in any
 * view hierarchy.
 *
 * The theme comes from [NepaliPickerAppearance], so the view follows the device's dark-mode
 * setting and any colours the app has set, and repaints when either changes.
 *
 * The content wraps its own height rather than filling the host, and reports that height in
 * density independent points through [onHeightChange]; a host that cannot measure Compose content
 * itself, such as a Flutter platform view, sizes its frame from that. Use [nepaliDialogOverlay]
 * for a dialog.
 */
internal fun nepaliPickerView(
    context: Context,
    onHeightChange: (Float) -> Unit,
    content: @Composable () -> Unit
): ComposeView = ComposeView(context).apply {
    ensureViewTreeOwners()
    setContent {
        MaterialTheme(colorScheme = appearanceColorScheme()) {
            Surface {
                MeasuredContent(onHeightChange, content)
            }
        }
    }
}

/**
 * Hosts a dialog composable in an invisible overlay attached to the activity's decor, themed from
 * [NepaliPickerAppearance] like the inline pickers.
 *
 * A Compose dialog opens its own window above every view, so the overlay itself never paints;
 * it exists only to give the dialog a composition to live in. The returned handle detaches the
 * overlay without invoking any caller callback, for a host tearing down while the dialog is up.
 */
internal fun nepaliDialogOverlay(
    activity: Activity,
    content: @Composable (dismissOverlay: () -> Unit) -> Unit
): NepaliDialogHandle {
    val decor = activity.window.decorView as ViewGroup
    val host = ComposeView(activity)
    host.ensureViewTreeOwners()

    var detached = false
    val detach = {
        if (!detached) {
            detached = true
            host.post { decor.removeView(host) }
        }
    }

    host.setContent {
        MaterialTheme(colorScheme = appearanceColorScheme()) {
            content(detach)
        }
    }
    decor.addView(host, ViewGroup.LayoutParams(0, 0))

    return NepaliDialogHandle { detach() }
}

/**
 * Resolves the selectable-date policy for a factory parameter, treating `null` as "every date is
 * selectable", the same reading the iOS factories give it.
 */
internal fun NepaliSelectableDates?.orAllDates(): NepaliSelectableDates =
    this ?: NepaliDatePickerDefaults.AllDates

/**
 * Attaches lifecycle and saved-state owners to the view when its window provides none, and gives
 * the composition its own recomposer.
 *
 * A [ComposeView] refuses to compose without both owners in its view tree, and Compose's shared
 * window recomposer reads the lifecycle from the window's content view, which a plain activity,
 * such as the one a Flutter app runs in, never provides. This shim drives a synthetic lifecycle
 * from the view's own attach and detach, and installs a view-local recomposer so the window is
 * never consulted. A view whose tree already has owners is left alone.
 */
private fun ComposeView.ensureViewTreeOwners() {
    if (findViewTreeLifecycleOwner() != null && findViewTreeSavedStateRegistryOwner() != null) {
        return
    }
    val owner = ViewAttachmentOwner()
    setViewTreeLifecycleOwner(owner)
    setViewTreeSavedStateRegistryOwner(owner)
    addOnAttachStateChangeListener(owner)

    val recomposer = Recomposer(AndroidUiDispatcher.CurrentThread)
    setParentCompositionContext(recomposer)
    val recomposeScope = CoroutineScope(AndroidUiDispatcher.CurrentThread + SupervisorJob())
    recomposeScope.launch { recomposer.runRecomposeAndApplyChanges() }
    addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
        override fun onViewAttachedToWindow(view: View) = Unit

        override fun onViewDetachedFromWindow(view: View) {
            recomposer.cancel()
            recomposeScope.cancel()
        }
    })

    if (isAttachedToWindow) {
        owner.onViewAttachedToWindow(this)
    }
}

/**
 * A lifecycle that mirrors one view's time in a window: resumed while attached, destroyed on
 * detach. The shim only ever backs a hosted view that is created for a single embedding and
 * thrown away with it, so detach is the end of its life.
 */
private class ViewAttachmentOwner :
    LifecycleOwner, SavedStateRegistryOwner, View.OnAttachStateChangeListener {

    private val lifecycleRegistry = LifecycleRegistry(this)
    private val savedStateController = SavedStateRegistryController.create(this)

    init {
        savedStateController.performRestore(null)
    }

    override val lifecycle: Lifecycle
        get() = lifecycleRegistry

    override val savedStateRegistry: SavedStateRegistry
        get() = savedStateController.savedStateRegistry

    override fun onViewAttachedToWindow(v: View) {
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
    }

    override fun onViewDetachedFromWindow(v: View) {
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    }
}
