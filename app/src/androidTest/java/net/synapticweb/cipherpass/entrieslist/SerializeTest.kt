/*
 * Copyright (C) 2021 Eugen Rădulescu <synapticwebb@gmail.com>
 * This file is part of the CipherPass project ( https://github.com/synapticweb/CipherPass ).
 * See the LICENSE file in the project root for license terms.
 */

package net.synapticweb.cipherpass.entrieslist

import android.net.Uri
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import junit.framework.Assert.assertNull
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import net.synapticweb.cipherpass.R
import net.synapticweb.cipherpass.TestCipherPassApp
import net.synapticweb.cipherpass.model.CustomField
import net.synapticweb.cipherpass.model.Entry
import net.synapticweb.cipherpass.util.CipherPassTestRule
import net.synapticweb.cipherpass.util.getOrAwaitValue
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import java.io.File

class SerializeTest {
    @get:Rule
    val testRule = CipherPassTestRule()

    @get:Rule
    var rule : TestRule = InstantTaskExecutorRule()
    private lateinit var viewModel : EntriesListViewModel

    private lateinit var app : TestCipherPassApp

    @Before
    fun setup() {
        testRule.setDb()
        app = ApplicationProvider.getApplicationContext()
        viewModel = EntriesListViewModel(testRule.repository, app)
        
        runBlocking { 
            val entry1 = Entry(entryName = "entry1")
            entry1.username = "entry1 username"
            entry1.password = "password1"
            entry1.url = "http://www.example.net"
            entry1.comment = "Comentariu"

            val entry1Id = testRule.repository.insertEntry(entry1)

            val field1 = CustomField(entry1Id, "field1", false, "field1_value")
            testRule.repository.insertCustomField(field1)

            val field2 = CustomField(entry1Id, "field2", true, "field2_value")
            testRule.repository.insertCustomField(field2)

            val field3 = CustomField(entry1Id, "field3", false)
            testRule.repository.insertCustomField(field3)

            val entry2 = Entry(entryName = "entry2")
            entry2.password = "password2"
            val entry2Id = testRule.repository.insertEntry(entry2)

            val field = CustomField(entry2Id, "field", false, "field_value")
            testRule.repository.insertCustomField(field)
        }
    }

    @After
    fun clean() {
        File(app.dataDir, "cipherpass-test.json").delete()
    }

    private fun checkDb(numEntries : Int) {
        runBlocking(Dispatchers.IO) {
            val entries = testRule.repository.getAllEntriesSync()

            assertThat(entries.size, `is`(numEntries))

            for(index in 0 until numEntries step 2) {
                assertThat(entries[index].entryName, `is`("entry1"))
                assertThat(entries[index].username, `is`("entry1 username"))
                assertThat(entries[index].password, `is`("password1"))
                assertThat(entries[index].url, `is`("http://www.example.net"))
                assertThat(entries[index].comment, `is`("Comentariu"))

                val fields1 = testRule.repository.getCustomFieldsSync(entries[index].id)

                assertThat(fields1.size, `is`(3))

                assertThat(fields1[0].fieldName, `is`("field1"))
                assertThat(fields1[0].isProtected, `is`(false))
                assertThat(fields1[0].value, `is`("field1_value"))

                assertThat(fields1[1].fieldName, `is`("field2"))
                assertThat(fields1[1].isProtected, `is`(true))
                assertThat(fields1[1].value, `is`("field2_value"))

                assertThat(fields1[2].fieldName, `is`("field3"))
                assertThat(fields1[2].isProtected, `is`(false))
                assertNull(fields1[2].value)

                assertThat(entries[index + 1].entryName, `is`("entry2"))
                assertNull(entries[index + 1].username)
                assertThat(entries[index + 1].password, `is`("password2"))
                assertNull(entries[index + 1].url)
                assertNull(entries[index + 1].comment)

                val fields2 = testRule.repository.getCustomFieldsSync(entries[index + 1].id)

                assertThat(fields2.size, `is`(1))
                assertThat(fields2[0].fieldName, `is`("field"))
                assertThat(fields2[0].isProtected, `is`(false))
                assertThat(fields2[0].value, `is`("field_value"))
            }
        }
    }

    @Test
    fun emptyDb_recreateDb() {
        val exportUri = Uri.withAppendedPath(Uri.fromFile(app.dataDir), "cipherpass-test.json")
        viewModel.exportJson(exportUri)

        val result = viewModel.serializeResults.getOrAwaitValue().getContentIfNotHandled()
        if(result == R.string.export_success) {
            runBlocking(Dispatchers.IO) {
                viewModel.emptyDb()
                assertThat(testRule.repository.dbContainsEntries(), `is`(false))
            }
            viewModel.readJsonData(exportUri)
            val importRes = viewModel.finishedImport.getOrAwaitValue().getContentIfNotHandled()
            importRes?.let {
                checkDb(2)
            }
        }
    }

    @Test
    fun dbWithEntries_replaceEntries_recreateDb() {
        val exportUri = Uri.withAppendedPath(Uri.fromFile(app.dataDir), "cipherpass-test.json")

        viewModel.exportJson(exportUri)
        val result = viewModel.serializeResults.getOrAwaitValue().getContentIfNotHandled()
        if(result == R.string.export_success)
            viewModel.readJsonData(exportUri)

        val hasEntries = viewModel.hasEntries.getOrAwaitValue().getContentIfNotHandled()
        hasEntries?.let {
            viewModel.importEntries(true)
        }

        val importRes = viewModel.finishedImport.getOrAwaitValue().getContentIfNotHandled()
        importRes?.let {
            checkDb(2)
        }
    }

    @Test
    fun dbWithEntries_doNotReplaceEntries_recreateDb() {
        val exportUri = Uri.withAppendedPath(Uri.fromFile(app.dataDir), "cipherpass-test.json")

        viewModel.exportJson(exportUri)
        val result = viewModel.serializeResults.getOrAwaitValue().getContentIfNotHandled()
        if(result == R.string.export_success)
            viewModel.readJsonData(exportUri)

        val hasEntries = viewModel.hasEntries.getOrAwaitValue().getContentIfNotHandled()
        hasEntries?.let {
            viewModel.importEntries(false)
        }

        val importRes = viewModel.finishedImport.getOrAwaitValue().getContentIfNotHandled()
        importRes?.let {
            checkDb(4)
        }
    }
}
