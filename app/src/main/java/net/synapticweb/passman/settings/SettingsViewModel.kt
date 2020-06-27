package net.synapticweb.passman.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import net.synapticweb.passman.model.Repository
import javax.inject.Inject

class SettingsViewModel @Inject constructor(private val repository: Repository, application: Application) :
    AndroidViewModel(application)