package net.synapticweb.passman.entrydetail

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import net.synapticweb.passman.model.Entry
import net.synapticweb.passman.model.Repository
import net.synapticweb.passman.util.Event
import javax.inject.Inject

class EntryDetailViewModel @Inject constructor(private val repository: Repository,
                                               application: Application
) : AndroidViewModel(application) {

    val entry = MutableLiveData<Entry>()
    val finishDeletion = MutableLiveData<Event<Boolean>>()

    fun getEntry(entryId : Long) {
        viewModelScope.launch {
            entry.value = repository.getEntry(entryId)
        }
    }

    fun deleteEntry() {
        entry.value?.let {
            viewModelScope.launch {
                repository.deleteEntry(it)
                finishDeletion.value = Event(true)
            }
        }
    }
}