package net.synapticweb.passman.addeditentry

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import net.synapticweb.passman.INSERT_SUCCES
import net.synapticweb.passman.R
import net.synapticweb.passman.model.Entry
import net.synapticweb.passman.model.Repository
import net.synapticweb.passman.util.Event
import javax.inject.Inject

class AddeditEntryViewModel @Inject constructor(private val repository: Repository,
                                                application: Application) :
    AndroidViewModel(application) {

    val username = MutableLiveData<String?>()
    val name = MutableLiveData<String>()
    val password = MutableLiveData<String>()
    val rePassword = MutableLiveData<String>()
    val url = MutableLiveData<String?>()
    val comment = MutableLiveData<String?>()
    val result = MutableLiveData<Event<Int>>()
    private lateinit var savedEntry : Entry
    val icon = MutableLiveData<Int>(R.drawable.item_key)

    fun populate(entryId : Long) {
        viewModelScope.launch {
            repository.getEntry(entryId). let { entry ->
                name.value = entry.entryName
                password.value = entry.password
                rePassword.value = entry.password
                username.value = entry.username
                url.value = entry.url
                comment.value = entry.comment
                icon.value = entry.icon
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
        entry.icon = icon.value!!

        viewModelScope.launch {
            if (entryId != 0L) {
                val numRows = repository.updateEntry(entry)
                result.value = if(numRows == 1)
                    Event(R.string.addedit_save_ok)
                else
                    Event(R.string.addedit_save_error)
            }
            else {
                val rowId = repository.insertEntry(entry)
                result.value = if(rowId.toInt() == -1)
                    Event(R.string.addedit_save_error)
                else
                    Event(INSERT_SUCCES)
            }
        }
    }

    fun setIcon(iconRes : Int) {
        icon.value = iconRes
    }
}