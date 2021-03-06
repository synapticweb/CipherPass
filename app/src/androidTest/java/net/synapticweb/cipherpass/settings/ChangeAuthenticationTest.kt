/*
 * Copyright (C) 2021 Eugen Rădulescu <synapticwebb@gmail.com>
 * This file is part of the CipherPass project ( https://github.com/synapticweb/CipherPass ).
 * See the LICENSE file in the project root for license terms.
 */

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
import kotlinx.coroutines.runBlocking
import net.synapticweb.cipherpass.*
import net.synapticweb.cipherpass.authenticate.KEY_STORAGE_HARDWARE
import net.synapticweb.cipherpass.authenticate.KEY_STORAGE_SOFTWARE
import net.synapticweb.cipherpass.authenticate.KEY_STORAGE_TYPE_KEY
import net.synapticweb.cipherpass.util.*
import org.hamcrest.CoreMatchers.`is`
import org.junit.Rule
import org.junit.Test

class ChangeAuthenticationTest {
    @get:Rule
    val testRule = CipherPassTestRule()
    private val prefWrapper = PrefWrapper.getInstance(ApplicationProvider.getApplicationContext())
    private val appLockKey = testRule.application.resources.getString(R.string.applock_key)
    private val applockPasswd = testRule.application.resources.getString(R.string.applock_passwd_value)
    private val appLockNoLock = testRule.application.resources.getString(R.string.applock_nolock_value)
    private val appLockSystem = testRule.application.resources.getString(R.string.applock_system_value)

    @Test
    fun softBacked_showWarning() {
        prefWrapper.setPref(appLockKey, applockPasswd)
        prefWrapper.removePref(ENCRYPTED_PASS_KEY)
        prefWrapper.removePref(DO_NOT_SHOW_WARNING_KEY)
        prefWrapper.setPref(KEY_STORAGE_TYPE_KEY, KEY_STORAGE_SOFTWARE)

        val fragmentScenario = launchFragmentInContainer<SettingsFragment>(null, R.style.AppTheme)
        testRule.dataBindingIdlingResource.monitorFragment(fragmentScenario)
        onView(withText("Authentication type")).perform(click())
        onView(withText("No authentication")).perform(click())

        onView(withId(R.id.soft_storage_warning)).check(matches(isDisplayed()))
        onView(withId(R.id.md_text_title)).check(matches(withText(R.string.warning_title)))
    }

    @Test
    fun softBacked_clickOK_showEditText() {
        prefWrapper.setPref(appLockKey, applockPasswd)
        prefWrapper.removePref(ENCRYPTED_PASS_KEY)
        prefWrapper.removePref(DO_NOT_SHOW_WARNING_KEY)
        prefWrapper.setPref(KEY_STORAGE_TYPE_KEY, KEY_STORAGE_SOFTWARE)

        val fragmentScenario = launchFragmentInContainer<SettingsFragment>(null, R.style.AppTheme)
        testRule.dataBindingIdlingResource.monitorFragment(fragmentScenario)
        onView(withText("Authentication type")).perform(click())
        onView(withText("No authentication")).perform(click())

        onView(withText("OK")).perform(click())
        onView(withId(R.id.passphrase)).check(matches(isDisplayed()))
        onView(withId(R.id.md_text_title)).check(matches(withText(R.string.enter_pass)))
    }

    @Test
    fun badPass_Error() {
        testRule.setDb()
        prefWrapper.setPref(KEY_STORAGE_TYPE_KEY, KEY_STORAGE_HARDWARE)
        prefWrapper.setPref(appLockKey, applockPasswd)
        prefWrapper.removePref(ENCRYPTED_PASS_KEY)

        val fragmentScenario = launchFragmentInContainer<SettingsFragment>(null, R.style.AppTheme)
        testRule.dataBindingIdlingResource.monitorFragment(fragmentScenario)

        onView(withText("Authentication type")).perform(click())
        onView(withText("No authentication")).perform(click())
        onView(withId(R.id.passphrase)).perform(typeText("test1"), closeSoftKeyboard())
        onView(withText("OK")).perform(click())

        onView(withId(R.id.pass_layout)).check(matches(hasTextInputLayoutErrorText(
            testRule.application.getString(R.string.pass_incorect))))
    }

    @Test
    fun goodPass_modifySettings() {
        testRule.setDb()
        prefWrapper.setPref(KEY_STORAGE_TYPE_KEY, KEY_STORAGE_HARDWARE)
        prefWrapper.setPref(appLockKey, applockPasswd)
        prefWrapper.removePref(ENCRYPTED_PASS_KEY)

        val fragmentScenario = launchFragmentInContainer<SettingsFragment>(null, R.style.AppTheme)
        testRule.dataBindingIdlingResource.monitorFragment(fragmentScenario)

        onView(withText("Authentication type")).perform(click())
        onView(withText("No authentication")).perform(click())
        onView(withId(R.id.passphrase)).perform(typeText("test"), closeSoftKeyboard())
        onView(withText("OK")).perform(click())

        val passwd = testRule.cipher.decryptPassFromSettings()
        assertThat(passwd, `is`("test".toCharArray()))
        assertThat(prefWrapper.getString(appLockKey), `is`(appLockNoLock))
    }

    @Test
    fun systemAuth_toNoAuth_noDialog() {
        prefWrapper.setPref(appLockKey, appLockSystem)
        testRule.cipher.encryptPassToSettings("test".toCharArray())

        val fragmentScenario = launchFragmentInContainer<SettingsFragment>(null, R.style.AppTheme)
        testRule.dataBindingIdlingResource.monitorFragment(fragmentScenario)
        onView(withText("Authentication type")).perform(click())
        onView(withText("No authentication")).perform(click())

        onView(withId(R.id.md_root)).check(doesNotExist())
        assertThat(prefWrapper.getString(appLockKey), `is`(appLockNoLock))
    }

    @Test
    fun systemAuth_toPassAuth_noDialog_passErased() {
        prefWrapper.setPref(appLockKey, appLockSystem)
        testRule.cipher.encryptPassToSettings("test".toCharArray())

        val fragmentScenario = launchFragmentInContainer<SettingsFragment>(null, R.style.AppTheme)
        testRule.dataBindingIdlingResource.monitorFragment(fragmentScenario)
        onView(withText("Authentication type")).perform(click())
        onView(withText("Ask for password")).perform(click())

        onView(withId(R.id.md_root)).check(doesNotExist())
        assertNull(testRule.cipher.decryptPassFromSettings())
        assertThat(prefWrapper.getString(appLockKey), `is`(applockPasswd))
    }

    @Test
    fun goodPass_errorWriteSettings_showSnackbar() {
        testRule.setDb()
        prefWrapper.setPref(KEY_STORAGE_TYPE_KEY, KEY_STORAGE_HARDWARE)
        prefWrapper.setPref(appLockKey, applockPasswd)
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
        assertThat(prefWrapper.getString(appLockKey), `is`(applockPasswd))
    }

    @Test
    fun softBack_checkNoMoreWarnings_warningNotDisplayedAgain() {
        prefWrapper.setPref(appLockKey, applockPasswd)
        prefWrapper.removePref(ENCRYPTED_PASS_KEY)
        prefWrapper.removePref(DO_NOT_SHOW_WARNING_KEY)
        prefWrapper.setPref(KEY_STORAGE_TYPE_KEY, KEY_STORAGE_SOFTWARE)

        val fragmentScenario = launchFragmentInContainer<SettingsFragment>(null, R.style.AppTheme)
        testRule.dataBindingIdlingResource.monitorFragment(fragmentScenario)
        onView(withText("Authentication type")).perform(click())
        onView(withText("No authentication")).perform(click())

        onView(withId(R.id.stop_showing_warning)).perform(click())
        onView(withText("OK")).perform(click())
        runBlocking {
            delay(200)
        }
        onView(withText("CANCEL")).perform(click())

        onView(withText("Authentication type")).perform(click())
        onView(withText("No authentication")).perform(click())

        onView(withId(R.id.soft_storage_warning)).check(doesNotExist())
    }
}