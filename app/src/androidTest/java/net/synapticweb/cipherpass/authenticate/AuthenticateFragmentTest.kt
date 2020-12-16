package net.synapticweb.cipherpass.authenticate

import androidx.test.core.app.ActivityScenario.launch
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import junit.framework.Assert.assertNotNull
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import net.synapticweb.cipherpass.*
import net.synapticweb.cipherpass.util.CipherPassTestRule
import net.synapticweb.cipherpass.model.Hash
import net.synapticweb.cipherpass.util.*
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Rule
import org.junit.Test


class AuthenticateFragmentTest {
    @get:Rule
    val testRule = CipherPassTestRule()
    private val prefWrapper = PrefWrapper.getInstance(ApplicationProvider.getApplicationContext())

    @Test
    fun dbNotInitialized_PasswdNoMatch_Error() {
        prefWrapper.removePref(PASSPHRASE_SET_KEY)
        prefWrapper.setPref(APPLOCK_KEY, APPLOCK_PASSWD_VALUE)

        val activityScenario = launch(MainActivity::class.java)
        testRule.dataBindingIdlingResource.monitorActivity(activityScenario)

        onView(withId(R.id.passphrase)).perform(typeText("test"), closeSoftKeyboard())
        onView(withId(R.id.passphrase_retype)).perform(typeText("test2"), closeSoftKeyboard())
        onView(withId(R.id.send_pass)).perform(click())

        onView(withId(R.id.pass_layout)).check(
            matches(hasTextInputLayoutErrorText(testRule.application.getString(R.string.pass_no_match))))

        assertThat(testRule.repository.isUnlocked(), `is`(false))
    }

    @Test
    fun dbNotInitialized_EmptyPasswd_Error() {
        prefWrapper.removePref(PASSPHRASE_SET_KEY)
        prefWrapper.setPref(APPLOCK_KEY, APPLOCK_PASSWD_VALUE)

        val activityScenario = launch(MainActivity::class.java)
        testRule.dataBindingIdlingResource.monitorActivity(activityScenario)

        onView(withId(R.id.send_pass)).perform(click())

        onView(withId(R.id.pass_layout)).check(
            matches(hasTextInputLayoutErrorText(testRule.application.getString(R.string.pass_empty))))
    }


    @Test
    fun dbNotInitialized_goodPasswds_Authenticate()  {
        prefWrapper.removePref(PASSPHRASE_SET_KEY)
        prefWrapper.setPref(APPLOCK_KEY, APPLOCK_PASSWD_VALUE)

        val activityScenario = launch(MainActivity::class.java)
        testRule.dataBindingIdlingResource.monitorActivity(activityScenario)

        onView(withId(R.id.passphrase)).perform(typeText(TEST_PASS), closeSoftKeyboard())
        onView(withId(R.id.passphrase_retype)).perform(typeText(TEST_PASS), closeSoftKeyboard())
        onView(withId(R.id.send_pass)).perform(click())

        onView(withId(R.id.entries_list_root)).check(matches(isDisplayed()))
        assertThat(testRule.repository.isUnlocked(), `is`(true))

        assertThat(prefWrapper.getBoolean(PASSPHRASE_SET_KEY), `is`(true))

       val hashObj : Hash? = runBlocking {
           testRule.repository.getHash()
        }

        assertNotNull(hashObj)
        val currentHash = runBlocking {
            testRule.repository.createHashString(
                TEST_PASS.toCharArray(), hexStrToByteArray(hashObj!!.salt), prefWrapper.getString(
                HASH_TYPE_KEY) ?: HASH_PBKDF2)
        }
        assertThat(currentHash, `is`(hashObj!!.hash))
    }

    @Test
    fun dbInitialized_badPasswd_Error() {
        runBlocking {
            testRule.repository.unlock(TEST_PASS.toByteArray())
            testRule.repository.lock()
        }
        prefWrapper.setPref(PASSPHRASE_SET_KEY, true)
        prefWrapper.setPref(APPLOCK_KEY, APPLOCK_PASSWD_VALUE)

        val activityScenario = launch(MainActivity::class.java)
        testRule.dataBindingIdlingResource.monitorActivity(activityScenario)

        onView(withId(R.id.passphrase)).perform(typeText("test1"), closeSoftKeyboard())
        onView(withId(R.id.send_pass)).perform(click())

        onView(withId(R.id.pass_layout)).check(
            matches(hasTextInputLayoutErrorText(testRule.application.getString(R.string.pass_incorect))))

        assertThat(testRule.repository.isUnlocked(), `is`(false))
    }

    @Test
    fun appRunnedOnce_emptyPasswd_Error() {
        prefWrapper.setPref(PASSPHRASE_SET_KEY, true)
        prefWrapper.setPref(APPLOCK_KEY, APPLOCK_PASSWD_VALUE)

        val activityScenario = launch(MainActivity::class.java)
        testRule.dataBindingIdlingResource.monitorActivity(activityScenario)

        onView(withId(R.id.send_pass)).perform(click())

        onView(withId(R.id.pass_layout)).check(
            matches(hasTextInputLayoutErrorText(testRule.application.getString(R.string.pass_empty))))

        assertThat(testRule.repository.isUnlocked(), `is`(false))
    }



    @Test
    fun dbInitialized_goodPasswd_Authenticate() {
        runBlocking {
            testRule.repository.unlock(TEST_PASS.toByteArray())
            testRule.repository.lock()
        }
        prefWrapper.setPref(PASSPHRASE_SET_KEY, true)
        prefWrapper.setPref(APPLOCK_KEY, APPLOCK_PASSWD_VALUE)

        val activityScenario = launch(MainActivity::class.java)
        testRule.dataBindingIdlingResource.monitorActivity(activityScenario)

        onView(withId(R.id.passphrase)).perform(typeText(TEST_PASS), closeSoftKeyboard())
        onView(withId(R.id.send_pass)).perform(click())

        onView(withId(R.id.entries_list_root)).check(matches(isDisplayed()))
        assertThat(testRule.repository.isUnlocked(), `is`(true))
    }

    @Test
    fun dbNotInitialized_createHashReturnFalse_showSnackbar() {
        testRule.repository.createPassHashFalse = true
        prefWrapper.removePref(PASSPHRASE_SET_KEY)
        prefWrapper.setPref(APPLOCK_KEY, APPLOCK_PASSWD_VALUE)
        val activityScenario = launch(MainActivity::class.java)
        testRule.dataBindingIdlingResource.monitorActivity(activityScenario)

        onView(withId(R.id.passphrase)).perform(typeText(TEST_PASS), closeSoftKeyboard())
        onView(withId(R.id.passphrase_retype)).perform(typeText(TEST_PASS), closeSoftKeyboard())
        onView(withId(R.id.send_pass)).perform(click())

        //Testăm dacă arată snackbarul: https://stackoverflow.com/a/39915776
        onView(withText(testRule.application.getString(R.string.error_setting_pass)))
            .check(matches(withEffectiveVisibility(Visibility.VISIBLE)))

        val dbFile = testRule.application.getDatabasePath(TEST_DATABASE_NAME)
        assertThat(dbFile.exists(), `is`(false))
    }

    @Test
    fun dbInitialized_noLock_authenticate() {
        testRule.setDb()
        runBlocking {
            testRule.cipher.encryptPassToSettings(TEST_PASS.toCharArray())
        }
        testRule.repository.lock()
        prefWrapper.setPref(PASSPHRASE_SET_KEY, true)
        prefWrapper.setPref(APPLOCK_KEY, APPLOCK_NOLOCK_VALUE)

        val activityScenario = launch(MainActivity::class.java)
        testRule.dataBindingIdlingResource.monitorActivity(activityScenario)

        runBlocking {
            delay(300)
        }
        onView(withId(R.id.entries_list_root)).check(matches(isDisplayed()))
        assertThat(testRule.repository.isUnlocked(), `is`(true))
    }

    @Test
    fun dbInitialized_noLock_readPreferenceError() {
        testRule.setDb()
        testRule.repository.lock()
        prefWrapper.removePref(ENCRYPTED_PASS_KEY)
        prefWrapper.setPref(PASSPHRASE_SET_KEY, true)
        prefWrapper.setPref(APPLOCK_KEY, APPLOCK_NOLOCK_VALUE)

        val activityScenario = launch(MainActivity::class.java)
        testRule.dataBindingIdlingResource.monitorActivity(activityScenario)

        onView(withText(testRule.application.getString(R.string.system_lock_unavailable)))
            .check(matches(withEffectiveVisibility(Visibility.VISIBLE)))
    }
}