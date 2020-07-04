package net.synapticweb.passman.settings

import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import net.synapticweb.passman.*
import net.synapticweb.passman.di.TestAppComponent
import net.synapticweb.passman.model.Hash
import net.synapticweb.passman.util.*
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.io.DataInputStream
import java.io.FileInputStream


class SettingsFragmentTest {
    @get:Rule
    val testRule = CryptoPassTestRule()

    private lateinit var cipher : TestCryptoPassCipher

    @Before
    fun init() {
        cipher = (testRule.application.appComponent as TestAppComponent).cipher as TestCryptoPassCipher
    }

    @Test
    fun emptyActPass_error() {
        val fragmentScenario = launchFragmentInContainer<SettingsFragment>(null, R.style.AppTheme)
        testRule.dataBindingIdlingResource.monitorFragment(fragmentScenario)

        onView(withText("Change password")).perform(click())
        onView(withText("OK")).perform(click())

        onView(withId(R.id.pass_layout)).check(
            matches(hasTextInputLayoutErrorText(testRule.application.getString(R.string.act_pass_empty)))
        )
    }

    @Test
    fun emptyNewPass_Error() {
        val fragmentScenario = launchFragmentInContainer<SettingsFragment>(null, R.style.AppTheme)
        testRule.dataBindingIdlingResource.monitorFragment(fragmentScenario)

        onView(withText("Change password")).perform(click())
        onView(withId(R.id.actual_passphrase)).perform(typeText(TEST_PASS), closeSoftKeyboard())
        onView(withText("OK")).perform(click())

        onView(withId(R.id.new_pass_layout)).check(
            matches(hasTextInputLayoutErrorText(testRule.application.getString(R.string.pass_empty)))
        )
    }

    @Test
    fun newPass_noMatch_error() {
        testRule.setDb()
        val fragmentScenario = launchFragmentInContainer<SettingsFragment>(null, R.style.AppTheme)
        testRule.dataBindingIdlingResource.monitorFragment(fragmentScenario)

        onView(withText("Change password")).perform(click())
        onView(withId(R.id.actual_passphrase)).perform(typeText(TEST_PASS), closeSoftKeyboard())

        onView(withId(R.id.new_passphrase)).perform(typeText("test1"), closeSoftKeyboard())
        onView(withId(R.id.new_passphrase_retype)).perform(typeText("test2"), closeSoftKeyboard())

        onView(withText("OK")).perform(click())

        onView(withId(R.id.new_pass_layout)).check(
            matches(hasTextInputLayoutErrorText(testRule.application.getString(R.string.pass_no_match)))
        )

    }

    @Test
    fun actPass_noMatch_error() {
        testRule.setDb()
        val fragmentScenario = launchFragmentInContainer<SettingsFragment>(null, R.style.AppTheme)
        testRule.dataBindingIdlingResource.monitorFragment(fragmentScenario)

        onView(withText("Change password")).perform(click())
        onView(withId(R.id.actual_passphrase)).perform(typeText("incorect"), closeSoftKeyboard())

        onView(withId(R.id.new_passphrase)).perform(typeText("test1"), closeSoftKeyboard())
        onView(withId(R.id.new_passphrase_retype)).perform(typeText("test1"), closeSoftKeyboard())

        onView(withText("OK")).perform(click())

        onView(withId(R.id.pass_layout)).check(
            matches(hasTextInputLayoutErrorText(testRule.application.getString(R.string.pass_incorect)))
        )
    }

    @Test
    fun correctInput_strongPass_passChange() {
        testRule.setDb()
        val fragmentScenario = launchFragmentInContainer<SettingsFragment>(null, R.style.AppTheme)
        testRule.dataBindingIdlingResource.monitorFragment(fragmentScenario)

        onView(withText("Change password")).perform(click())
        onView(withId(R.id.actual_passphrase)).perform(typeText(TEST_PASS), closeSoftKeyboard())

        onView(withId(R.id.new_passphrase)).perform(typeText("test1"), closeSoftKeyboard())
        onView(withId(R.id.new_passphrase_retype)).perform(typeText("test1"), closeSoftKeyboard())

        onView(withText("OK")).perform(click())

        runBlocking {
            testRule.repository.lock()
            testRule.repository.unlock("test1".toByteArray())
        }

        assertThat(testRule.repository.isUnlocked(), `is`(true))
        val passValid = runBlocking {
            testRule.repository.isPassValid("test1".toCharArray(), false)
        }

        assertThat(passValid, `is`(true))
        assertThat(testRule.encFile.exists(), `is`(false))
    }

    @Test
    fun correctInput_weakPass_passChange() {
        testRule.setDb()
        testRule.setString(APPLOCK_KEY, APPLOCK_SYSTEM_VALUE)

        val fragmentScenario = launchFragmentInContainer<SettingsFragment>(null, R.style.AppTheme)
        testRule.dataBindingIdlingResource.monitorFragment(fragmentScenario)

        onView(withText("Change password")).perform(click())
        onView(withId(R.id.actual_passphrase)).perform(typeText(TEST_PASS), closeSoftKeyboard())

        onView(withId(R.id.new_passphrase)).perform(typeText("test1"), closeSoftKeyboard())
        onView(withId(R.id.new_passphrase_retype)).perform(typeText("test1"), closeSoftKeyboard())

        onView(withText("OK")).perform(click())

        assertThat(testRule.encFile.exists(), `is`(true))

        val encrypted : ByteArray = runBlocking(Dispatchers.IO) {
            val reader = DataInputStream(FileInputStream(testRule.encFile))
            val nBytesToRead: Int = reader.available()
            val bytes = ByteArray(nBytesToRead)
            if (nBytesToRead > 0)
                reader.read(bytes)

            bytes
        }

        val passDecrypt = cipher.decrypt(encrypted)
        assertThat(passDecrypt.contentEquals("test1".toByteArray()), `is`(true))
    }
}