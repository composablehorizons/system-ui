package com.composables.systemui

import kotlin.test.Test
import kotlin.test.assertEquals

class SystemUiStateTest {
    @Test fun restoresUnderlyingRequestWhenOverrideLeaves() {
        val state = SystemUiState()
        val screen = Any()
        val overlay = Any()
        val light = SystemBarAppearance(SystemBarIcons.Light, SystemBarIcons.Light)
        val dark = SystemBarAppearance(SystemBarIcons.Dark)
        state.update(screen, light)
        state.update(overlay, dark)
        state.update(screen, SystemBarAppearance())
        assertEquals(dark, state.appearance)
        state.remove(overlay)
        assertEquals(SystemBarAppearance(), state.appearance)
        state.update(screen, light)
        assertEquals(light, state.appearance)
        state.remove(screen)
        assertEquals(SystemBarAppearance(), state.appearance)
    }
}
