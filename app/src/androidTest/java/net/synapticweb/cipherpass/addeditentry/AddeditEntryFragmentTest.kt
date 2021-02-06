/*
 * Copyright (C) 2021 Eugen Rădulescu <synapticwebb@gmail.com>
 * This file is part of the CipherPass project ( https://github.com/synapticweb/CipherPass ). 
 * See the LICENSE file in the project root for license terms. 
 */

package net.synapticweb.cipherpass.addeditentry

import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.Navigation
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
import net.synapticweb.cipherpass.R
import net.synapticweb.cipherpass.model.CustomField
import net.synapticweb.cipherpass.model.Entry
import net.synapticweb.cipherpass.util.*
import org.hamcrest.core.Is.`is`
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito

class AddeditEntryFragmentTest {
    @get:Rule
    val testRule = CipherPassTestRule()

    @Test
    fun nameEmpty_showToast() {
        val bundle = AddeditEntryFragmentArgs(0, "New entry").toBundle()
        val fragmentScenario =
            launchFragmentInContainer<AddeditEntryFragment>(bundle, R.style.AppTheme)
        testRule.dataBindingIdlingResource.monitorFragment(fragmentScenario)

        onView(withId(R.id.username)).perform(typeText("test_username"))
        onView(withId(R.id.save)).perform(click())

        onView(withText(R.string.addedit_name_empty)).inRoot(isToast()).check(matches(isDisplayed()))
    }


    @Test
    fun passwdNoMatch_showToast() {
        val bundle = AddeditEntryFragmentArgs(0, "New entry").toBundle()
        val fragmentScenario =
            launchFragmentInContainer<AddeditEntryFragment>(bundle, R.style.AppTheme)
        testRule.dataBindingIdlingResource.monitorFragment(fragmentScenario)

        onView(withId(R.id.name)).perform(typeText("test_name"))
        onView(withId(R.id.username)).perform(typeText("test_username"))
        onView(withId(R.id.pass)).perform(typeText("test_pass"))
        onView(withId(R.id.repass)).perform(typeText("test_pass2"))
        onView(withId(R.id.save)).perform(click())

        onView(withText(R.string.addedit_pass_nomatch)).inRoot(isToast()).check(matches(isDisplayed()))
    }

    @Test
    fun newEntry_save_retain() : Unit = runBlocking {
        testRule.setDb()
        val bundle = AddeditEntryFragmentArgs(0, "New entry").toBundle()
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

        onView(withId(R.id.name)).perform(typeText("test_name"))
        onView(withId(R.id.username)).perform(typeText("test_username"))
        onView(withId(R.id.pass)).perform(typeText("test_pass"))
        onView(withId(R.id.repass)).perform(typeText("test_pass"))
        onView(withId(R.id.url)).perform(typeText("url"), closeSoftKeyboard())
        onView(withId(R.id.comment)).perform(typeText("comment"), closeSoftKeyboard())

        onView(withId(R.id.save)).perform(click())

        val entry : Entry? = testRule.repository.getAllEntries(
            testRule.application.resources.getString(R.string.sort_creation_desc_value))
                .value?.get(0)
        if (entry != null) {
            assertThat(entry.entryName, `is`("test_name"))
            assertThat(entry.username, `is`("test_username"))
            assertThat(entry.password, `is`("test_pass"))
            assertThat(entry.url, `is`("url"))
            assertThat(entry.comment, `is`("comment"))
        }

        return@runBlocking
    }

    @Test
    fun newEntry_notDirty_showsToast(): Unit = runBlocking {
        val bundle = AddeditEntryFragmentArgs(0, "New entry").toBundle()
        val fragmentScenario =
            launchFragmentInContainer<AddeditEntryFragment>(bundle, R.style.AppTheme)
        testRule.dataBindingIdlingResource.monitorFragment(fragmentScenario)

        onView(withId(R.id.save)).perform(click())
        onView(withText(R.string.addedit_nochange)).inRoot(isToast()).check(matches(isDisplayed()))
        return@runBlocking
    }

    @Test
    fun editOldEntry_retainChanges() = runBlocking {
        testRule.setDb()

        val item = Entry(entryName = "account_name")
        item.username = "username"
        item.password = "password"
        item.url = "url"
        testRule.repository.insertEntry(item)

        val bundle = AddeditEntryFragmentArgs(1, "account_name").toBundle()
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
    fun addField_isDisplayed() : Unit = runBlocking {
        val bundle = AddeditEntryFragmentArgs(0, "New entry").toBundle()
        val fragmentScenario =
            launchFragmentInContainer<AddeditEntryFragment>(bundle, R.style.AppTheme)
        testRule.dataBindingIdlingResource.monitorFragment(fragmentScenario)

        onView(withId(R.id.add_new_field)).perform(click())
        onView(withId(R.id.field_name_input)).perform(typeText("custom_field"), closeSoftKeyboard())
        onView(withText(android.R.string.ok)).perform(click())

        onView(withHint("custom_field")).check(matches(isDisplayed()))
        Unit
    }

    @Test
    fun addField_deleteIt_isNotDisplayed() {
        val bundle = AddeditEntryFragmentArgs(0, "New entry").toBundle()
        val fragmentScenario =
            launchFragmentInContainer<AddeditEntryFragment>(bundle, R.style.AppTheme)
        testRule.dataBindingIdlingResource.monitorFragment(fragmentScenario)

        onView(withId(R.id.add_new_field)).perform(click())
        onView(withId(R.id.field_name_input)).perform(typeText("custom_field"), closeSoftKeyboard())
        onView(withText(android.R.string.ok)).perform(click())

        onView(withId(R.id.custom_fields)).perform(actionOnItemAtPosition
        <CustomFieldsAdapter.ViewHolder>(0, RecyclerViewActions.clickChildViewWithId(R.id.delete_field)))
        onView(withText(android.R.string.ok)).perform(click())

        onView(withHint("custom_field")).check(doesNotExist())
    }

    @Test
    fun addField_editIt_changeIsVisible() {
        val bundle = AddeditEntryFragmentArgs(0, "New entry").toBundle()
        val fragmentScenario =
            launchFragmentInContainer<AddeditEntryFragment>(bundle, R.style.AppTheme)
        testRule.dataBindingIdlingResource.monitorFragment(fragmentScenario)

        onView(withId(R.id.add_new_field)).perform(click())
        onView(withId(R.id.field_name_input)).perform(typeText("custom_field"), closeSoftKeyboard())
        onView(withText(android.R.string.ok)).perform(click())

        onView(withId(R.id.custom_fields)).perform(actionOnItemAtPosition
        <CustomFieldsAdapter.ViewHolder>(0, RecyclerViewActions.clickChildViewWithId(R.id.edit_field)))

        onView(withHint(R.string.new_field_input_hint)).perform(replaceText("custom_field2"), closeSoftKeyboard())
        onView(withText(android.R.string.ok)).perform(click())

        onView(withHint("custom_field2")).check(matches(isDisplayed()))
    }

    @Test
    fun editEntry_addField_save_retain() : Unit = runBlocking {
        testRule.setDb()
        val item = Entry(entryName = "account_name")
        testRule.repository.insertEntry(item)

        val bundle = AddeditEntryFragmentArgs(1, "account_name").toBundle()
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

        onView(withId(R.id.add_new_field)).perform(click())
        onView(withId(R.id.field_name_input)).perform(typeText("custom_field"), closeSoftKeyboard())
        onView(withText(android.R.string.ok)).perform(click())
        onView(withHint("custom_field")).perform(typeText("field_value"), closeSoftKeyboard())
        onView(withId(R.id.save)).perform(click())

        val field = testRule.repository.getCustomField(1)
        assertNotNull(field)
        assertThat(field.entry, `is`(1L))
        assertThat(field.fieldName, `is`("custom_field"))
        assertThat(field.value, `is`("field_value"))

        return@runBlocking
    }

    @Test
    fun editEntry_deleteField_save_retain() : Unit = runBlocking {
        testRule.setDb()
        val item = Entry(entryName = "account_name")
        testRule.repository.insertEntry(item)

        val cf = CustomField(1, "custom_field", false, "field_value")
        testRule.repository.insertCustomField(cf)

        val bundle = AddeditEntryFragmentArgs(1, "account_name").toBundle()
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

        onView(withId(R.id.custom_fields)).perform(actionOnItemAtPosition
        <CustomFieldsAdapter.ViewHolder>(0, RecyclerViewActions.clickChildViewWithId(R.id.delete_field)))
        onView(withText(android.R.string.ok)).perform(click())
        onView(withId(R.id.save)).perform(click())

        val cfields = testRule.repository.getCustomFieldsSync(1)
        assertThat(cfields.isEmpty(), `is`(true))

        val field = testRule.repository.getCustomField(1)
        assertNull(field)

        return@runBlocking
    }

    @Test
    fun editEntry_editFieldMetas_save_retain() : Unit = runBlocking {
        testRule.setDb()
        val item = Entry(entryName = "account_name")
        testRule.repository.insertEntry(item)

        val cf = CustomField(1, "custom_field", false, "field_value")
        testRule.repository.insertCustomField(cf)

        val bundle = AddeditEntryFragmentArgs(1, "account_name").toBundle()
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

        onView(withId(R.id.custom_fields)).perform(actionOnItemAtPosition
        <CustomFieldsAdapter.ViewHolder>(0, RecyclerViewActions.clickChildViewWithId(R.id.edit_field)))
        onView(withHint(R.string.new_field_input_hint)).perform(replaceText("custom_field2"), closeSoftKeyboard())
        onView(withId(R.id.protected_field)).perform(click())
        onView(withText(android.R.string.ok)).perform(click())

        onView(withId(R.id.save)).perform(click())

        val field = testRule.repository.getCustomFieldsSync(1)[0]
        assertThat(field.fieldName, `is`("custom_field2"))
        assertThat(field.isProtected, `is`(true))
        assertThat(field.value, `is`("field_value"))

        return@runBlocking
    }


    @Test
    fun editEntry_editFieldValue_saveDb() = runBlocking {
        testRule.setDb()
        val entry = Entry(entryName = "account_name")
        testRule.repository.insertEntry(entry)

        var customField = CustomField(1, "field_name", false, "field_value")
        testRule.repository.insertCustomField(customField)

        val bundle = AddeditEntryFragmentArgs(1, "account_name").toBundle()
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

        onView(withHint("field_name")).perform(replaceText(""), typeText("field_value_modified"),
            closeSoftKeyboard())

        onView(withId(R.id.save)).perform(click())
        customField = testRule.repository.getCustomField(1)
        assertThat(customField.value, `is`("field_value_modified"))
    }

    @Test
    fun newEntry_enterMultipleFields_saveDb() : Unit = runBlocking {
        testRule.setDb()
        val bundle = AddeditEntryFragmentArgs(0, "New entry").toBundle()
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

        onView(withId(R.id.name)).perform(typeText("test_name"), closeSoftKeyboard())

        onView(withId(R.id.add_new_field)).perform(click())

        delay(100)
        onView(withHint(R.string.new_field_input_hint)).perform(typeText("field1"), closeSoftKeyboard())
        onView(withId(R.id.protected_field)).perform(click())
        onView(withText(android.R.string.ok)).perform(click())

        onView(withId(R.id.addedit_scrollview)).perform(swipeUp())

        onView(withId(R.id.add_new_field)).perform(click())

        delay(100)
        onView(withHint(R.string.new_field_input_hint)).perform(typeText("field2"), closeSoftKeyboard())
        onView(withText(android.R.string.ok)).perform(click())

        onView(withId(R.id.addedit_scrollview)).perform(swipeUp())

        onView(withId(R.id.add_new_field)).perform(click())

        delay(100)
        onView(withHint(R.string.new_field_input_hint)).perform(typeText("field3"), closeSoftKeyboard())
        onView(withText(android.R.string.ok)).perform(click())

        //dacă aș fi scris textul cu ajutorul RecyclerViewActions inputul nu ar fi primit focus
        //și textul nu s-ar fi salvat.
        onView(withId(R.id.addedit_scrollview)).perform(swipeUp())
        onView(withHint("field3")).perform(typeText("field3_value"), closeSoftKeyboard())

        onView(withId(R.id.addedit_scrollview)).perform(swipeUp())
        onView(withHint("field2")).perform(typeText("field2_value"), closeSoftKeyboard())

        onView(withId(R.id.addedit_scrollview)).perform(swipeUp())
        onView(withHint("field1")).perform(typeText("field1_value"), closeSoftKeyboard())

        onView(withId(R.id.custom_fields)).perform(actionOnItemAtPosition
        <CustomFieldsAdapter.ViewHolder>(1,
            RecyclerViewActions.clickChildViewWithId(R.id.delete_field)))
        delay(100)
        onView(withText(android.R.string.ok)).perform(click())

        onView(withId(R.id.save)).perform(click())

        val fields = testRule.repository.getCustomFieldsSync(1L)
        assertThat(fields.size, `is`(2))
        assertThat(fields[0].entry, `is`(1L))
        assertThat(fields[0].fieldName, `is`("field1"))
        assertThat(fields[0].value, `is`("field1_value"))
        assertThat(fields[0].isProtected, `is`(true))

        assertThat(fields[1].entry, `is`(1L))
        assertThat(fields[1].fieldName, `is`("field3"))
        assertThat(fields[1].value, `is`("field3_value"))

        return@runBlocking
    }

}