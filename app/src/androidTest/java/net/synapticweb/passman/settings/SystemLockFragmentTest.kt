package net.synapticweb.passman.settings

import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import net.synapticweb.passman.*
import net.synapticweb.passman.di.TestAppComponent
import net.synapticweb.passman.util.*
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.not
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import java.io.DataInputStream
import java.io.File
import java.io.FileInputStream

class SystemLockFragmentTest {
    @get:Rule
    val testRule = CryptoPassTestRule()

    private lateinit var cipher : TestCryptoPassCipher

    @Before
    fun init() {
        cipher = (testRule.application.appComponent as TestAppComponent).cipher as TestCryptoPassCipher
    }

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
        cipher.hasHardwareStorage = true
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

        checkPassEncryptionAndPrefsSet()
    }

    private fun checkPassEncryptionAndPrefsSet() {
        val encFile = File(testRule.application.filesDir.absolutePath + "/" +
                TEST_ENCRYPTED_PASS_FILENAME)

        assertThat(encFile.exists(), `is`(true))


        val reader = DataInputStream(FileInputStream(encFile))
        val nBytesToRead: Int = reader.available()
        val encrypted = ByteArray(nBytesToRead)
        if (nBytesToRead > 0)
            reader.read(encrypted)

        assertThat(TEST_PASS, `is`(String(cipher.decrypt(encrypted))))
        encFile.delete()

        assertThat(testRule.getString(APPLOCK_KEY), `is`(APPLOCK_SYSTEM_VALUE))
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
        cipher.hasHardwareStorage = false

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

        checkPassEncryptionAndPrefsSet()
    }

    @Test
    fun goodPass_softwareStorage_secondScreen_cancel_goSettings() {
        testRule.setDb()
        cipher.hasHardwareStorage = false
        testRule.setString(APPLOCK_KEY, APPLOCK_PASSWD_VALUE)

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

        val encFile = File(testRule.application.filesDir.absolutePath + "/" +
                TEST_ENCRYPTED_PASS_FILENAME)

        assertThat(encFile.exists(), `is`(false))
        assertThat(testRule.getString(APPLOCK_KEY), `is`(APPLOCK_PASSWD_VALUE))
    }
}