package net.synapticweb.cipherpass.entrydetail

import androidx.appcompat.view.menu.ActionMenuItem
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import junit.framework.Assert.assertNull
import kotlinx.coroutines.runBlocking
import net.synapticweb.cipherpass.R
import net.synapticweb.cipherpass.TestCipherPassApp
import net.synapticweb.cipherpass.model.CustomField
import net.synapticweb.cipherpass.model.Entry
import net.synapticweb.cipherpass.util.CipherPassTestRule
import net.synapticweb.cipherpass.util.hasItemAtPosition
import net.synapticweb.cipherpass.util.monitorFragment
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito

class EntryDetailFragmentTest {
    @get:Rule
    val testRule = CipherPassTestRule()

    @Test
    fun deleteEntry_wipedFromDb() = runBlocking {
        testRule.setDb()

        val item = Entry()
        item.entryName = "account_name"
        item.password = "password"
        testRule.repository.insertEntry(item)

        val bundle = EntryDetailFragmentArgs(1).toBundle()
        val mockNav = Mockito.mock(NavController::class.java)
        val fragmentScenario = launchFragmentInContainer(bundle, R.style.AppTheme) {
            EntryDetailFragment().also { fragment ->
                fragment.viewLifecycleOwnerLiveData.observeForever { viewLifeCycleOwner ->
                    if(viewLifeCycleOwner != null)
                        Navigation.setViewNavController(fragment.requireView(), mockNav)
                }
            }
        }
        testRule.dataBindingIdlingResource.monitorFragment(fragmentScenario)
        //https://stackoverflow.com/a/59129586
        val context = ApplicationProvider.getApplicationContext<TestCipherPassApp>()
        val deleteMenuItem = ActionMenuItem(context, 0, R.id.delete, 0, 0, null)
        fragmentScenario.onFragment { fragment ->  
            fragment.onOptionsItemSelected(deleteMenuItem)
        }
        onView(withText(context.getString(android.R.string.ok))).perform(click())
        assertNull(testRule.repository.getEntry(1))
    }

    @Test
    fun addCustomField_showInDetail() : Unit =  runBlocking {
        testRule.setDb()
        val entry = Entry()
        entry.entryName = "account_name"
        entry.username = "username"
        testRule.repository.insertEntry(entry)

        val customField = CustomField(1, "field_name", false, "field_value")
        testRule.repository.insertCustomField(customField)

        val bundle = EntryDetailFragmentArgs(1).toBundle()
        val fragmentScenario = launchFragmentInContainer<EntryDetailFragment>(bundle, R.style.AppTheme)

        testRule.dataBindingIdlingResource.monitorFragment(fragmentScenario)
        onView(withText("username")).check(matches(isDisplayed()))

        onView(withId(R.id.custom_fields)).check(
            matches(hasItemAtPosition(0, hasDescendant(withText("field_name")))))

        onView(withId(R.id.custom_fields)).check(
            matches(hasItemAtPosition(0, hasDescendant(withText("field_value")))))
        return@runBlocking
    }
}