package net.synapticweb.passman.entrieslist

import android.app.Application
import androidx.lifecycle.*
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

    private val _openEntryEvent = MutableLiveData<Event<Pair<Long, String>>>()
    val openEntryEvent : LiveData<Event<Pair<Long, String>>> = _openEntryEvent

    fun openEntry(entryId : Long, title : String) {
        _openEntryEvent.value = Event(Pair(entryId, title))
    }

}