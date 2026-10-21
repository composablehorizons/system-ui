package com.composables.systemui

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.graphics.Color
import android.os.Build
import android.view.View
import android.view.ViewParent
import android.view.Window
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.window.DialogWindowProvider
import androidx.core.view.WindowCompat

private val windowRequests = mutableMapOf<Window, WindowSystemBars>()

@Composable
internal actual fun NativeSystemBars(appearance: SystemBarAppearance) {
    val view = LocalView.current
    // Reapply requests after configuration changes without changing their ownership order.
    LocalConfiguration.current
    val window = requireNotNull(view.systemUiWindow()) { "SystemBars requires an Activity or dialog window" }
    val key = remember { Any() }
    DisposableEffect(window, key) {
        val requests = windowRequests.getOrPut(window) { WindowSystemBars(window) }
        requests.update(key, appearance)
        onDispose {
            requests.remove(key)
            if (requests.isEmpty) windowRequests.remove(window)
        }
    }
    SideEffect { windowRequests[window]?.update(key, appearance) }
}

private class WindowSystemBars(private val window: Window) {
    private val controller = WindowCompat.getInsetsController(window, window.decorView)
    private val originalStatus = controller.isAppearanceLightStatusBars
    private val originalNavigation = controller.isAppearanceLightNavigationBars
    private val originalNavigationColor = window.navigationBarColor
    private val originalNavigationContrast =
        if (Build.VERSION.SDK_INT >= 29) window.isNavigationBarContrastEnforced else false
    private val requests = linkedMapOf<Any, SystemBarAppearance>()
    val isEmpty: Boolean get() = requests.isEmpty()

    fun update(key: Any, appearance: SystemBarAppearance) {
        requests[key] = appearance
        applyAppearance()
    }

    fun remove(key: Any) {
        requests.remove(key)
        applyAppearance()
    }

    private fun applyAppearance() {
        window.navigationBarColor = if (isEmpty) originalNavigationColor else Color.TRANSPARENT
        if (Build.VERSION.SDK_INT >= 29) {
            window.isNavigationBarContrastEnforced = if (isEmpty) originalNavigationContrast else false
        }
        val status = requests.values.lastOrNull { it.statusBar != SystemBarIcons.Default }?.statusBar
        val navigation = requests.values.lastOrNull { it.navigationBar != SystemBarIcons.Default }?.navigationBar
        controller.isAppearanceLightStatusBars = when (status) {
            SystemBarIcons.Dark -> true
            SystemBarIcons.Light -> false
            else -> originalStatus
        }
        controller.isAppearanceLightNavigationBars = when (navigation) {
            SystemBarIcons.Dark -> true
            SystemBarIcons.Light -> false
            else -> originalNavigation
        }
    }
}

private fun View.systemUiWindow(): Window? {
    if (this is DialogWindowProvider) return window
    var ancestor: ViewParent? = parent
    while (ancestor != null) {
        if (ancestor is DialogWindowProvider) return ancestor.window
        ancestor = ancestor.parent
    }
    return context.activity()?.window
}

private tailrec fun Context.activity(): Activity? =
    when (this) {
        is Activity -> this
        is ContextWrapper -> baseContext.activity()
        else -> null
    }
