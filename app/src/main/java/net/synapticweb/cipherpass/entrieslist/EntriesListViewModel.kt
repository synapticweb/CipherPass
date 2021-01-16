package net.synapticweb.cipherpass.entrieslist

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.lifecycle.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import net.synapticweb.cipherpass.*
import net.synapticweb.cipherpass.model.Repository
import net.synapticweb.cipherpass.model.Entry
import net.synapticweb.cipherpass.util.Event
import net.synapticweb.cipherpass.util.PrefWrapper
import java.io.FileOutputStream
import javax.inject.Inject


class EntriesListViewModel @Inject constructor(private val repository: Repository, application: Application) :
    AndroidViewModel(application) {

    private val _refresh = MutableLiveData(false)

    private val _entries : LiveData<List<Entry>> = _refresh.switchMap {
        val prefs = PrefWrapper.getInstance(getApplication())
        val sortOrder = prefs.getString(SORT_ORDER_KEY) ?: SORT_CREATION_DESC
        repository.getAllEntries(sortOrder)
    }

    val entries : LiveData<List<Entry>> = _entries

    private val _openEntryEvent = MutableLiveData<Event<Long>>()
    val openEntryEvent : LiveData<Event<Long>> = _openEntryEvent

    private val _searchResults = MutableLiveData<Event<List<Entry>>>()
    val searchResults : MutableLiveData<Event<List<Entry>>> = _searchResults

    private val _exportResult = MutableLiveData<Event<Int>>()
    val exportResult : LiveData<Event<Int>> = _exportResult

    init {
        loadEntries()
    }

    fun openEntry(entryId : Long) {
        _openEntryEvent.value = Event(entryId)
    }

    fun search(query : String) {
        if(query.length < 3) {
            _searchResults.value = Event(arrayListOf())
            return
        }
        //unul sau mai multe spații sau 0 sau mai multe caractere nonalfanumerice urmate
        //de 1 sau mai multe spații: dacă scrie term1, term2 să nu caute după term1, .
        val elems = query.split("[^a-zA-Z0-9]*\\s+".toRegex())
        val finalElems = mutableListOf<String>()
        for(elem in elems)
            if(elem.isNotBlank())
                finalElems.add(elem)

        viewModelScope.launch(Dispatchers.IO) {
            repository.queryDb(finalElems). let {
                    withContext(Dispatchers.Main) {
                        _searchResults.value = Event(it)
                    }
            }
        }
    }

    fun loadEntries() {
        _refresh.value = true
    }

    fun exportJson(fileUri : Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            val entries = repository.getAllEntriesSync()
            for(entry in entries)
                entry.customFields = repository.getCustomFieldsSync(entry.id)

            val contentResolver = getApplication<CipherPassApp>().contentResolver
            try {
                contentResolver.openFileDescriptor(fileUri, "w")?. use { it ->
                    FileOutputStream(it.fileDescriptor).use { outpuStream ->
                        outpuStream.write(Json.encodeToString(entries).toByteArray())
                    }
                }
            }
            catch (e : Exception) {
                Log.e(APP_TAG, e.message.toString())
                withContext(Dispatchers.Main) {
                    _exportResult.value = Event(R.string.serialize_error)
                }
                return@launch
            }

            withContext(Dispatchers.Main) {
                _exportResult.value = Event(R.string.serialize_success)
            }
        }
    }
}