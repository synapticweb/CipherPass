/*
 * Copyright (C) 2021 Eugen Rădulescu <synapticwebb@gmail.com>
 * This file is part of the CipherPass project ( https://github.com/synapticweb/CipherPass ).
 * See the LICENSE file in the project root for license terms.
 */

package net.synapticweb.cipherpass.entrieslist

import android.annotation.SuppressLint
import android.app.Application
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import androidx.annotation.VisibleForTesting
import androidx.lifecycle.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import net.synapticweb.cipherpass.*
import net.synapticweb.cipherpass.model.Entry
import net.synapticweb.cipherpass.data.Repository
import net.synapticweb.cipherpass.util.Event
import net.synapticweb.cipherpass.util.PrefWrapper
import java.io.BufferedReader
import java.io.FileOutputStream
import java.io.InputStreamReader
import javax.inject.Inject

const val MAX_FILE_LENGTH = 10*1000*1024
const val SORT_ORDER_KEY = "sort_order_key"


class EntriesListViewModel @Inject constructor(
    private val repository: Repository,
    application: Application
) :
    AndroidViewModel(application) {

    private val _refresh = MutableLiveData(false)
    val unauthorized = MutableLiveData<Event<Boolean>>()

    private val _entries : LiveData<List<Entry>> = _refresh.switchMap {
        val prefs = PrefWrapper.getInstance(getApplication())
        val sortOrder = prefs.getString(SORT_ORDER_KEY) ?:
            getApplication<CipherPassApp>().resources.getString(R.string.sort_creation_desc_name)
        try {
            repository.getAllEntries(sortOrder)
        }
        catch (e : SecurityException) {
            Log.e(APP_TAG, "Accessing list of entries when database locked.")
            unauthorized.value = Event(true)
            MutableLiveData(listOf())
        }
    }

    val entries : LiveData<List<Entry>> = _entries

    private val _openEntryEvent = MutableLiveData<Event<Long>>()
    val openEntryEvent : LiveData<Event<Long>> = _openEntryEvent

    private val _searchResults = MutableLiveData<Event<List<Entry>>>()
    val searchResults : MutableLiveData<Event<List<Entry>>> = _searchResults

    private val _serializeResults = MutableLiveData<Event<Int>>()
    val serializeResults : LiveData<Event<Int>> = _serializeResults

    private val _hasEntries = MutableLiveData<Event<Boolean>>()
    val hasEntries : LiveData<Event<Boolean>> = _hasEntries

    private val _finishedImport = MutableLiveData<Event<Boolean>>()
    val finishedImport : LiveData<Event<Boolean>> = _finishedImport

    lateinit var jsonData : List<Entry>

    init {
        loadEntries()
    }

    fun openEntry(entryId: Long) {
        _openEntryEvent.value = Event(entryId)
    }

    fun search(query: String) {
        if(query.length < 3) {
            _searchResults.value = Event(arrayListOf())
            return
        }
        //Separatorul este format din unul sau mai multe spații sau 0 sau mai multe caractere
        //nonalfanumerice urmate de 1 sau mai multe spații: dacă scrie term1, term2
        //să nu caute după "term1,".
        val elems = query.split("[^a-zA-Z0-9]*\\s+".toRegex())
        val finalElems = mutableListOf<String>()
        for(elem in elems)
            if(elem.isNotBlank())
                finalElems.add(elem)

        try {
            viewModelScope.launch(Dispatchers.IO) {
                repository.queryDb(finalElems).let {
                    withContext(Dispatchers.Main) {
                        _searchResults.value = Event(it)
                    }
                }
            }
        }
        catch (e : SecurityException) {
            Log.d(APP_TAG, "Manual search query while db is locked. ")
            unauthorized.value = Event(true)
        }
    }

    fun loadEntries() {
        _refresh.value = true
    }

    fun exportJson(fileUri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            var entries : List<Entry> = listOf()
            try {
                entries = repository.getAllEntriesSync()
                for (entry in entries)
                    entry.customFields = repository.getCustomFieldsSync(entry.id)
            }
            catch (e : SecurityException) {
                withContext(Dispatchers.Main) {
                    Log.e(APP_TAG, "Export json while db is locked. ")
                    unauthorized.value = Event(true)
                }
            }

            val contentResolver = getApplication<CipherPassApp>().contentResolver
            try {
                contentResolver.openFileDescriptor(fileUri, "w")?. use { it ->
                    FileOutputStream(it.fileDescriptor).use { outpuStream ->
                        outpuStream.write(Json.encodeToString(entries).toByteArray())
                    }
                }
            }
            catch (e: Exception) {
                Log.e(APP_TAG, e.message.toString())
                withContext(Dispatchers.Main) {
                    _serializeResults.value = Event(R.string.export_error)
                }
                return@launch
            }

            withContext(Dispatchers.Main) {
                _serializeResults.value = Event(R.string.export_success)
            }
        }
    }

    @SuppressLint("Recycle")
    fun readJsonData(fileUri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val contentResolver = getApplication<CipherPassApp>().contentResolver
                val sb = StringBuilder()

               contentResolver.query(fileUri, arrayOf(OpenableColumns.SIZE),
                   null, null, null)?.let {
                   if(it.moveToNext()) {
                       val size = it.getString(it.getColumnIndex(OpenableColumns.SIZE)).toLong()
                       if (size > MAX_FILE_LENGTH) {
                           withContext(Dispatchers.Main) {
                               _serializeResults.value = Event(R.string.import_error)
                           }
                           it.close()
                           return@launch
                       }
                   }
                   it.close()
               }

                contentResolver.openInputStream(fileUri)?.use { inputStream ->
                    BufferedReader(InputStreamReader(inputStream)).use { reader ->
                        val buffer = CharArray(1024)
                        var charsRead: Int
                        while (reader.read(buffer, 0, buffer.size).also { charsRead = it } > 0) {
                            sb.append(buffer, 0, charsRead)
                        }
                    }
                }

                jsonData = Json.decodeFromString(sb.toString())

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _serializeResults.value = Event(R.string.import_error)
                }
                return@launch
            }

            try {
                if (repository.dbContainsEntries()) {
                    withContext(Dispatchers.Main) {
                        _hasEntries.value = Event(true)
                    }
                    return@launch
                }
            }
            catch (e : SecurityException) {
                withContext(Dispatchers.Main) {
                    Log.d(APP_TAG, "Read json data while db is locked. ")
                    unauthorized.value = Event(true)
                }
            }

            importEntries(false)
        }
    }

    fun importEntries(replace : Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            if(replace)
                emptyDb()

            try {
                for (entry in jsonData) {
                    val entryId = repository.insertEntry(entry)
                    entry.customFields?.let {
                        for (field in it) {
                            field.entry = entryId
                            repository.insertCustomField(field)
                        }
                    }
                }
            }
            catch (e : SecurityException) {
                withContext(Dispatchers.Main) {
                    Log.d(APP_TAG, "Import entries while db is locked. ")
                    unauthorized.value = Event(true)
                }
            }
            withContext(Dispatchers.Main) {
                _finishedImport.value = Event(true)
            }
        }
    }

    @VisibleForTesting
    suspend fun emptyDb() {
        try {
            val entries = repository.getAllEntriesSync()
            for (entry in entries) {
                val fields = repository.getCustomFieldsSync(entry.id)
                for (field in fields)
                    repository.deleteCustomField(field)
                repository.deleteEntry(entry)
            }
        }
        catch (e : SecurityException) {
            withContext(Dispatchers.Main) {
                Log.d(APP_TAG, "Empty dp while db is locked. ")
                unauthorized.value = Event(true)
            }
        }
    }

    fun getIconRes(iconName : String) : Int {
        val context = getApplication<CipherPassApp>()
        return context.resources.getIdentifier(iconName, "drawable", context.packageName)
    }

    fun lockAndReauth() {
        repository.lock()
        unauthorized.value = Event(true)
    }
}