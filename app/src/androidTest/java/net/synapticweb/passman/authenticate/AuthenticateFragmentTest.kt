package net.synapticweb.passman.authenticate

import androidx.test.core.app.ActivityScenario.launch
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import net.synapticweb.passman.util.CryptoPassTestRule
import net.synapticweb.passman.MainActivity
import net.synapticweb.passman.PASSPHRASE_SET_KEY
import net.synapticweb.passman.R
import net.synapticweb.passman.model.Hash
import net.synapticweb.passman.util.*
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Rule
import org.junit.Test


class AuthenticateFragmentTest {
    @get:Rule
    val testRule = CryptoPassTestRule()

    @Test
    fun dbNotInitialized_PasswdNoMatch_Error() {
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
        val activityScenario = launch(MainActivity::class.java)
        testRule.dataBindingIdlingResource.monitorActivity(activityScenario)

        onView(withId(R.id.send_pass)).perform(click())

        onView(withId(R.id.pass_layout)).check(
            matches(hasTextInputLayoutErrorText(testRule.application.getString(R.string.pass_empty))))
    }


    @Test
    fun dbNotInitialized_goodPasswds_Authenticate()  {
        val activityScenario = launch(MainActivity::class.java)
        testRule.dataBindingIdlingResource.monitorActivity(activityScenario)

        onView(withId(R.id.passphrase)).perform(typeText("test"), closeSoftKeyboard())
        onView(withId(R.id.passphrase_retype)).perform(typeText("test"), closeSoftKeyboard())
        onView(withId(R.id.send_pass)).perform(click())


        onView(withId(R.id.secrets_list)).check(matches(isDisplayed()))
        assertThat(testRule.repository.isUnlocked(), `is`(true))

        assertThat(testRule.getBoolean(PASSPHRASE_SET_KEY), `is`(true))

       val hashObj : Hash = runBlocking {
           //delay este necesar deoarece în momentul în care se apasă pe butonul send se apelează
           //createPassHash care rulează asincron.
           delay(600)
           testRule.repository.getHash()
        }

        val currentHash = byteArrayToHexStr(createHash("test", hexStrToByteArray(hashObj.salt)))
        assertThat(currentHash, `is`(hashObj.hash))
    }

    @Test
    fun dbInitialized_badPasswd_Error() {
        runBlocking {
            testRule.repository.unlock("test".toByteArray())
            testRule.repository.lock()
        }
        testRule.setBoolean(PASSPHRASE_SET_KEY, true)

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
        testRule.setBoolean(PASSPHRASE_SET_KEY, true)

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
            testRule.repository.unlock("test".toByteArray())
            testRule.repository.lock()
        }
        testRule.setBoolean(PASSPHRASE_SET_KEY, true)

        val activityScenario = launch(MainActivity::class.java)
        testRule.dataBindingIdlingResource.monitorActivity(activityScenario)

        onView(withId(R.id.passphrase)).perform(typeText("test"), closeSoftKeyboard())
        onView(withId(R.id.send_pass)).perform(click())

        onView(withId(R.id.secrets_list)).check(matches(isDisplayed()))
        assertThat(testRule.repository.isUnlocked(), `is`(true))
    }
}