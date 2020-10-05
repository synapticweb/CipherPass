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

    fun checkDirty(name : String,
                   username : String?,
                   password : String,
                   url : String?,
                   comment : String?) : Boolean {
        return if (savedEntry.entryName != name || savedEntry.username != username ||
            savedEntry.password != password || savedEntry.url != url ||
            savedEntry.comment != comment
        ) true
        else
            return false
    }

    fun saveEntry(name : String,
                  username : String?,
                  password : String,
                  url : String?,
                  comment : String?,
                  entryId : Long) {
        var dirty = false

        val entry = if (entryId != 0L) savedEntry
        else
            Entry()
        if(entry.entryName != name) {
            entry.entryName = name
            dirty = true
        }
        if(entry.username != username) {
            entry.username = username
            dirty = true
        }

        if(entry.password != password) {
            entry.password = password
            dirty = true
        }

        if(entry.url != url) {
            entry.url = url
            dirty = true
        }

        if(entry.comment != comment) {
            entry.comment = comment
            dirty = true
        }

        if(dirty) {
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
        else
            result.value = Event(R.string.addedit_nochange)
    }
}