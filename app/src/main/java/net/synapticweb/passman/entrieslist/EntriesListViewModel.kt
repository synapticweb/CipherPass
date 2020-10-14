package net.synapticweb.passman.entrieslist

import android.app.Application
import androidx.lifecycle.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.synapticweb.passman.model.Repository
import net.synapticweb.passman.model.Entry
import net.synapticweb.passman.util.Event
import javax.inject.Inject


class EntriesListViewModel @Inject constructor(private val repository: Repository, application: Application) :
    AndroidViewModel(application) {

    //Dacă repository nu este inițializat getAllSecrets întoarce LiveData<null>, ceea ce îi permite observerului
    //din fragment să apeleze fragmentul de autentificare.
    private val _entries : LiveData<List<Entry>> = repository.getAllEntries()
    val entries : LiveData<List<Entry>> = _entries

    private val _openEntryEvent = MutableLiveData<Event<Long>>()
    val openEntryEvent : LiveData<Event<Long>> = _openEntryEvent

    private val _searchResults = MutableLiveData<Event<List<Entry>>>()
    val searchResults : MutableLiveData<Event<List<Entry>>> = _searchResults

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

}