package net.synapticweb.passman.addeditentry

import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import kotlinx.coroutines.runBlocking
import net.synapticweb.passman.R
import net.synapticweb.passman.model.Entry
import net.synapticweb.passman.model.SortOrder
import net.synapticweb.passman.util.CryptoPassTestRule
import net.synapticweb.passman.util.isToast
import net.synapticweb.passman.util.monitorFragment
import org.hamcrest.core.Is.`is`
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito

class AddeditEntryFragmentTest {
    @get:Rule
    val testRule = CryptoPassTestRule()

    @Test
    fun addEntry_nameEmpty_showToast() {
        val bundle = AddeditEntryFragmentArgs(0, "New entry").toBundle()
        val fragmentScenario = launchFragmentInContainer<AddeditEntryFragment>(bundle, R.style.AppTheme)
        testRule.dataBindingIdlingResource.monitorFragment(fragmentScenario)
        onView(withId(R.id.id)).perform(typeText("test_username"))
        onView(withId(R.id.save)).perform(click())

        onView(withText(R.string.addedit_name_empty)).inRoot(isToast()).check(matches(isDisplayed()))
    }

    @Test
    fun addEntry_passwdEmpty_showToast() {
        val bundle = AddeditEntryFragmentArgs(0, "New entry").toBundle()
        val fragmentScenario = launchFragmentInContainer<AddeditEntryFragment>(bundle, R.style.AppTheme)
        testRule.dataBindingIdlingResource.monitorFragment(fragmentScenario)
        onView(withId(R.id.name)).perform(typeText("test_name"))
        onView(withId(R.id.id)).perform(typeText("test_username"))
        onView(withId(R.id.save)).perform(click())

        onView(withText(R.string.addedit_pass_empty)).inRoot(isToast()).check(matches(isDisplayed()))
    }

    @Test
    fun addEdit_passwdNoMatch_showToast() {
        val bundle = AddeditEntryFragmentArgs(0, "New entry").toBundle()
        val fragmentScenario = launchFragmentInContainer<AddeditEntryFragment>(bundle, R.style.AppTheme)
        testRule.dataBindingIdlingResource.monitorFragment(fragmentScenario)
        onView(withId(R.id.name)).perform(typeText("test_name"))
        onView(withId(R.id.id)).perform(typeText("test_username"))
        onView(withId(R.id.pass)).perform(typeText("test_pass"))
        onView(withId(R.id.repass)).perform(typeText("test_pass2"))
        onView(withId(R.id.save)).perform(click())

        onView(withText(R.string.addedit_pass_nomatch)).inRoot(isToast()).check(matches(isDisplayed()))
    }

    @Test
    fun addEdit_goodInputSave_recordInDb() {
        testRule.setDb()
        val bundle = AddeditEntryFragmentArgs(0, "New entry").toBundle()
        val mockNav = Mockito.mock(NavController::class.java)
        val fragmentScenario = launchFragmentInContainer(bundle, R.style.AppTheme) {
            AddeditEntryFragment().also { fragment ->
                fragment.viewLifecycleOwnerLiveData.observeForever { viewLifeCycleOwner ->
                    if(viewLifeCycleOwner != null)
                        Navigation.setViewNavController(fragment.requireView(), mockNav)
                }
            }
        }

        testRule.dataBindingIdlingResource.monitorFragment(fragmentScenario)
        onView(withId(R.id.name)).perform(typeText("test_name"))
        onView(withId(R.id.id)).perform(typeText("test_username"))
        onView(withId(R.id.pass)).perform(typeText("test_pass"))
        onView(withId(R.id.repass)).perform(typeText("test_pass"))
        onView(withId(R.id.url)).perform(typeText("url"), closeSoftKeyboard())
        onView(withId(R.id.comment)).perform(typeText("comment"), closeSoftKeyboard())
        onView(withId(R.id.save)).perform(click())

        val entry : Entry? = testRule.repository.getAllEntries(SortOrder.CREATION_DATE).value?.get(0)
        if (entry != null) {
            assertThat(entry.entryName, `is`("test_name"))
            assertThat(entry.username, `is`("test_username"))
            assertThat(entry.password, `is`("test_pass"))
            assertThat(entry.url, `is`("url"))
            assertThat(entry.comment, `is`("comment"))
        }
    }

    @Test
    fun addEdit_edit_notDirty_showsToast(): Unit = runBlocking {
        testRule.setDb()

        val item = Entry()
        item.entryName = "account_name"
        item.username = "username"
        item.password = "password"
        item.url = "url"
        testRule.repository.insertEntry(item)

        val bundle = AddeditEntryFragmentArgs(1, "account_name").toBundle()
        val mockNav = Mockito.mock(NavController::class.java)
        val fragmentScenario = launchFragmentInContainer(bundle, R.style.AppTheme) {
            AddeditEntryFragment().also { fragment ->
                fragment.viewLifecycleOwnerLiveData.observeForever { viewLifeCycleOwner ->
                    if(viewLifeCycleOwner != null)
                        Navigation.setViewNavController(fragment.requireView(), mockNav)
                }
            }
        }

        testRule.dataBindingIdlingResource.monitorFragment(fragmentScenario)
        onView(withId(R.id.save)).perform(click())
        onView(withText(R.string.addedit_nochange)).inRoot(isToast()).check(matches(isDisplayed()))
        return@runBlocking
    }

    @Test
    fun addEdit_edit_retainChanges() = runBlocking {
        testRule.setDb()

        val item = Entry()
        item.entryName = "account_name"
        item.username = "username"
        item.password = "password"
        item.url = "url"
        testRule.repository.insertEntry(item)

        val bundle = AddeditEntryFragmentArgs(1, "account_name").toBundle()
        val mockNav = Mockito.mock(NavController::class.java)
        val fragmentScenario = launchFragmentInContainer(bundle, R.style.AppTheme) {
            AddeditEntryFragment().also { fragment ->
                fragment.viewLifecycleOwnerLiveData.observeForever { viewLifeCycleOwner ->
                    if(viewLifeCycleOwner != null)
                        Navigation.setViewNavController(fragment.requireView(), mockNav)
                }
            }
        }

        testRule.dataBindingIdlingResource.monitorFragment(fragmentScenario)
        onView(withId(R.id.id)).perform(replaceText("username_changed"), closeSoftKeyboard())
        onView(withId(R.id.pass)).perform(replaceText("password_changed"), closeSoftKeyboard())
        onView(withId(R.id.repass)).perform(replaceText("password_changed"), closeSoftKeyboard())
        onView(withId(R.id.comment)).perform(typeText("comment"), closeSoftKeyboard())
        onView(withId(R.id.save)).perform(click())

        val entry : Entry = testRule.repository.getEntry(1)

        assertThat(entry.entryName, `is`("account_name"))
        assertThat(entry.username, `is`("username_changed"))
        assertThat(entry.password, `is`("password_changed"))
        assertThat(entry.url, `is`("url"))
        assertThat(entry.comment, `is`("comment"))
    }

}