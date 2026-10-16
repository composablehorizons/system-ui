@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package com.composables.systemui

import androidx.compose.runtime.*
import androidx.compose.ui.window.ComposeUIViewController
import platform.UIKit.*

fun SystemUiViewController(content: @Composable () -> Unit): UIViewController =
    SystemUiHostingController(content)

private class SystemUiHostingController(private val content: @Composable () -> Unit) : UIViewController(null, null) {
    private val state = SystemUiState()

    override fun viewDidLoad() {
        super.viewDidLoad()
        val child = ComposeUIViewController {
            ProvideSystemUi(state) {
                content()
                val appearance = state.appearance
                SideEffect {
                    statusBarIcons = appearance.statusBar
                    setNeedsStatusBarAppearanceUpdate()
                }
            }
        }
        addChildViewController(child)
        view.addSubview(child.view)
        child.view.setFrame(view.bounds)
        child.view.autoresizingMask = UIViewAutoresizingFlexibleWidth or UIViewAutoresizingFlexibleHeight
        child.didMoveToParentViewController(this)
    }

    private var statusBarIcons = SystemBarIcons.Default
    override fun preferredStatusBarStyle(): UIStatusBarStyle = when (statusBarIcons) {
        SystemBarIcons.Default -> UIStatusBarStyleDefault
        SystemBarIcons.Light -> UIStatusBarStyleLightContent
        SystemBarIcons.Dark -> UIStatusBarStyleDarkContent
    }
}

@Composable
internal actual fun NativeSystemBars(appearance: SystemBarAppearance) {
    error("Host Compose with SystemUiViewController to use SystemBars on iOS")
}
