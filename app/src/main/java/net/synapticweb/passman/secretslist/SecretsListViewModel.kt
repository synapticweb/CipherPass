package net.synapticweb.passman.secretslist

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import net.synapticweb.passman.di.FragmentScope
import net.synapticweb.passman.model.Repository
import javax.inject.Inject

@FragmentScope
class SecretsListViewModel @Inject constructor(val repository: Repository, application: Application) :
    AndroidViewModel(application) {
}

@Suppress("UNCHECKED_CAST")
class SecretsListViewModelFactory(private val repository: Repository,
    private val application: Application) : ViewModelProvider.AndroidViewModelFactory(application)
    {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T  =
            (SecretsListViewModel(repository, application) as T)
    }