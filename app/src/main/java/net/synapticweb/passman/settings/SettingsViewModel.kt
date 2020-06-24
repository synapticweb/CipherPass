package net.synapticweb.passman.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import net.synapticweb.passman.byteArrayToHexStr
import net.synapticweb.passman.createHash
import net.synapticweb.passman.hexStrToByteArray
import net.synapticweb.passman.model.Hash
import net.synapticweb.passman.model.Repository
import javax.inject.Inject

class SettingsViewModel @Inject constructor(private val repository: Repository, application: Application) :
    AndroidViewModel(application)