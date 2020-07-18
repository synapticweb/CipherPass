package net.synapticweb.passman.settings

import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers
import junit.framework.Assert.assertNotNull
import kotlinx.coroutines.runBlocking
import net.synapticweb.passman.*
import net.synapticweb.passman.model.Hash
import net.synapticweb.passman.util.*
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Rule
import org.junit.Test

class ChangeHashTest {
    @get:Rule
    val testRule = CryptoPassTestRule()
    private val prefWrapper = PrefWrapper.getInstance(ApplicationProvider.getApplicationContext())

    @Test
    fun emptyPass_error() {
        val fragmentScenario = launchFragmentInContainer<SettingsFragment>(null, R.style.AppTheme)
        testRule.dataBindingIdlingResource.monitorFragment(fragmentScenario)

        Espresso.onView(ViewMatchers.withText("Hash function")).perform(ViewActions.click())
        Espresso.onView(ViewMatchers.withText("SHA512 (fast - medium security)")).perform(ViewActions.click())
        Thread.sleep(100)
        Espresso.onView(ViewMatchers.withText("OK")).perform(ViewActions.click())

        Espresso.onView(ViewMatchers.withId(R.id.pass_layout)).check(
            ViewAssertions.matches(hasTextInputLayoutErrorText(testRule.application.getString(R.string.pass_empty)))
        )
    }

    @Test
    fun badPass_error() {
        testRule.setDb()
        val fragmentScenario = launchFragmentInContainer<SettingsFragment>(null, R.style.AppTheme)
        testRule.dataBindingIdlingResource.monitorFragment(fragmentScenario)

        Espresso.onView(ViewMatchers.withText("Hash function")).perform(ViewActions.click())
        Espresso.onView(ViewMatchers.withText("SHA512 (fast - medium security)")).perform(ViewActions.click())
        Espresso.onView(ViewMatchers.withId(R.id.passphrase))
            .perform(ViewActions.typeText("test1"), ViewActions.closeSoftKeyboard())

        Espresso.onView(ViewMatchers.withText("OK")).perform(ViewActions.click())
        Espresso.onView(ViewMatchers.withId(R.id.pass_layout)).check(
            ViewAssertions.matches(hasTextInputLayoutErrorText(testRule.application.getString(R.string.pass_incorect))))

    }

    @Test
    fun goodPass_hashFunctionChanged() {
        testRule.setDb(HASH_MD5_VALUE)
        prefWrapper.setPref(HASH_TYPE_KEY, HASH_MD5_VALUE)
        val fragmentScenario = launchFragmentInContainer<SettingsFragment>(null, R.style.AppTheme)
        testRule.dataBindingIdlingResource.monitorFragment(fragmentScenario)

        Espresso.onView(ViewMatchers.withText("Hash function")).perform(ViewActions.click())
        Espresso.onView(ViewMatchers.withText("SHA512 (fast - medium security)")).perform(ViewActions.click())
        Espresso.onView(ViewMatchers.withId(R.id.passphrase))
            .perform(ViewActions.typeText(TEST_PASS), ViewActions.closeSoftKeyboard())
        Espresso.onView(ViewMatchers.withText("OK")).perform(ViewActions.click())

        val hashObj : Hash? = runBlocking {
            testRule.repository.getHash()
        }

        assertNotNull(hashObj)
        assertThat(prefWrapper.getString(HASH_TYPE_KEY), `is`(HASH_SHA_VALUE))
        val currentHash = runBlocking {
            testRule.repository.createHashString(
                TEST_PASS.toCharArray(), hexStrToByteArray(hashObj!!.salt), HASH_SHA_VALUE)
        }
        assertThat(currentHash, `is`(hashObj!!.hash))
    }

    @Test
    fun goodPass_errorPassHash() {
        testRule.setDb(HASH_MD5_VALUE)
        prefWrapper.setPref(HASH_TYPE_KEY, HASH_MD5_VALUE)
        testRule.repository.createPassHashFalse = true
        val fragmentScenario = launchFragmentInContainer<SettingsFragment>(null, R.style.AppTheme)
        testRule.dataBindingIdlingResource.monitorFragment(fragmentScenario)

        Espresso.onView(ViewMatchers.withText("Hash function")).perform(ViewActions.click())
        Espresso.onView(ViewMatchers.withText("SHA512 (fast - medium security)")).perform(ViewActions.click())
        Espresso.onView(ViewMatchers.withId(R.id.passphrase))
            .perform(ViewActions.typeText(TEST_PASS), ViewActions.closeSoftKeyboard())
        Espresso.onView(ViewMatchers.withText("OK")).perform(ViewActions.click())

        Espresso.onView(ViewMatchers.withText(R.string.hash_change_error))
            .inRoot(isToast()).check(ViewAssertions.matches(ViewMatchers.isDisplayed()))

        assertThat(prefWrapper.getString(HASH_TYPE_KEY), `is`(HASH_MD5_VALUE))
    }
}