package net.synapticweb.passman.settings

import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers
import junit.framework.Assert.assertNotNull
import kotlinx.coroutines.runBlocking
import net.synapticweb.passman.*
import net.synapticweb.passman.di.TestAppComponent
import net.synapticweb.passman.model.Hash
import net.synapticweb.passman.util.*
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class ChangeHashTest {
    @get:Rule
    val testRule = CryptoPassTestRule()

    private lateinit var cipher : TestCryptoPassCipher

    @Before
    fun init() {
        cipher = (testRule.application.appComponent as TestAppComponent).cipher as TestCryptoPassCipher
    }

    @Test
    fun emptyPass_error() {
        val fragmentScenario = launchFragmentInContainer<SettingsFragment>(null, R.style.AppTheme)
        testRule.dataBindingIdlingResource.monitorFragment(fragmentScenario)

        Espresso.onView(ViewMatchers.withText("Hash function")).perform(ViewActions.click())
        Espresso.onView(ViewMatchers.withText("SHA512 (fast - medium security)")).perform(ViewActions.click())
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
        testRule.setDb()
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
        assertThat(testRule.getString(HASH_TYPE_KEY) == HASH_SHA_VALUE, `is`(true))
        val currentHash = runBlocking {
            createHashString(
                TEST_PASS.toCharArray(), hexStrToByteArray(hashObj!!.salt), HASH_SHA_VALUE)
        }
        assertThat(currentHash, `is`(hashObj!!.hash))
    }
}