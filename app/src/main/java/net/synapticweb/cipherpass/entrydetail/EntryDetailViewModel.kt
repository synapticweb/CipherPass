/*
 * Copyright (C) 2021 Eugen Rădulescu <synapticwebb@gmail.com>
 * This file is part of the CipherPass project ( https://github.com/synapticweb/CipherPass ).
 * See the LICENSE file in the project root for license terms.
 */

package net.synapticweb.cipherpass.entrydetail

import android.app.Application
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.lifecycle.*
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.synapticweb.cipherpass.CipherPassApp
import net.synapticweb.cipherpass.R
import net.synapticweb.cipherpass.model.CustomField
import net.synapticweb.cipherpass.model.Entry
import net.synapticweb.cipherpass.data.Repository
import net.synapticweb.cipherpass.util.Event
import net.synapticweb.cipherpass.util.PrefWrapper
import java.util.concurrent.TimeUnit
import javax.inject.Inject

const val CLIPBOARD_LABEL_KEY = "net.synapticweb.cipherpass.clipboard_key"

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
        val res = getApplication<CipherPassApp>().resources
        val clipboard = getApplication<CipherPassApp>().
                    getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(CLIPBOARD_LABEL_KEY, data)
        clipboard.setPrimaryClip(clip)
        val prefWrapper = PrefWrapper.getInstance(getApplication())
        val clipboardTimeout = prefWrapper.getString(res.getString(R.string.clipboard_timeout_key))

        clipboardTimeout?. let {
            if(it != res.getString(R.string.clipboard_timeout_disabled_value)) {
                val cleanClipboard = OneTimeWorkRequestBuilder<CleanClipboard>()
                    .setInitialDelay(it.toLong(), TimeUnit.SECONDS)
                    .build()
                WorkManager.getInstance(getApplication()).enqueue(cleanClipboard)
            }
        }

        finishCopy.value = Event(dataName.toString())
    }
}