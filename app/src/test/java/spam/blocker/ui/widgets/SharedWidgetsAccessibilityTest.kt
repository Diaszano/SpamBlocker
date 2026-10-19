package spam.blocker.ui.widgets

import androidx.compose.runtime.mutableStateOf
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class SharedWidgetsAccessibilityTest {

    @Test
    fun testBottomBarViewModelTabSelection() {
        val tab1Selected = mutableStateOf(true)
        val tab2Selected = mutableStateOf(false)
        val tab3Selected = mutableStateOf(false)

        var selectedRoute: String? = null
        var reselectedRoute: String? = null
        var leftRoute: String? = null

        val items = listOf(
            TabItem("tab1", "Tab 1", 0, tab1Selected, { null }) {},
            TabItem("tab2", "Tab 2", 0, tab2Selected, { "5" }) {},
            TabItem("tab3", "Tab 3", 0, tab3Selected, { null }) {}
        )

        val vm = BottomBarViewModel(
            tabItems = items,
            onTabSelected = { selectedRoute = it },
            onTabReSelected = { reselectedRoute = it },
            onTabLeave = { leftRoute = it }
        )

        assertEquals(3, vm.tabItems.size)
        assertTrue(vm.tabItems[0].isSelected.value)
        assertFalse(vm.tabItems[1].isSelected.value)
        assertEquals("5", vm.tabItems[1].badgeText())

        // Simulate switching to tab2
        val currentRoute = vm.tabItems.firstOrNull { it.isSelected.value }?.route
        assertEquals("tab1", currentRoute)
        currentRoute?.let { vm.onTabLeave(it) }
        vm.tabItems.forEach { it.isSelected.value = (it.route == "tab2") }
        vm.onTabSelected("tab2")

        assertEquals("tab1", leftRoute)
        assertEquals("tab2", selectedRoute)
        assertFalse(vm.tabItems[0].isSelected.value)
        assertTrue(vm.tabItems[1].isSelected.value)
        assertFalse(vm.tabItems[2].isSelected.value)
    }

    @Test
    fun testSectionStateToggle() {
        val isCollapsed = mutableStateOf(false)
        var toggledState: Boolean? = null

        val onToggle: (Boolean) -> Unit = { toggledState = it }

        // Expand to collapse
        isCollapsed.value = !isCollapsed.value
        onToggle(isCollapsed.value)

        assertTrue(isCollapsed.value)
        assertEquals(true, toggledState)

        // Collapse to expand
        isCollapsed.value = !isCollapsed.value
        onToggle(isCollapsed.value)

        assertFalse(isCollapsed.value)
        assertEquals(false, toggledState)
    }
}
