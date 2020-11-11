package net.synapticweb.passman.addeditentry

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import net.synapticweb.passman.EDIT_SUCCESS
import net.synapticweb.passman.INSERT_SUCCES
import net.synapticweb.passman.NO_ENTRY_CUSTOM_FIELD_ID
import net.synapticweb.passman.R
import net.synapticweb.passman.model.CustomField
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
    val saveResult = MutableLiveData<Event<Int>>()
    val toastMessages = MutableLiveData<Event<Int>>()
    private lateinit var savedEntry : Entry
    val icon = MutableLiveData<Int>(R.drawable.item_key)
    lateinit var customFields : LiveData<List<CustomField>>
    val loadEnded = MutableLiveData<Event<Boolean>>()

    fun populate(entryId : Long) {
        viewModelScope.launch {
            customFields = repository.getCustomFields(entryId)
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
        loadEnded.value = Event(true)
    }

    fun saveEntry(name : String,
                  username : String?,
                  password : String,
                  url : String?,
                  comment : String?,
                  customFieldsData : Map<Long, String>
                  ) {

        val entry = if (::savedEntry.isInitialized) savedEntry
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

            if (::savedEntry.isInitialized) {
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
                if(!::savedEntry.isInitialized)
                    field.entry = rowId
                if(repository.updateCustomField(field) != 1)
                    customFieldsError = true
            }

            if(entryError || customFieldsError) {
                toastMessages.value = Event(R.string.addedit_save_error)
                return@launch
            }

            if(::savedEntry.isInitialized) {
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

    fun createCustomField(fieldName : String) {
        val entry = if(::savedEntry.isInitialized)
            savedEntry.id
        else NO_ENTRY_CUSTOM_FIELD_ID
        val field = CustomField(entry, fieldName)

        viewModelScope.launch {
         val rowId = repository.insertCustomField(field)
            if(rowId.toInt() == -1)
                toastMessages.value = Event(R.string.insert_cf_error)
        }
    }

    fun initCustomFields() {
        if(!::customFields.isInitialized) {
            customFields = repository.getCustomFields(NO_ENTRY_CUSTOM_FIELD_ID)
            loadEnded.value = Event(true)
        }
    }

    fun deleteCustomField(field : CustomField) {
        viewModelScope.launch {
            repository.deleteCustomField(field)
        }
    }
}