package com.akira.island.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * Unit tests for the decide-only logic behind "pull down -> quick menu".
 * No Android framework dependency.
 */
class SwipeDownMenuTest {

    private val threshold = 100f
    private val cancelThreshold = 4f

    private fun action(
        expanded: Boolean = false,
        vertical: Boolean = true,
        dy: Float = 150f,
        limit: Float = threshold,
        transient: Boolean = false,
        idle: Boolean = false,
        swipeExpand: Boolean = true,
        idleExpand: Boolean = true,
        menuEnabled: Boolean = true,
        cancelled: Boolean = false,
    ) = swipeDownAction(
        expanded = expanded,
        vertical = vertical,
        dy = dy,
        threshold = limit,
        transient = transient,
        idle = idle,
        swipeExpand = swipeExpand,
        idleExpand = idleExpand,
        menuEnabled = menuEnabled,
        cancelled = cancelled,
    )

    @Test
    fun `menu off keeps the classic expand on an activity`() {
        assertEquals(SwipeDownAction.EXPAND, action(menuEnabled = false))
    }

    @Test
    fun `menu off does nothing when swipeExpand is off`() {
        assertEquals(SwipeDownAction.NONE, action(menuEnabled = false, swipeExpand = false))
    }

    @Test
    fun `menu off keeps expand for the idle capsule only with idleExpand`() {
        assertEquals(SwipeDownAction.EXPAND, action(menuEnabled = false, idle = true))
        assertEquals(SwipeDownAction.NONE, action(menuEnabled = false, idle = true, idleExpand = false))
    }

    @Test
    fun `menu on opens the quick menu on an activity even if swipeExpand is off`() {
        assertEquals(SwipeDownAction.QUICK_MENU, action(swipeExpand = false))
    }

    @Test
    fun `menu on still expands the idle capsule`() {
        assertEquals(SwipeDownAction.EXPAND, action(idle = true))
    }

    @Test
    fun `menu on does nothing on an empty capsule without idleExpand`() {
        assertEquals(SwipeDownAction.NONE, action(idle = true, idleExpand = false))
    }

    @Test
    fun `expanded island ignores a downward swipe`() {
        assertEquals(SwipeDownAction.NONE, action(expanded = true))
    }

    @Test
    fun `transient activity ignores the swipe`() {
        assertEquals(SwipeDownAction.NONE, action(transient = true))
    }

    @Test
    fun `upward and horizontal gestures are not a pull down`() {
        assertEquals(SwipeDownAction.NONE, action(vertical = false))
        assertEquals(SwipeDownAction.NONE, action(dy = -150f))
    }

    @Test
    fun `movement below the threshold is ignored`() {
        assertEquals(SwipeDownAction.NONE, action(dy = threshold))
    }

    @Test
    fun `cancelled swipe opens the menu before the system steals the gesture`() {
        assertEquals(SwipeDownAction.QUICK_MENU, action(dy = 20f, limit = cancelThreshold, cancelled = true))
    }

    @Test
    fun `cancelled swipe below the short threshold is ignored`() {
        assertEquals(SwipeDownAction.NONE, action(dy = 2f, limit = cancelThreshold, cancelled = true))
    }

    @Test
    fun `cancelled swipe changes nothing when the menu is off`() {
        assertEquals(SwipeDownAction.NONE, action(dy = 20f, limit = cancelThreshold, menuEnabled = false, cancelled = true))
    }

    @Test
    fun `cancelled upward swipe is ignored`() {
        assertEquals(SwipeDownAction.NONE, action(dy = -20f, limit = cancelThreshold, cancelled = true))
    }

    @Test
    fun `opening the menu remembers the capsule to return to`() {
        val menu = QuickMenuLogic.open("music")
        assertEquals("music", menu.activityId)
        assertEquals("music", menu.capsuleOnClose)
    }

    @Test
    fun `closing the menu returns to the previous capsule`() {
        val open = QuickMenuLogic.open("music")
        assertNull(QuickMenuLogic.close())
        // The activity itself is untouched: the capsule id is the one we remembered on open.
        assertEquals("music", open.capsuleOnClose)
    }

    @Test
    fun `opening over the idle capsule remembers no activity`() {
        assertNull(QuickMenuLogic.open(null).capsuleOnClose)
    }
}
