package net.synapticweb.passman.settings

import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import net.synapticweb.passman.*
import net.synapticweb.passman.util.*
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.not
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify

class SystemLockFragmentTest {
    @get:Rule
    val testRule = CryptoPassTestRule()
    private val prefWrapper = PrefWrapper.getInstance(ApplicationProvider.getApplicationContext())

    @Test
    fun emptyPass_Error() {
        val bundle = SystemLockFragmentArgs(APPLOCK_SYSTEM_VALUE).toBundle()
        val fragmentScenario = launchFragmentInContainer<SystemLockFragment>(bundle, R.style.AppTheme)
        testRule.dataBindingIdlingResource.monitorFragment(fragmentScenario)
        onView(withId(R.id.action_button)).perform(click())

        onView(withId(R.id.pass_layout)).check(matches(
            hasTextInputLayoutErrorText(testRule.application.getString(R.string.pass_empty))))
    }

    @Test
    fun badPass_Error() {
        testRule.setDb()
        val bundle = SystemLockFragmentArgs(APPLOCK_SYSTEM_VALUE).toBundle()
        val fragmentScenario = launchFragmentInContainer<SystemLockFragment>(bundle, R.style.AppTheme)
        testRule.dataBindingIdlingResource.monitorFragment(fragmentScenario)

        onView(withId(R.id.passphrase)).perform(typeText("test1"), closeSoftKeyboard())
        onView(withId(R.id.action_button)).perform(click())

        onView(withId(R.id.pass_layout)).check(matches(
            hasTextInputLayoutErrorText(testRule.application.getString(R.string.pass_incorect))))
    }

    @Test
    fun goodPass_hardwareStorage_goSettings() {
        testRule.setDb()
        testRule.cipher.hasHardwareStorage = true
        //https://stackoverflow.com/a/59027482
        val mockNav = mock(NavController::class.java)
        val bundle = SystemLockFragmentArgs(APPLOCK_SYSTEM_VALUE).toBundle()
        val fragmentScenario = launchFragmentInContainer(bundle, R.style.AppTheme) {
            SystemLockFragment().also { fragment ->
                fragment.viewLifecycleOwnerLiveData.observeForever { viewLifeCycleOwner ->
                    if(viewLifeCycleOwner != null)
                        Navigation.setViewNavController(fragment.requireView(), mockNav)
                }
            }
        }
        testRule.dataBindingIdlingResource.monitorFragment(fragmentScenario)

        onView(withId(R.id.passphrase)).perform(typeText(TEST_PASS), closeSoftKeyboard())
        onView(withId(R.id.action_button)).perform(click())

        verify(mockNav).navigate(SystemLockFragmentDirections.
                actionSystemLockFragmentToSettingsFragment())

        val encryptedPass = prefWrapper.getString(ENCRYPTED_PASS_KEY)
        assertNotNull(encryptedPass)
        assertThat(String(testRule.cipher.decrypt(hexStrToByteArray(encryptedPass!!))),
            `is`(TEST_PASS))
    }

    private fun checkSecondScreen() {
        onView(withId(R.id.soft_storage_warning)).check(matches(isDisplayed()))
        onView(withId(R.id.passphrase)).check(matches(not(isDisplayed())))
        onView(withId(R.id.cancel_button)).check(matches(isDisplayed()))
        onView(withId(R.id.action_button)).check(matches(withText(android.R.string.yes)))
    }

    @Test
    fun goodPass_softwareStorage_secondScreen_ok_goSettings() {
        testRule.setDb()
        testRule.cipher.hasHardwareStorage = false

        val mockNav = mock(NavController::class.java)
        val bundle = SystemLockFragmentArgs(APPLOCK_SYSTEM_VALUE).toBundle()
        val fragmentScenario = launchFragmentInContainer(bundle, R.style.AppTheme) {
            SystemLockFragment().also { fragment ->
                fragment.viewLifecycleOwnerLiveData.observeForever { viewLifeCycleOwner ->
                    if(viewLifeCycleOwner != null)
                        Navigation.setViewNavController(fragment.requireView(), mockNav)
                }
            }
        }
        testRule.dataBindingIdlingResource.monitorFragment(fragmentScenario)

        onView(withId(R.id.passphrase)).perform(typeText(TEST_PASS), closeSoftKeyboard())
        onView(withId(R.id.action_button)).perform(click())

        checkSecondScreen()

        onView(withId(R.id.action_button)).perform(click())

        verify(mockNav).navigate(SystemLockFragmentDirections.
        actionSystemLockFragmentToSettingsFragment())

        val encryptedPass = prefWrapper.getString(ENCRYPTED_PASS_KEY)
        assertNotNull(encryptedPass)
        assertThat(String(testRule.cipher.decrypt(hexStrToByteArray(encryptedPass!!))),
            `is`(TEST_PASS))
    }

    @Test
    fun goodPass_softwareStorage_secondScreen_cancel_goSettings() {
        testRule.setDb()
        testRule.cipher.hasHardwareStorage = false
        prefWrapper.setPref(APPLOCK_KEY, APPLOCK_PASSWD_VALUE)
        prefWrapper.removePref(ENCRYPTED_PASS_KEY)

        val mockNav = mock(NavController::class.java)
        val bundle = SystemLockFragmentArgs(APPLOCK_SYSTEM_VALUE).toBundle()
        val fragmentScenario = launchFragmentInContainer(bundle, R.style.AppTheme) {
            SystemLockFragment().also { fragment ->
                fragment.viewLifecycleOwnerLiveData.observeForever { viewLifeCycleOwner ->
                    if(viewLifeCycleOwner != null)
                        Navigation.setViewNavController(fragment.requireView(), mockNav)
                }
            }
        }
        testRule.dataBindingIdlingResource.monitorFragment(fragmentScenario)

        onView(withId(R.id.passphrase)).perform(typeText(TEST_PASS), closeSoftKeyboard())
        onView(withId(R.id.action_button)).perform(click())

        checkSecondScreen()

        onView(withId(R.id.cancel_button)).perform(click())

        verify(mockNav).navigate(SystemLockFragmentDirections.
            actionSystemLockFragmentToSettingsFragment())

        assertNull(prefWrapper.getString(ENCRYPTED_PASS_KEY)) //nu s-a salvat parola criptată
        assertThat(prefWrapper.getString(APPLOCK_KEY), `is`(APPLOCK_PASSWD_VALUE))
    }

    @Test
    fun goodPass_errorWritingSettings() {
        testRule.setDb()
        testRule.cipher.encryptPassReturnError = true
        prefWrapper.setPref(APPLOCK_KEY, APPLOCK_PASSWD_VALUE)

        val bundle = SystemLockFragmentArgs(APPLOCK_SYSTEM_VALUE).toBundle()
        val fragmentScenario = launchFragmentInContainer<SystemLockFragment>(bundle, R.style.AppTheme)
        testRule.dataBindingIdlingResource.monitorFragment(fragmentScenario)

        onView(withId(R.id.passphrase)).perform(typeText(TEST_PASS), closeSoftKeyboard())
        onView(withId(R.id.action_button)).perform(click())

        //dacă afișează Snackbar
        onView(withText(testRule.application.getString(R.string.system_lock_file_write_fail)))
            .check(matches(withEffectiveVisibility(Visibility.VISIBLE)))

        assertThat(prefWrapper.getString(APPLOCK_KEY), `is`(APPLOCK_PASSWD_VALUE))
    }
}