package net.synapticweb.cipherpass.entrieslist

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.runBlocking
import net.synapticweb.cipherpass.TestCipherPassApp
import net.synapticweb.cipherpass.model.Entry
import net.synapticweb.cipherpass.util.CipherPassTestRule
import net.synapticweb.cipherpass.util.getOrAwaitValue
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule

class EntriesListViewModelTest {
    @get:Rule
    val testRule = CipherPassTestRule()
    //fără asta la instanțierea viewModelului îmi dă că nu poate să seteze _entries.value de pe un thread
    //background. Problema ar fi că în lipsa UI-ului codul rulează pe un thread random: https://jeroenmols.com/blog/2019/01/17/livedatajunit5/
    @get:Rule
    var rule : TestRule = InstantTaskExecutorRule()
    private lateinit var viewModel : EntriesListViewModel

    @Before
    fun setup() {
        testRule.setDb()
        val app = ApplicationProvider.getApplicationContext<TestCipherPassApp>()
        viewModel = EntriesListViewModel(testRule.repository, app)
        runBlocking {
            val item = Entry()
            item.entryName = "first entry"
            item.username = "first username"
            item.url = "http://www.example.net"
            item.comment = "Comentariu"
            testRule.repository.insertEntry(item)

            item.entryName = "second item"
            item.username = "second username"
            item.url = "http://www.secondurl.net"
            item.comment = "bla bla"
            testRule.repository.insertEntry(item)

            item.entryName = "third item"
            item.username = "third username"
            item.url = "http://www.thirdurl.net"
            item.comment = "Al treilea comentariu"
            testRule.repository.insertEntry(item)
        }
    }

    @Test
    fun search_single_word_one_record() {
        viewModel.search("first")
        val result = viewModel.searchResults.getOrAwaitValue().getContentIfNotHandled()
        if (result != null) {
            assertThat(result.size, `is`(1))
            assertThat(result[0].entryName, `is`("first entry"))
        }
    }

    @Test
    fun search_wordStem_two_records() {
        viewModel.search("com")
        val result = viewModel.searchResults.getOrAwaitValue().getContentIfNotHandled()
        if (result != null) {
            assertThat(result.size, `is`(2))
            assertThat(result[0].entryName, `is`("first entry"))
            assertThat(result[1].entryName, `is`("third item"))
        }
    }

    @Test
    fun search_middleWord_noResults() {
        viewModel.search("tariu")
        val result = viewModel.searchResults.getOrAwaitValue().getContentIfNotHandled()
        if (result != null) {
            assertThat(result.size, `is`(0))
        }
    }

    @Test
    fun search_2terms_2records() {
        viewModel.search("first second")
        val result = viewModel.searchResults.getOrAwaitValue().getContentIfNotHandled()
        if (result != null) {
            assertThat(result.size, `is`(2))
            assertThat(result[0].entryName, `is`("first entry"))
            assertThat(result[1].entryName, `is`("second item"))
        }
    }

    @Test
    fun search_2terms_commaSeparated_2records() {
        viewModel.search("first, second")
        val result = viewModel.searchResults.getOrAwaitValue().getContentIfNotHandled()
        if (result != null) {
            assertThat(result.size, `is`(2))
            assertThat(result[0].entryName, `is`("first entry"))
            assertThat(result[1].entryName, `is`("second item"))
        }
    }

    @Test
    fun search_2terms_secondNonexistent_findsFirst() {
        viewModel.search("third vasile")
        val result = viewModel.searchResults.getOrAwaitValue().getContentIfNotHandled()
        if (result != null) {
            assertThat(result.size, `is`(1))
            assertThat(result[0].entryName, `is`("third item"))
        }
    }

    @Test
    fun search_2terms_formPhrase() {
        viewModel.search("first entry")
        val result = viewModel.searchResults.getOrAwaitValue().getContentIfNotHandled()
        if (result != null) {
            assertThat(result.size, `is`(1))
            assertThat(result[0].entryName, `is`("first entry"))
        }
    }
}