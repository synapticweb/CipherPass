package net.synapticweb.passman.addeditentry

import androidx.appcompat.view.menu.ActionMenuItem
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition
import androidx.test.espresso.matcher.ViewMatchers.*
import junit.framework.Assert.assertNotNull
import junit.framework.Assert.assertNull
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import net.synapticweb.passman.R
import net.synapticweb.passman.TestCryptoPassApp
import net.synapticweb.passman.model.CustomField
import net.synapticweb.passman.model.Entry
import net.synapticweb.passman.model.SortOrder
import net.synapticweb.passman.util.*
import org.hamcrest.core.Is.`is`
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito
import androidx.fragment.app.testing.FragmentScenario

class AddeditEntryFragmentTest {
    @get:Rule
    val testRule = CryptoPassTestRule()

    private fun createCustomField(name : String) {
        onView(withId(R.id.add_new_field)).perform(click())
        onView(withId(R.id.field_name_input)).perform(typeText(name), closeSoftKeyboard())
        onView(withText(android.R.string.ok)).perform(click())
    }

    private fun createScenarioWithNavController(entryId: Long, title : String) :
            FragmentScenario<AddeditEntryFragment> {
        val bundle = AddeditEntryFragmentArgs(entryId, title).toBundle()
        val mockNav = Mockito.mock(NavController::class.java)
        val fragmentScenario = launchFragmentInContainer(bundle, R.style.AppTheme) {
            AddeditEntryFragment().also { fragment ->
                fragment.viewLifecycleOwnerLiveData.observeForever { viewLifeCycleOwner ->
                    if(viewLifeCycleOwner != null)
                        Navigation.setViewNavController(fragment.requireView(), mockNav)
                }
            }
        }
        testRule.dataBindingIdlingResource.monitorFragment(fragmentScenario)
        return fragmentScenario
    }

    private fun createScenarioWithoutNavController(entryId: Long, title : String) :
            FragmentScenario<AddeditEntryFragment> {
        val bundle = AddeditEntryFragmentArgs(entryId, title).toBundle()
        val fragmentScenario =
            launchFragmentInContainer<AddeditEntryFragment>(bundle, R.style.AppTheme)
        testRule.dataBindingIdlingResource.monitorFragment(fragmentScenario)
        return fragmentScenario
    }

    private fun closeFragment(scenario: FragmentScenario<AddeditEntryFragment>) {
        val context = ApplicationProvider.getApplicationContext<TestCryptoPassApp>()
        val closeMenuItem = ActionMenuItem(context, 0, R.id.close, 0, 0, null)
        scenario.onFragment { fragment ->
            fragment.onOptionsItemSelected(closeMenuItem)
        }
    }

    @Test
    fun addEntry_nameEmpty_showToast() {
        testRule.setDb()
        createScenarioWithoutNavController(0, "New entry")
        onView(withId(R.id.username)).perform(typeText("test_username"))
        onView(withId(R.id.save)).perform(click())

        onView(withText(R.string.addedit_name_empty)).inRoot(isToast()).check(matches(isDisplayed()))
    }


    @Test
    fun addEdit_passwdNoMatch_showToast() {
        testRule.setDb()
        createScenarioWithoutNavController(0, "New entry")
        onView(withId(R.id.name)).perform(typeText("test_name"))
        onView(withId(R.id.username)).perform(typeText("test_username"))
        onView(withId(R.id.pass)).perform(typeText("test_pass"))
        onView(withId(R.id.repass)).perform(typeText("test_pass2"))
        onView(withId(R.id.save)).perform(click())

        onView(withText(R.string.addedit_pass_nomatch)).inRoot(isToast()).check(matches(isDisplayed()))
    }

    @Test
    fun addEdit_goodInputSave_recordInDb() {
        testRule.setDb()
        createScenarioWithNavController(0, "New entry")
        onView(withId(R.id.name)).perform(typeText("test_name"))
        onView(withId(R.id.username)).perform(typeText("test_username"))
        onView(withId(R.id.pass)).perform(typeText("test_pass"))
        onView(withId(R.id.repass)).perform(typeText("test_pass"))
        onView(withId(R.id.url)).perform(typeText("url"), closeSoftKeyboard())
        onView(withId(R.id.comment)).perform(typeText("comment"), closeSoftKeyboard())
        onView(withId(R.id.save)).perform(click())

        val entry : Entry? = testRule.repository.getAllEntries(SortOrder.CREATION_DATE).value?.get(0)
        if (entry != null) {
            assertThat(entry.entryName, `is`("test_name"))
            assertThat(entry.username, `is`("test_username"))
            assertThat(entry.password, `is`("test_pass"))
            assertThat(entry.url, `is`("url"))
            assertThat(entry.comment, `is`("comment"))
        }
    }

    @Test
    fun addEdit_edit_notDirty_showsToast(): Unit = runBlocking {
        testRule.setDb()

        val item = Entry()
        item.entryName = "account_name"
        item.username = "username"
        item.password = "password"
        item.url = "url"
        testRule.repository.insertEntry(item)

        createScenarioWithNavController(0, "New entry")

        onView(withId(R.id.save)).perform(click())
        onView(withText(R.string.addedit_nochange)).inRoot(isToast()).check(matches(isDisplayed()))
        return@runBlocking
    }

    @Test
    fun addEdit_edit_retainChanges() = runBlocking {
        testRule.setDb()

        val item = Entry()
        item.entryName = "account_name"
        item.username = "username"
        item.password = "password"
        item.url = "url"
        testRule.repository.insertEntry(item)

        createScenarioWithNavController(1, "account_name")

        onView(withId(R.id.username)).perform(replaceText("username_changed"), closeSoftKeyboard())
        onView(withId(R.id.pass)).perform(replaceText("password_changed"), closeSoftKeyboard())
        onView(withId(R.id.repass)).perform(replaceText("password_changed"), closeSoftKeyboard())
        onView(withId(R.id.comment)).perform(typeText("comment"), closeSoftKeyboard())
        onView(withId(R.id.save)).perform(click())

        val entry : Entry = testRule.repository.getEntry(1)

        assertThat(entry.entryName, `is`("account_name"))
        assertThat(entry.username, `is`("username_changed"))
        assertThat(entry.password, `is`("password_changed"))
        assertThat(entry.url, `is`("url"))
        assertThat(entry.comment, `is`("comment"))
    }


    @Test
    fun newEntry_addField() = runBlocking {
        testRule.setDb()
        createScenarioWithoutNavController(0, "New entry")
        createCustomField("custom_field")

        onView(withHint("custom_field")).check(matches(isDisplayed()))
        val customField = testRule.repository.getCustomField(1)

        assertNotNull(customField)
        assertThat(customField.entry, `is`(0L))
        assertThat(customField.fieldName, `is`("custom_field"))
        assertThat(customField.value, `is`(""))
    }

    @Test
    fun newEntry_addField_close_cleanDb() = runBlocking {
        testRule.setDb()

        val fragmentScenario = createScenarioWithNavController(0, "New entry")
        createCustomField("custom_field")

        onView(withHint("custom_field")).check(matches(isDisplayed()))
        var customField = testRule.repository.getCustomField(1)
        assertNotNull(customField)
        closeFragment(fragmentScenario)

        delay(400)

        customField = testRule.repository.getCustomField(1)
        assertNull(customField)
    }

    @Test
    fun newEntry_addField_delete_dissapears_staysInDb() = runBlocking {
        testRule.setDb()
        createScenarioWithoutNavController(0, "New entry")
        createCustomField("custom_field")
        onView(withHint("custom_field")).check(matches(isDisplayed()))

        onView(withId(R.id.custom_fields)).perform(actionOnItemAtPosition
        <CustomFieldsAdapter.ViewHolder>(0, RecyclerViewActions.clickChildViewWithId(R.id.delete_field)))
        delay(200)
        onView(withHint("custom_field")).check(doesNotExist())

        val customField = testRule.repository.getCustomField(1)
        assertNotNull(customField)
    }

    @Test
    fun newEntry_addField_save_retain() = runBlocking {
        testRule.setDb()
        createScenarioWithNavController(0, "New entry")
        createCustomField("custom_field")

        var customField = testRule.repository.getCustomField(1)
        assertNotNull(customField)

        onView(withHint("custom_field")).perform(typeText("field_value"), closeSoftKeyboard())
        onView(withId(R.id.name)).perform(typeText("entry_name"), closeSoftKeyboard())

        onView(withId(R.id.save)).perform(click())

        val entry = testRule.repository.getEntry(1)
        customField = testRule.repository.getCustomField(1)

        assertThat(customField.entry, `is`(entry.id))
        assertThat(customField.value, `is`("field_value"))
        assertThat(entry.entryName, `is`("entry_name"))
    }

    @Test
    fun editEntry_addField_saveRetain() = runBlocking {
        testRule.setDb()
        val entry = Entry()
        entry.entryName = "account_name"
        entry.username = "username"
        testRule.repository.insertEntry(entry)

        createScenarioWithNavController(1, "account_name")
        createCustomField("custom_field")

        var customField = testRule.repository.getCustomField(1)
        assertNotNull(customField)
        assertThat(customField.entry, `is`(1L))

        onView(withHint("custom_field")).perform(typeText("field_value"), closeSoftKeyboard())

        onView(withId(R.id.save)).perform(click())
        customField = testRule.repository.getCustomField(1)

        assertThat(customField.entry, `is`(1L))
        assertThat(customField.value, `is`("field_value"))
    }

    @Test
    fun editEntry_addField_close_cleanDb() = runBlocking {
        testRule.setDb()
        val entry = Entry()
        entry.entryName = "account_name"
        entry.username = "username"
        testRule.repository.insertEntry(entry)

        val fragmentScenario = createScenarioWithNavController(1, "account_name")
        createCustomField("custom_field")
        closeFragment(fragmentScenario)

        delay(400)

        val customField = testRule.repository.getCustomField(1)
        assertNull(customField)
    }

    @Test
    fun editEntry_editField_saveRetain() = runBlocking {
        testRule.setDb()
        val entry = Entry()
        entry.entryName = "account_name"
        testRule.repository.insertEntry(entry)

        var customField = CustomField(1, "field_name", "field_value")
        testRule.repository.insertCustomField(customField)

        createScenarioWithNavController(1, "account_name")
        onView(withHint("field_name")).perform(replaceText("field_value_modified"),
            closeSoftKeyboard())

        onView(withId(R.id.save)).perform(click())
        customField = testRule.repository.getCustomField(1)
        assertThat(customField.value, `is`("field_value_modified"))
    }

    @Test
    fun editEntry_editField_close_dbUnmodified() = runBlocking {
        testRule.setDb()
        val entry = Entry()
        entry.entryName = "account_name"
        testRule.repository.insertEntry(entry)

        var customField = CustomField(1, "field_name", "field_value")
        testRule.repository.insertCustomField(customField)

        val fragmentScenario = createScenarioWithNavController(1, "account_name")
        onView(withHint("field_name")).perform(replaceText("field_value_modified"),
            closeSoftKeyboard())

        closeFragment(fragmentScenario)

        delay(400)

        customField = testRule.repository.getCustomField(1)
        assertThat(customField.value, `is`("field_value"))
    }
}