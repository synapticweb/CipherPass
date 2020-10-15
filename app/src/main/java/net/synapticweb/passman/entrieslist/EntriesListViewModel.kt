package net.synapticweb.passman.entrieslist

import android.app.Application
import androidx.lifecycle.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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
        repository.getAllEntries(getSortOrder())
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
        val elems = query.split("\\s+".toRegex())
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

    private fun getSortOrder() : SortOrder {
        val prefs = PrefWrapper.getInstance(getApplication())
        return when(prefs.getString(SORT_ORDER_KEY)) {
            "0" -> SortOrder.CREATION_DATE
            "1" -> SortOrder.NAME
            "2" -> SortOrder.MODIFICATION_DATE
            else -> SortOrder.CREATION_DATE
        }
    }

    fun loadEntries() {
        _refresh.value = true
    }

}