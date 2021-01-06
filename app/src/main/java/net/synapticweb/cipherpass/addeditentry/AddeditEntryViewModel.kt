package net.synapticweb.cipherpass.addeditentry

import android.app.Application
import androidx.lifecycle.*
import kotlinx.coroutines.*
import net.synapticweb.cipherpass.*
import net.synapticweb.cipherpass.model.CustomField
import net.synapticweb.cipherpass.model.Entry
import net.synapticweb.cipherpass.model.Repository
import net.synapticweb.cipherpass.util.Event
import net.synapticweb.cipherpass.util.wrapEspressoIdlingResource
import javax.inject.Inject

class AddeditEntryViewModel @Inject constructor(private val repository: Repository,
                                                application: Application) :
    AndroidViewModel(application) {

    val username = MutableLiveData<String?>()
    val entryId = MutableLiveData<Long>()
    val customFields : LiveData<List<CustomField>> = entryId.switchMap {
        repository.getCustomFields(it)
    }

    val name = MutableLiveData<String>()
    val password = MutableLiveData<String?>()
    val rePassword = MutableLiveData<String?>()
    val url = MutableLiveData<String?>()
    val comment = MutableLiveData<String?>()
    val saveResult = MutableLiveData<Event<Int>>()
    val toastMessages = MutableLiveData<Event<Int>>()
    private lateinit var savedEntry : Entry
    val icon = MutableLiveData<Int>(R.drawable.item_key)
    private val newFields = arrayListOf<Long>()

    private fun isEdit() : Boolean {
        return entryId.value != 0L
    }

    fun populate(id : Long) {
        entryId.value = id
        if(id == 0L)
            return

        viewModelScope.launch {  //edit
            repository.getEntry(id). let { entry ->
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
                  password : String?,
                  url : String?,
                  comment : String?,
                  customFieldsData : Map<Long, String>
                  ) {

        val entry = if (isEdit()) savedEntry
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
            var rowId : Long = 0
            var entryError = false
            var customFieldsError = false

            if (isEdit()) {
                if(repository.updateEntry(entry) == -1)
                    entryError = true
            }
            else {
                rowId = repository.insertEntry(entry)
                if(rowId.toInt() == -1)
                    entryError = true
            }

            for(customField in customFieldsData) {
                val field = repository.getCustomField(customField.key)

                field.value = customField.value
                if(!isEdit())
                    field.entry = rowId
                if(repository.updateCustomField(field) != 1) {
                    customFieldsError = true
                    break
                }
            }

            if(entryError || customFieldsError) {
                toastMessages.value = Event(R.string.addedit_save_error)
                return@launch
            }

            if(isEdit()) {
                saveResult.value = Event(EDIT_SUCCESS)
                toastMessages.value = Event(R.string.addedit_save_ok)
            }
            else
                saveResult.value = Event(INSERT_SUCCES)
        }
    }

    fun setIcon(iconRes : Int) {
        icon.value = iconRes
    }

    fun createCustomField(fieldName : String, isProtected : Boolean) {
        val entry = if(isEdit())
            savedEntry.id
        else NO_ENTRY_CUSTOM_FIELD_ID
        val field = CustomField(entry, fieldName, isProtected)

        viewModelScope.launch {
            val rowId = repository.insertCustomField(field)
            if(rowId.toInt() == -1)
                toastMessages.value = Event(R.string.insert_cf_error)
            else
                newFields.add(rowId)
        }
    }

    fun cleanCustomFields() {
        wrapEspressoIdlingResource {
            viewModelScope.launch(Dispatchers.IO) {
                for (key in newFields) {
                    val field = repository.getCustomField(key)
                    repository.deleteCustomField(field)
                }
            }
        }
    }

    fun editCustomField(id: Long, fieldName: String, isProtected: Boolean) {
        viewModelScope.launch {
            val field = repository.getCustomField(id)
            field.fieldName = fieldName
            field.isProtected = isProtected
            repository.updateCustomField(field)
        }
    }

    fun deleteCustomField(id: Long) {
        viewModelScope.launch {
            newFields.remove(id)
            val field = repository.getCustomField(id)
            repository.deleteCustomField(field)
        }
    }
}