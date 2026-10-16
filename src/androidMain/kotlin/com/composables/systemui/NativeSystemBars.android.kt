package com.composables.systemui

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.graphics.Color
import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

@Composable
internal actual fun NativeSystemBars(appearance: SystemBarAppearance) {
    val view = LocalView.current
    val configuration = LocalConfiguration.current
    DisposableEffect(view, appearance, configuration) {
        val window =
            requireNotNull(view.context.activity()) { "SystemBars requires an Activity window" }
                .window
        val controller = WindowCompat.getInsetsController(window, view)
        val previousStatus = controller.isAppearanceLightStatusBars
        val previousNavigation = controller.isAppearanceLightNavigationBars
        val previousNavigationColor = window.navigationBarColor
        val previousNavigationContrast =
            if (Build.VERSION.SDK_INT >= 29) window.isNavigationBarContrastEnforced else false
        window.navigationBarColor = Color.TRANSPARENT
        if (Build.VERSION.SDK_INT >= 29) window.isNavigationBarContrastEnforced = false
        when (appearance.statusBar) {
            SystemBarIcons.Light -> controller.isAppearanceLightStatusBars = false
            SystemBarIcons.Dark -> controller.isAppearanceLightStatusBars = true
            SystemBarIcons.Default -> Unit
        }
        when (appearance.navigationBar) {
            SystemBarIcons.Light -> controller.isAppearanceLightNavigationBars = false
            SystemBarIcons.Dark -> controller.isAppearanceLightNavigationBars = true
            SystemBarIcons.Default -> Unit
        }
        onDispose {
            window.navigationBarColor = previousNavigationColor
            if (Build.VERSION.SDK_INT >= 29)
                window.isNavigationBarContrastEnforced = previousNavigationContrast
            controller.isAppearanceLightStatusBars = previousStatus
            controller.isAppearanceLightNavigationBars = previousNavigation
        }
    }
}

private tailrec fun Context.activity(): Activity? =
    when (this) {
        is Activity -> this
        is ContextWrapper -> baseContext.activity()
        else -> null
    }
