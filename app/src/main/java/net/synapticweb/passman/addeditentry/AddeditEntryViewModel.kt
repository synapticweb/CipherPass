package net.synapticweb.passman.addeditentry

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import net.synapticweb.passman.R
import net.synapticweb.passman.model.Entry
import net.synapticweb.passman.model.Repository
import net.synapticweb.passman.util.Event
import javax.inject.Inject

class AddeditEntryViewModel @Inject constructor(private val repository: Repository,
                                                application: Application) :
    AndroidViewModel(application) {

    val name = MutableLiveData<String>()
    val username = MutableLiveData<String>()
    val password = MutableLiveData<String>()
    val url = MutableLiveData<String>()
    val comment = MutableLiveData<String>()
    val result = MutableLiveData<Event<Int>>()
    lateinit var savedEntry : Entry

    fun populate(entryId : Long) {
        viewModelScope.launch {
            repository.getEntry(entryId). let { entry ->
                name.value = entry.entryName
                password.value = entry.password
                username.value = entry.username
                url.value = entry.url
                comment.value = entry.comment
                savedEntry = entry
            }
        }
    }

    fun saveEntry(name : String,
                  username : String?,
                  password : String,
                  url : String?,
                  comment : String?,
                  entryId : Long) {

        val entry = if (entryId != 0L) savedEntry
        else
            Entry()

        entry.entryName = name
        entry.username = username
        entry.password = password
        entry.url = url
        entry.comment = comment
        entry.modificationDate = System.currentTimeMillis()

        viewModelScope.launch {
            if (entryId != 0L) {
                repository.updateEntry(entry)
                result.value = Event(R.string.addedit_save_ok)
            }
            else {
                repository.insertEntry(entry)
                result.value = Event(0)
            }
        }

    }
}