package net.synapticweb.cipherpass.settings

import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import junit.framework.Assert.assertNull
import kotlinx.coroutines.delay
import net.synapticweb.cipherpass.*
import net.synapticweb.cipherpass.authenticate.APPLOCK_KEY
import net.synapticweb.cipherpass.authenticate.APPLOCK_NOLOCK_VALUE
import net.synapticweb.cipherpass.authenticate.APPLOCK_PASSWD_VALUE
import net.synapticweb.cipherpass.authenticate.APPLOCK_SYSTEM_VALUE
import net.synapticweb.cipherpass.util.*
import org.hamcrest.CoreMatchers.`is`
import org.junit.Rule
import org.junit.Test

class ChangeAuthenticationTest {
    @get:Rule
    val testRule = CipherPassTestRule()
    private val prefWrapper = PrefWrapper.getInstance(ApplicationProvider.getApplicationContext())

    @Test
    fun softBacked_showWarning() {
        prefWrapper.setPref(APPLOCK_KEY, APPLOCK_PASSWD_VALUE)
        prefWrapper.removePref(ENCRYPTED_PASS_KEY)
        prefWrapper.removePref(DO_NOT_SHOW_WARNING)

        val fragmentScenario = launchFragmentInContainer<SettingsFragment>(null, R.style.AppTheme)
        testRule.dataBindingIdlingResource.monitorFragment(fragmentScenario)
        onView(withText("Authentication type")).perform(click())
        onView(withText("No authentication")).perform(click())

        onView(withId(R.id.soft_storage_warning)).check(matches(isDisplayed()))
        onView(withId(R.id.md_text_title)).check(matches(withText(R.string.warning_title)))
    }

    @Test
    fun softBacked_clickOK_showEditText() {
        prefWrapper.setPref(APPLOCK_KEY, APPLOCK_PASSWD_VALUE)
        prefWrapper.removePref(ENCRYPTED_PASS_KEY)
        prefWrapper.removePref(DO_NOT_SHOW_WARNING)

        val fragmentScenario = launchFragmentInContainer<SettingsFragment>(null, R.style.AppTheme)
        testRule.dataBindingIdlingResource.monitorFragment(fragmentScenario)
        onView(withText("Authentication type")).perform(click())
        onView(withText("No authentication")).perform(click())

        onView(withText("OK")).perform(click())
        onView(withId(R.id.passphrase)).check(matches(isDisplayed()))
        onView(withId(R.id.soft_storage_warning)).check(matches(withEffectiveVisibility(Visibility.GONE)))
        onView(withId(R.id.stop_showing_warning_box)).check(matches(withEffectiveVisibility(Visibility.GONE)))
        onView(withId(R.id.md_text_title)).check(matches(withText(R.string.enter_password_title)))
    }

    @Test
    fun badPass_Error() {
        testRule.setDb()
        testRule.cipher.hasHardwareStorage = true
        prefWrapper.setPref(APPLOCK_KEY, APPLOCK_PASSWD_VALUE)
        prefWrapper.removePref(ENCRYPTED_PASS_KEY)

        val fragmentScenario = launchFragmentInContainer<SettingsFragment>(null, R.style.AppTheme)
        testRule.dataBindingIdlingResource.monitorFragment(fragmentScenario)

        onView(withText("Authentication type")).perform(click())
        onView(withText("No authentication")).perform(click())
        onView(withId(R.id.passphrase)).perform(typeText("test1"), closeSoftKeyboard())
        onView(withText("OK")).perform(click())

        onView(withId(R.id.md_text_title)).check(matches(withText(R.string.enter_password_title)))
        onView(withId(R.id.pass_layout)).check(matches(hasTextInputLayoutErrorText(
            testRule.application.getString(R.string.pass_incorect))))
    }

    @Test
    fun goodPass_modifySettings() {
        testRule.setDb()
        testRule.cipher.hasHardwareStorage = true
        prefWrapper.setPref(APPLOCK_KEY, APPLOCK_PASSWD_VALUE)
        prefWrapper.removePref(ENCRYPTED_PASS_KEY)

        val fragmentScenario = launchFragmentInContainer<SettingsFragment>(null, R.style.AppTheme)
        testRule.dataBindingIdlingResource.monitorFragment(fragmentScenario)

        onView(withText("Authentication type")).perform(click())
        onView(withText("No authentication")).perform(click())
        onView(withId(R.id.passphrase)).perform(typeText("test"), closeSoftKeyboard())
        onView(withText("OK")).perform(click())

        val passwd = testRule.cipher.decryptPassFromSettings()
        assertThat(passwd, `is`("test".toCharArray()))
        assertThat(prefWrapper.getString(APPLOCK_KEY), `is`(APPLOCK_NOLOCK_VALUE))
    }

    @Test
    fun systemAuth_toNoAuth_noDialog() {
        prefWrapper.setPref(APPLOCK_KEY, APPLOCK_SYSTEM_VALUE)
        testRule.cipher.encryptPassToSettings("test".toCharArray())

        val fragmentScenario = launchFragmentInContainer<SettingsFragment>(null, R.style.AppTheme)
        testRule.dataBindingIdlingResource.monitorFragment(fragmentScenario)
        onView(withText("Authentication type")).perform(click())
        onView(withText("No authentication")).perform(click())

        onView(withId(R.id.md_root)).check(doesNotExist())
        assertThat(prefWrapper.getString(APPLOCK_KEY), `is`(APPLOCK_NOLOCK_VALUE))
    }

    @Test
    fun systemAuth_toPassAuth_noDialog_passErased() {
        prefWrapper.setPref(APPLOCK_KEY, APPLOCK_SYSTEM_VALUE)
        testRule.cipher.encryptPassToSettings("test".toCharArray())

        val fragmentScenario = launchFragmentInContainer<SettingsFragment>(null, R.style.AppTheme)
        testRule.dataBindingIdlingResource.monitorFragment(fragmentScenario)
        onView(withText("Authentication type")).perform(click())
        onView(withText("Ask for password")).perform(click())

        onView(withId(R.id.md_root)).check(doesNotExist())
        assertNull(testRule.cipher.decryptPassFromSettings())
        assertThat(prefWrapper.getString(APPLOCK_KEY), `is`(APPLOCK_PASSWD_VALUE))
    }

    @Test
    fun goodPass_errorWriteSettings_showSnackbar() {
        testRule.setDb()
        testRule.cipher.hasHardwareStorage = true
        prefWrapper.setPref(APPLOCK_KEY, APPLOCK_PASSWD_VALUE)
        prefWrapper.removePref(ENCRYPTED_PASS_KEY)
        testRule.cipher.encryptPassReturnError = true

        val fragmentScenario = launchFragmentInContainer<SettingsFragment>(null, R.style.AppTheme)
        testRule.dataBindingIdlingResource.monitorFragment(fragmentScenario)

        onView(withText("Authentication type")).perform(click())
        onView(withText("No authentication")).perform(click())
        onView(withId(R.id.passphrase)).perform(typeText("test"), closeSoftKeyboard())
        onView(withText("OK")).perform(click())

        //dacă afișează Snackbar
        onView(withText(testRule.application.getString(R.string.system_lock_file_write_fail)))
            .check(matches(withEffectiveVisibility(Visibility.VISIBLE)))

        assertNull(testRule.cipher.decryptPassFromSettings())
        assertThat(prefWrapper.getString(APPLOCK_KEY), `is`(APPLOCK_PASSWD_VALUE))
    }

    @Test
    fun softBack_checkNoMoreWarnings_warningNotDisplayedAgain() {
        prefWrapper.setPref(APPLOCK_KEY, APPLOCK_PASSWD_VALUE)
        prefWrapper.removePref(ENCRYPTED_PASS_KEY)
        prefWrapper.removePref(DO_NOT_SHOW_WARNING)

        val fragmentScenario = launchFragmentInContainer<SettingsFragment>(null, R.style.AppTheme)
        testRule.dataBindingIdlingResource.monitorFragment(fragmentScenario)
        onView(withText("Authentication type")).perform(click())
        onView(withText("No authentication")).perform(click())

        onView(withId(R.id.stop_showing_warning)).perform(click())
        onView(withText("OK")).perform(click())
        onView(withText("CANCEL")).perform(click())

        onView(withText("Authentication type")).perform(click())
        onView(withText("No authentication")).perform(click())

        onView(withId(R.id.soft_storage_warning)).check(matches(withEffectiveVisibility(Visibility.GONE)))
    }
}