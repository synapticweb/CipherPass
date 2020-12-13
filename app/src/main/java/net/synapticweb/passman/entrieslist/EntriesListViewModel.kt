package net.synapticweb.passman.entrieslist

import android.app.Application
import androidx.lifecycle.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.synapticweb.passman.SORT_CREATION_DESC
import net.synapticweb.passman.SORT_ORDER_KEY
import net.synapticweb.passman.model.Repository
import net.synapticweb.passman.model.Entry
import net.synapticweb.passman.model.SortOrder
import net.synapticweb.passman.util.Event
import net.synapticweb.passman.util.PrefWrapper
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
}