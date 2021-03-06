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
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import junit.framework.Assert.assertNotNull
import junit.framework.Assert.assertNull
import kotlinx.coroutines.runBlocking
import net.synapticweb.cipherpass.*
import net.synapticweb.cipherpass.util.*
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Rule
import org.junit.Test


class ChangePassTest {
    @get:Rule
    val testRule = CipherPassTestRule()
    private val prefWrapper = PrefWrapper.getInstance(ApplicationProvider.getApplicationContext())
    private val appLockKey = testRule.application.resources.getString(R.string.applock_key)
    private val applockPasswd = testRule.application.resources.getString(R.string.applock_passwd_value)
    private val appLockSystem = testRule.application.resources.getString(R.string.applock_system_value)

    @Test
    fun emptyNewPass_Error() {
        val fragmentScenario = launchFragmentInContainer<SettingsFragment>(null, R.style.AppTheme)
        testRule.dataBindingIdlingResource.monitorFragment(fragmentScenario)

        onView(withText(R.string.change_pass_title)).perform(click())
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

        onView(withText(R.string.change_pass_title)).perform(click())
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

        onView(withText(R.string.change_pass_title)).perform(click())
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
        prefWrapper.setPref(appLockKey, applockPasswd)
        prefWrapper.removePref(ENCRYPTED_PASS_KEY)

        val fragmentScenario = launchFragmentInContainer<SettingsFragment>(null, R.style.AppTheme)
        testRule.dataBindingIdlingResource.monitorFragment(fragmentScenario)

        onView(withText(R.string.change_pass_title)).perform(click())
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
            testRule.repository.isPassValid("test1".toCharArray())
        }

        assertThat(passValid, `is`(true))
        assertNull(prefWrapper.getString(ENCRYPTED_PASS_KEY))
    }

    @Test
    fun correctInput_weakPass_passChange() {
        testRule.setDb()
        prefWrapper.setPref(appLockKey, appLockSystem)

        val fragmentScenario = launchFragmentInContainer<SettingsFragment>(null, R.style.AppTheme)
        testRule.dataBindingIdlingResource.monitorFragment(fragmentScenario)

        onView(withText(R.string.change_pass_title)).perform(click())
        onView(withId(R.id.actual_passphrase)).perform(typeText(TEST_PASS), closeSoftKeyboard())

        onView(withId(R.id.new_passphrase)).perform(typeText("test1"), closeSoftKeyboard())
        onView(withId(R.id.new_passphrase_retype)).perform(typeText("test1"), closeSoftKeyboard())

        onView(withText("OK")).perform(click())

        val encryptedPass = prefWrapper.getString(ENCRYPTED_PASS_KEY)
        assertNotNull(encryptedPass)

        assertThat(String(testRule.cipher.decrypt(hexStrToByteArray(encryptedPass!!))),
            `is`("test1"))
    }

    @Test
    fun correctInput_weakAuth_errorPassHash() {
        testRule.setDb()
        testRule.repository.createPassHashFalse = true
        prefWrapper.setPref(appLockKey, appLockSystem)
        val fragmentScenario = launchFragmentInContainer<SettingsFragment>(null, R.style.AppTheme)
        testRule.dataBindingIdlingResource.monitorFragment(fragmentScenario)

        onView(withText(R.string.change_pass_title)).perform(click())
        onView(withId(R.id.actual_passphrase)).perform(typeText(TEST_PASS), closeSoftKeyboard())

        onView(withId(R.id.new_passphrase)).perform(typeText("test1"), closeSoftKeyboard())
        onView(withId(R.id.new_passphrase_retype)).perform(typeText("test1"), closeSoftKeyboard())

        //verificăm toastul
        onView(withText("OK")).perform(click())
        onView(withText(R.string.change_pass_error)).inRoot(isToast()).check(matches(isDisplayed()))

        runBlocking {
            testRule.repository.lock()
            testRule.repository.unlock(TEST_PASS.toByteArray())
        }
        assertThat(testRule.repository.isUnlocked(), `is`(true)) //parola nu s-a schimbat
        assertNull(prefWrapper.getString(ENCRYPTED_PASS_KEY)) //nu s-a salvat parola criptată
        assertThat(prefWrapper.getString(appLockKey), `is`(applockPasswd))
    }

    @Test
    fun correctInput_weakAuth_errorWriteEncryptedPass() {
        testRule.setDb()
        testRule.cipher.encryptPassReturnError = true
        prefWrapper.setPref(appLockKey, appLockSystem)
        val fragmentScenario = launchFragmentInContainer<SettingsFragment>(null, R.style.AppTheme)
        testRule.dataBindingIdlingResource.monitorFragment(fragmentScenario)

        onView(withText(R.string.change_pass_title)).perform(click())
        onView(withId(R.id.actual_passphrase)).perform(typeText(TEST_PASS), closeSoftKeyboard())

        onView(withId(R.id.new_passphrase)).perform(typeText("test1"), closeSoftKeyboard())
        onView(withId(R.id.new_passphrase_retype)).perform(typeText("test1"), closeSoftKeyboard())

        //verificăm toastul
        onView(withText("OK")).perform(click())
        onView(withText(R.string.change_pass_error)).inRoot(isToast()).check(matches(isDisplayed()))

        runBlocking {
            testRule.repository.lock()
            testRule.repository.unlock(TEST_PASS.toByteArray())
        }
        assertThat(testRule.repository.isUnlocked(), `is`(true)) //parola nu s-a schimbat
        assertNull(prefWrapper.getString(ENCRYPTED_PASS_KEY)) //nu s-a salvat parola criptată
        assertThat(prefWrapper.getString(appLockKey), `is`(applockPasswd))
    }
}