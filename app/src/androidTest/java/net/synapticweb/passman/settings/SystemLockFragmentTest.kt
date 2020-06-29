package net.synapticweb.passman.settings

import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import kotlinx.coroutines.runBlocking
import net.synapticweb.passman.R
import net.synapticweb.passman.TEST_ENCRYPTED_PASS_FILENAME
import net.synapticweb.passman.di.TestAppComponent
import net.synapticweb.passman.model.Hash
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

const val PASS = "test"

class SystemLockFragmentTest {
    @get:Rule
    val testRule = CryptoPassTestRule()

    lateinit var cipher : TestCryptoPassCipher

    private fun setDb() = runBlocking {
        testRule.repository.unlock(PASS.toByteArray())
        val salt = createSalt()
        val hashStr = byteArrayToHexStr(createHash(PASS, salt))
        val hash = Hash(hashStr, byteArrayToHexStr(salt))
        testRule.repository.insertHash(hash)
    }

    @Before
    fun init() {
        cipher = (testRule.application.appComponent as TestAppComponent).cipher as TestCryptoPassCipher
    }

    @Test
    fun emptyPass_Error() {
        val fragmentScenario = launchFragmentInContainer<SystemLockFragment>(null, R.style.AppTheme)
        testRule.dataBindingIdlingResource.monitorFragment(fragmentScenario)
        onView(withId(R.id.action_button)).perform(click())

        onView(withId(R.id.pass_layout)).check(matches(
            hasTextInputLayoutErrorText(testRule.application.getString(R.string.pass_empty))))
    }

    @Test
    fun badPass_Error() {
        setDb()
        val fragmentScenario = launchFragmentInContainer<SystemLockFragment>(null, R.style.AppTheme)
        testRule.dataBindingIdlingResource.monitorFragment(fragmentScenario)

        onView(withId(R.id.passphrase)).perform(typeText("test1"), closeSoftKeyboard())
        onView(withId(R.id.action_button)).perform(click())

        onView(withId(R.id.pass_layout)).check(matches(
            hasTextInputLayoutErrorText(testRule.application.getString(R.string.pass_incorect))))
    }

    @Test
    fun goodPass_hardwareStorage_goSettings() {
        setDb()
        cipher.hasHardwareStorage = true
        //https://stackoverflow.com/a/59027482
        val mockNav = mock(NavController::class.java)
        val fragmentScenario = launchFragmentInContainer(null, R.style.AppTheme) {
            SystemLockFragment().also { fragment ->
                fragment.viewLifecycleOwnerLiveData.observeForever { viewLifeCycleOwner ->
                    if(viewLifeCycleOwner != null)
                        Navigation.setViewNavController(fragment.requireView(), mockNav)
                }
            }
        }
        testRule.dataBindingIdlingResource.monitorFragment(fragmentScenario)

        onView(withId(R.id.passphrase)).perform(typeText(PASS), closeSoftKeyboard())
        onView(withId(R.id.action_button)).perform(click())

        verify(mockNav).navigate(SystemLockFragmentDirections.
                actionSystemLockFragmentToSettingsFragment())

        checkPassEncryptionAndPrefsSet()
    }

    private fun checkPassEncryptionAndPrefsSet() {
        val encFile = File(testRule.application.filesDir.absolutePath + "/" +
                TEST_ENCRYPTED_PASS_FILENAME)

        assertThat(encFile.exists(), `is`(true))

        lateinit var encrypted : String
        val reader = DataInputStream(FileInputStream(encFile))
        val nBytesToRead: Int = reader.available()
        if (nBytesToRead > 0) {
            val bytes = ByteArray(nBytesToRead)
            reader.read(bytes)
            encrypted = String(bytes)
        }

        assertThat(PASS, `is`(cipher.decrypt(encrypted)))
        encFile.delete()

        assertThat(testRule.getString("applock"), `is`("system"))
    }

    private fun checkSecondScreen() {
        onView(withId(R.id.soft_storage_warning)).check(matches(isDisplayed()))
        onView(withId(R.id.passphrase)).check(matches(not(isDisplayed())))
        onView(withId(R.id.cancel_button)).check(matches(isDisplayed()))
        onView(withId(R.id.action_button)).check(matches(withText(android.R.string.yes)))
    }

    @Test
    fun goodPass_softwareStorage_secondScreen_ok_goSettings() {
        setDb()
        cipher.hasHardwareStorage = false

        val mockNav = mock(NavController::class.java)
        val fragmentScenario = launchFragmentInContainer(null, R.style.AppTheme) {
            SystemLockFragment().also { fragment ->
                fragment.viewLifecycleOwnerLiveData.observeForever { viewLifeCycleOwner ->
                    if(viewLifeCycleOwner != null)
                        Navigation.setViewNavController(fragment.requireView(), mockNav)
                }
            }
        }
        testRule.dataBindingIdlingResource.monitorFragment(fragmentScenario)

        onView(withId(R.id.passphrase)).perform(typeText(PASS), closeSoftKeyboard())
        onView(withId(R.id.action_button)).perform(click())

        checkSecondScreen()

        onView(withId(R.id.action_button)).perform(click())

        verify(mockNav).navigate(SystemLockFragmentDirections.
        actionSystemLockFragmentToSettingsFragment())

        checkPassEncryptionAndPrefsSet()
    }

    @Test
    fun goodPass_softwareStorage_secondScreen_cancel_goSettings() {
        setDb()
        cipher.hasHardwareStorage = false

        val mockNav = mock(NavController::class.java)
        val fragmentScenario = launchFragmentInContainer(null, R.style.AppTheme) {
            SystemLockFragment().also { fragment ->
                fragment.viewLifecycleOwnerLiveData.observeForever { viewLifeCycleOwner ->
                    if(viewLifeCycleOwner != null)
                        Navigation.setViewNavController(fragment.requireView(), mockNav)
                }
            }
        }
        testRule.dataBindingIdlingResource.monitorFragment(fragmentScenario)

        onView(withId(R.id.passphrase)).perform(typeText(PASS), closeSoftKeyboard())
        onView(withId(R.id.action_button)).perform(click())

        checkSecondScreen()

        onView(withId(R.id.cancel_button)).perform(click())

        verify(mockNav).navigate(SystemLockFragmentDirections.
            actionSystemLockFragmentToSettingsFragment())

        val encFile = File(testRule.application.filesDir.absolutePath + "/" +
                TEST_ENCRYPTED_PASS_FILENAME)

        assertThat(encFile.exists(), `is`(false))
        assertNull(testRule.getString("applock"))
    }
}