package net.synapticweb.passman.entrydetail

import androidx.appcompat.view.menu.ActionMenuItem
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.withText
import junit.framework.Assert.assertNull
import kotlinx.coroutines.runBlocking
import net.synapticweb.passman.R
import net.synapticweb.passman.TestCryptoPassApp
import net.synapticweb.passman.model.Entry
import net.synapticweb.passman.util.CryptoPassTestRule
import net.synapticweb.passman.util.monitorFragment
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito

class EntryDetailFragmentTest {
    @get:Rule
    val testRule = CryptoPassTestRule()

    @Test
    fun deleteEntry_wipedFromDb() = runBlocking {
        testRule.setDb()

        val item = Entry()
        item.entryName = "account_name"
        item.password = "password"
        testRule.repository.insertEntry(item)

        val bundle = EntryDetailFragmentArgs(1, "account_name").toBundle()
        val mockNav = Mockito.mock(NavController::class.java)
        val fragmentScenario = launchFragmentInContainer(bundle, R.style.AppTheme) {
            EntryDetailFragment().also { fragment ->
                fragment.viewLifecycleOwnerLiveData.observeForever { viewLifeCycleOwner ->
                    if(viewLifeCycleOwner != null)
                        Navigation.setViewNavController(fragment.requireView(), mockNav)
                }
            }
        }
        testRule.dataBindingIdlingResource.monitorFragment(fragmentScenario)
        //https://stackoverflow.com/a/59129586
        val context = ApplicationProvider.getApplicationContext<TestCryptoPassApp>()
        val deleteMenuItem = ActionMenuItem(context, 0, R.id.delete, 0, 0, null)
        fragmentScenario.onFragment { fragment ->  
            fragment.onOptionsItemSelected(deleteMenuItem)
        }
        onView(withText(context.getString(android.R.string.ok))).perform(click())
        assertNull(testRule.repository.getEntry(1))
    }
}