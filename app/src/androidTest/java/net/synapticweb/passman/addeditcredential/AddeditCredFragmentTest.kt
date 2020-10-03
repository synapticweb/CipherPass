package net.synapticweb.passman.addeditcredential

import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import net.synapticweb.passman.R
import net.synapticweb.passman.model.Credential
import net.synapticweb.passman.util.CryptoPassTestRule
import net.synapticweb.passman.util.isToast
import net.synapticweb.passman.util.monitorFragment
import org.hamcrest.core.Is.`is`
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito

class AddeditCredFragmentTest {
    @get:Rule
    val testRule = CryptoPassTestRule()

    @Test
    fun addCred_nameEmpty_showToast() {
        val bundle = AddeditCredFragmentArgs(null, "New entry").toBundle()
        val fragmentScenario = launchFragmentInContainer<AddeditCredFragment>(bundle, R.style.AppTheme)
        testRule.dataBindingIdlingResource.monitorFragment(fragmentScenario)
        onView(withId(R.id.save)).perform(click())

        onView(withText(R.string.addedit_name_empty)).inRoot(isToast()).check(matches(isDisplayed()))
    }

    @Test
    fun addCred_passwdEmpty_showToast() {
        val bundle = AddeditCredFragmentArgs(null, "New entry").toBundle()
        val fragmentScenario = launchFragmentInContainer<AddeditCredFragment>(bundle, R.style.AppTheme)
        testRule.dataBindingIdlingResource.monitorFragment(fragmentScenario)
        onView(withId(R.id.name)).perform(typeText("test_name"))
        onView(withId(R.id.id)).perform(typeText("test_username"))
        onView(withId(R.id.save)).perform(click())

        onView(withText(R.string.addedit_pass_empty)).inRoot(isToast()).check(matches(isDisplayed()))
    }

    @Test
    fun addEdit_passwdNoMatch_showToast() {
        val bundle = AddeditCredFragmentArgs(null, "New entry").toBundle()
        val fragmentScenario = launchFragmentInContainer<AddeditCredFragment>(bundle, R.style.AppTheme)
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
        val bundle = AddeditCredFragmentArgs(null, "New entry").toBundle()
        val mockNav = Mockito.mock(NavController::class.java)
        val fragmentScenario = launchFragmentInContainer(bundle, R.style.AppTheme) {
            AddeditCredFragment().also { fragment ->
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

        val credential : Credential? = testRule.repository.getAllCredentials().value?.get(0)
        if (credential != null) {
            assertThat(credential.accountName, `is`("test_name"))
            assertThat(credential.accountId, `is`("test_username"))
            assertThat(credential.password, `is`("test_pass"))
            assertThat(credential.url, `is`("url"))
            assertThat(credential.comment, `is`("comment"))
        }
    }
}