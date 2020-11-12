package net.synapticweb.passman.entrydetail

import android.app.Application
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.lifecycle.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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
    val entryId = MutableLiveData<Long>()
    val customFields : LiveData<List<CustomField>> = entryId.switchMap {
        repository.getCustomFields(it)
    }

    fun load(id: Long) {
        entryId.value = id
        getEntry(id)
    }

    private fun getEntry(entryId : Long) {
        viewModelScope.launch {
            entry.value = repository.getEntry(entryId)
        }
    }

    fun deleteEntry() { //todo: error handling
        entry.value?.let {
            viewModelScope.launch(Dispatchers.IO) {
                val customFields = repository.getCustomFieldsSync(it.id)
                for(field in customFields)
                    repository.deleteCustomField(field)
                repository.deleteEntry(it)

                withContext(Dispatchers.Main) {
                    finishDeletion.value = Event(true)
                }
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