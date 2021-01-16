package net.synapticweb.cipherpass.entrieslist

import androidx.appcompat.view.menu.ActionMenuItem
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import net.synapticweb.cipherpass.R
import net.synapticweb.cipherpass.SORT_ORDER_KEY
import net.synapticweb.cipherpass.TestCipherPassApp
import net.synapticweb.cipherpass.model.Entry
import net.synapticweb.cipherpass.util.CipherPassTestRule
import net.synapticweb.cipherpass.util.hasItemAtPosition
import net.synapticweb.cipherpass.util.monitorFragment
import org.junit.Rule
import org.junit.Test

class EntriesListFragmentTest {
    @get:Rule
    val testRule = CipherPassTestRule()

    @Test
    fun sort_by_default() : Unit = runBlocking {
        testRule.setDb()
        testRule.prefWrapper.removePref(SORT_ORDER_KEY)

        val item = Entry()
        item.entryName = "a_first_entry"
        item.password = "password"
        testRule.repository.insertEntry(item)

        item.insertionDate = System.currentTimeMillis() + 1000
        item.entryName = "b_second_entry"
        testRule.repository.insertEntry(item)

        item.insertionDate = System.currentTimeMillis() + 10000
        item.entryName = "c_third_entry"
        testRule.repository.insertEntry(item)

        val fragmentScenario = launchFragmentInContainer<EntriesListFragment>(null, R.style.AppTheme)
        testRule.dataBindingIdlingResource.monitorFragment(fragmentScenario)

        onView(withId(R.id.entries_list)).check(matches(hasItemAtPosition(0, hasDescendant(withText("c_third_entry")))))
        onView(withId(R.id.entries_list)).check(matches(hasItemAtPosition(1, hasDescendant(withText("b_second_entry")))))
        onView(withId(R.id.entries_list)).check(matches(hasItemAtPosition(2, hasDescendant(withText("a_first_entry")))))

        return@runBlocking
    }

    @Test
    fun sort_by_name() : Unit = runBlocking {
        testRule.setDb()
        testRule.prefWrapper.removePref(SORT_ORDER_KEY)

        val item = Entry()
        item.entryName = "a_first_entry"
        item.password = "password"
        testRule.repository.insertEntry(item)

        item.insertionDate = System.currentTimeMillis() + 1000
        item.entryName = "b_second_entry"
        testRule.repository.insertEntry(item)

        item.insertionDate = System.currentTimeMillis() + 1000
        item.entryName = "c_third_entry"
        testRule.repository.insertEntry(item)

        val fragmentScenario = launchFragmentInContainer<EntriesListFragment>(null, R.style.AppTheme)
        testRule.dataBindingIdlingResource.monitorFragment(fragmentScenario)
        val context = ApplicationProvider.getApplicationContext<TestCipherPassApp>()
        val sortMenuItem = ActionMenuItem(context, 0, R.id.sort, 0, 0, null)
        fragmentScenario.onFragment { fragment ->
            fragment.onOptionsItemSelected(sortMenuItem)
        }
        onView(withText(context.getString(R.string.sort_name_asc))).perform(click())

        onView(withId(R.id.entries_list)).check(matches(hasItemAtPosition(0, hasDescendant(withText("a_first_entry")))))
        onView(withId(R.id.entries_list)).check(matches(hasItemAtPosition(1, hasDescendant(withText("b_second_entry")))))
        onView(withId(R.id.entries_list)).check(matches(hasItemAtPosition(2, hasDescendant(withText("c_third_entry")))))

        return@runBlocking
    }

    @Test
    fun sort_by_modifDate() : Unit = runBlocking {
        testRule.setDb()
        testRule.prefWrapper.removePref(SORT_ORDER_KEY)

        val item = Entry()
        item.entryName = "a_first_entry"
        item.password = "password"
        testRule.repository.insertEntry(item)

        item.insertionDate = System.currentTimeMillis() + 1000
        item.entryName = "b_second_entry"
        item.modificationDate = System.currentTimeMillis() + 100000
        testRule.repository.insertEntry(item)

        item.insertionDate = System.currentTimeMillis() + 10000
        item.modificationDate = System.currentTimeMillis() + 10000
        item.entryName = "c_third_entry"
        testRule.repository.insertEntry(item)

        val fragmentScenario = launchFragmentInContainer<EntriesListFragment>(null, R.style.AppTheme)
        testRule.dataBindingIdlingResource.monitorFragment(fragmentScenario)
        val context = ApplicationProvider.getApplicationContext<TestCipherPassApp>()
        val sortMenuItem = ActionMenuItem(context, 0, R.id.sort, 0, 0, null)
        fragmentScenario.onFragment { fragment ->
            fragment.onOptionsItemSelected(sortMenuItem)
        }
        onView(withText(context.getString(R.string.sort_modif_asc))).perform(click())

        onView(withId(R.id.entries_list)).check(matches(hasItemAtPosition(0, hasDescendant(withText("a_first_entry")))))
        onView(withId(R.id.entries_list)).check(matches(hasItemAtPosition(1, hasDescendant(withText("c_third_entry")))))
        onView(withId(R.id.entries_list)).check(matches(hasItemAtPosition(2, hasDescendant(withText("b_second_entry")))))



        return@runBlocking
    }
}