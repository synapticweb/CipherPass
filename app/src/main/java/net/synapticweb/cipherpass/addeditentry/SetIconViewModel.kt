package net.synapticweb.cipherpass.addeditentry

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import javax.inject.Inject

class SetIconViewModel @Inject constructor(application: Application) :
    AndroidViewModel(application) {
    val icon = MutableLiveData<String>()

    fun setIcon(iconName : String) {
        icon.value = iconName
    }
}