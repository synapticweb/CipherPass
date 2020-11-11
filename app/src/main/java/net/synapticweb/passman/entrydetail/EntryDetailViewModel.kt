package net.synapticweb.passman.entrydetail

import android.app.Application
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import net.synapticweb.passman.CryptoPassApp
import net.synapticweb.passman.model.CustomField
import net.synapticweb.passman.model.Entry
import net.synapticweb.passman.model.Repository
import net.synapticweb.passman.util.Event
import javax.inject.Inject

class EntryDetailViewModel @Inject constructor(private val repository: Repository,
                                               application: Application
) : AndroidViewModel(application) {

    val entry = MutableLiveData<Entry>()
    val finishDeletion = MutableLiveData<Event<Boolean>>()
    val finishCopy = MutableLiveData<Event<String>>()
    lateinit var customFields : LiveData<List<CustomField>>
    val loadEnded = MutableLiveData<Event<Boolean>>()

    fun getEntry(entryId : Long) {
        viewModelScope.launch {
            entry.value = repository.getEntry(entryId)
            customFields = repository.getCustomFields(entryId)
            loadEnded.value = Event(true)
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

    fun copy(data : CharSequence, dataName : CharSequence) {
        val clipboard = getApplication<CryptoPassApp>().
                    getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(null, data)
        clipboard.setPrimaryClip(clip)
        finishCopy.value = Event(dataName.toString())
    }
}