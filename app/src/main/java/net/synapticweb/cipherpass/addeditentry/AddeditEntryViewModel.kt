package net.synapticweb.cipherpass.addeditentry

import android.app.Application
import androidx.lifecycle.*
import kotlinx.coroutines.*
import net.synapticweb.cipherpass.*
import net.synapticweb.cipherpass.model.CustomField
import net.synapticweb.cipherpass.model.Entry
import net.synapticweb.cipherpass.model.KEY_DRAWABLE_NAME
import net.synapticweb.cipherpass.model.Repository
import net.synapticweb.cipherpass.util.Event
import net.synapticweb.cipherpass.util.wrapEspressoIdlingResource
import javax.inject.Inject

const val NEW_FIELD = "new_field"
const val DIRTY_FIELD = "dirty_field"
const val INSERT_SUCCES = 0
const val EDIT_SUCCESS = 1
const val NO_ENTRY_CUSTOM_FIELD_ID = 0L

class AddeditEntryViewModel @Inject constructor(private val repository: Repository,
                                                application: Application) :
    AndroidViewModel(application) {

    val username = MutableLiveData<String?>()

    private var inMemoryFields = mutableListOf<CustomField>()
    val customFields = MutableLiveData<MutableList<CustomField>>()

    private val deletedFields = arrayListOf<CustomField>()

    val name = MutableLiveData<String>()
    val password = MutableLiveData<String?>()
    val rePassword = MutableLiveData<String?>()
    val url = MutableLiveData<String?>()
    val comment = MutableLiveData<String?>()
    val saveResult = MutableLiveData<Event<Int>>()
    val toastMessages = MutableLiveData<Event<Int>>()
    private lateinit var savedEntry : Entry
    private val icon = MutableLiveData(KEY_DRAWABLE_NAME)
    val iconRes : LiveData<Int> = icon.map {
        val context = getApplication<CipherPassApp>()
        context.resources.getIdentifier(it, "drawable", context.packageName)
    }

    private fun isEdit() : Boolean {
        return ::savedEntry.isInitialized
    }

    fun populate(id : Long) {
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
            withContext(Dispatchers.IO) {
                inMemoryFields = repository.getCustomFieldsSync(id).toMutableList()
            }
            customFields.value = inMemoryFields
        }
    }

    fun saveEntry(name : String,
                  username : String?,
                  password : String?,
                  url : String?,
                  comment : String?
                  ) {

        val entry = if (isEdit()) savedEntry
        else
            Entry(entryName = name)

        entry.entryName = name
        entry.username = username
        entry.password = password
        entry.url = url
        entry.comment = comment
        entry.modificationDate = System.currentTimeMillis()
        entry.icon = icon.value!!

        wrapEspressoIdlingResource {
            viewModelScope.launch {
                var newEntryRowId = 0L

                if (isEdit()) {
                    if (repository.updateEntry(entry) == -1) {
                        toastMessages.value = Event(R.string.addedit_save_error)
                        return@launch
                    }
                } else {
                    newEntryRowId = repository.insertEntry(entry)
                    if (newEntryRowId.toInt() == -1) {
                        toastMessages.value = Event(R.string.addedit_save_error)
                        return@launch
                    }
                }

                for (field in inMemoryFields) {
                    if (field.inMemoryState == NEW_FIELD) {
                        field.entry = if (isEdit()) entry.id else newEntryRowId

                        val cfRowId = repository.insertCustomField(field)
                        if (cfRowId.toInt() == -1) {
                            toastMessages.value = Event(R.string.addedit_save_error)
                            return@launch
                        }
                    } else if (field.inMemoryState == DIRTY_FIELD) {
                        val rowsUpdated = repository.updateCustomField(field)
                        if (rowsUpdated != 1) {
                            toastMessages.value = Event(R.string.addedit_save_error)
                            return@launch
                        }
                    }
                }

                for (field in deletedFields) {
                    if (repository.deleteCustomField(field) != 1) {
                        toastMessages.value = Event(R.string.addedit_save_error)
                        return@launch
                    }
                }

                if (isEdit()) {
                    saveResult.value = Event(EDIT_SUCCESS)
                    toastMessages.value = Event(R.string.addedit_save_ok)
                } else
                    saveResult.value = Event(INSERT_SUCCES)
            }
        }
    }

    fun setIcon(icon : String) {
        this.icon.value = icon
    }

    fun addCustomField(fieldName : String, isProtected : Boolean) {
        val entry = if(isEdit())
            savedEntry.id
        else NO_ENTRY_CUSTOM_FIELD_ID

        val field = CustomField(entry, fieldName, isProtected)
        field.inMemoryState = NEW_FIELD
        inMemoryFields.add(field)
        customFields.value = inMemoryFields
        //dacă live data nu provine de la db, faptul că în lista atașată se operează modificare nu
        //produce notificarea observerului. Trebuie să se schimbe lista cu totul:
        //https://stackoverflow.com/questions/47941537/notify-observer-when-item-is-added-to-list-of-livedata
    }

    fun editCustomField(position: Int, fieldName: String, isProtected: Boolean) {
       val field = inMemoryFields[position]
        field.fieldName = fieldName
        field.isProtected = isProtected
        if(field.inMemoryState != NEW_FIELD)
            field.inMemoryState = DIRTY_FIELD
        customFields.value = inMemoryFields
    }

    fun deleteCustomField(position: Int) {
        val field = inMemoryFields[position]
        if(field.inMemoryState != NEW_FIELD)
            deletedFields.add(field)
        inMemoryFields.remove(field)
        customFields.value = inMemoryFields
    }

    fun saveCustomField(position : Int, value : String) {
        val field = inMemoryFields[position]
        field.value = if(value.isBlank()) null else value
        if(field.inMemoryState != NEW_FIELD)
            field.inMemoryState = DIRTY_FIELD
    }
}