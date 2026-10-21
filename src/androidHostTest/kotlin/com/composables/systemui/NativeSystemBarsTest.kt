package com.composables.systemui

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.graphics.Color
import android.view.Window
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogWindowProvider
import androidx.core.view.WindowCompat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class NativeSystemBarsTest {
    @get:Rule val compose = createComposeRule()

    @Test fun closingLightOverrideRestoresDarkScreenIcons() {
        val showOverlay = mutableStateOf(false)
        val screenIcons = mutableStateOf(SystemBarIcons.Dark)
        lateinit var activity: Activity
        compose.setContent {
            val view = LocalView.current
            SideEffect { activity = view.context.activity() }
            SystemBars(screenIcons.value, screenIcons.value)
            if (showOverlay.value) SystemBars(SystemBarIcons.Light, SystemBarIcons.Light)
        }
        fun assertDark(dark: Boolean) = compose.runOnIdle {
            val controller = WindowCompat.getInsetsController(activity.window, activity.window.decorView)
            if (dark) {
                assertTrue(controller.isAppearanceLightStatusBars)
                assertTrue(controller.isAppearanceLightNavigationBars)
            } else {
                assertFalse(controller.isAppearanceLightStatusBars)
                assertFalse(controller.isAppearanceLightNavigationBars)
            }
        }
        assertDark(true)
        compose.runOnIdle { showOverlay.value = true }
        assertDark(false)
        compose.runOnIdle { screenIcons.value = SystemBarIcons.Light }
        assertDark(false)
        compose.runOnIdle { screenIcons.value = SystemBarIcons.Dark }
        assertDark(false)
        compose.runOnIdle { showOverlay.value = false }
        assertDark(true)
    }

    @Test fun disposingOutgoingScreenCannotOverwriteTheCurrentScreen() {
        val showOutgoing = mutableStateOf(false)
        val showCurrent = mutableStateOf(false)
        lateinit var activity: Activity
        compose.setContent {
            val view = LocalView.current
            SideEffect { activity = view.context.activity() }
            if (showOutgoing.value) SystemBars(SystemBarIcons.Dark, SystemBarIcons.Dark)
            if (showCurrent.value) SystemBars(SystemBarIcons.Dark, SystemBarIcons.Dark)
        }
        compose.runOnIdle {
            val controller = WindowCompat.getInsetsController(activity.window, activity.window.decorView)
            controller.isAppearanceLightStatusBars = false
            controller.isAppearanceLightNavigationBars = false
            activity.window.navigationBarColor = Color.BLUE
            activity.window.isNavigationBarContrastEnforced = true
            showOutgoing.value = true
        }
        compose.runOnIdle { showCurrent.value = true }
        compose.runOnIdle { showOutgoing.value = false }
        compose.runOnIdle {
            val controller = WindowCompat.getInsetsController(activity.window, activity.window.decorView)
            assertTrue(controller.isAppearanceLightStatusBars, "The current screen still requests dark icons")
            assertTrue(controller.isAppearanceLightNavigationBars, "The current screen still requests dark icons")
            assertEquals(Color.TRANSPARENT, activity.window.navigationBarColor)
            assertFalse(activity.window.isNavigationBarContrastEnforced)
        }
        compose.runOnIdle { showCurrent.value = false }
        compose.runOnIdle {
            val controller = WindowCompat.getInsetsController(activity.window, activity.window.decorView)
            assertFalse(controller.isAppearanceLightStatusBars, "Restore the original native appearance")
            assertFalse(controller.isAppearanceLightNavigationBars, "Restore the original native appearance")
            assertEquals(Color.BLUE, activity.window.navigationBarColor)
            assertTrue(activity.window.isNavigationBarContrastEnforced)
        }
    }

    @Test fun lightDialogIconsDoNotChangeTheActivityWindow() {
        val showDialog = mutableStateOf(false)
        lateinit var activity: Activity
        lateinit var dialogWindow: Window
        compose.setContent {
            val view = LocalView.current
            SideEffect { activity = view.context.activity() }
            SystemBars(SystemBarIcons.Dark, SystemBarIcons.Dark)
            if (showDialog.value) {
                Dialog(onDismissRequest = { showDialog.value = false }) {
                    val dialogView = LocalView.current
                    SideEffect { dialogWindow = (dialogView.parent as DialogWindowProvider).window }
                    SystemBars(SystemBarIcons.Light, SystemBarIcons.Light)
                }
            }
        }
        compose.runOnIdle { showDialog.value = true }
        compose.runOnIdle {
            val activityController = WindowCompat.getInsetsController(activity.window, activity.window.decorView)
            val dialogController = WindowCompat.getInsetsController(dialogWindow, dialogWindow.decorView)
            assertTrue(activityController.isAppearanceLightStatusBars)
            assertTrue(activityController.isAppearanceLightNavigationBars)
            assertFalse(dialogController.isAppearanceLightStatusBars)
            assertFalse(dialogController.isAppearanceLightNavigationBars)
        }
        compose.runOnIdle { showDialog.value = false }
        compose.runOnIdle {
            val controller = WindowCompat.getInsetsController(activity.window, activity.window.decorView)
            assertTrue(controller.isAppearanceLightStatusBars)
            assertTrue(controller.isAppearanceLightNavigationBars)
        }
    }
}

private tailrec fun Context.activity(): Activity = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.activity()
    else -> error("No Activity")
}
