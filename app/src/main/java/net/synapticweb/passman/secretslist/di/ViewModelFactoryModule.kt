package net.synapticweb.passman.secretslist.di

import androidx.lifecycle.ViewModelProvider
import dagger.Module
import dagger.Provides
import net.synapticweb.passman.model.Repository
import net.synapticweb.passman.secretslist.SecretsListViewModelFactory

@Module
class ViewModelFactoryModule {
    @Provides
    fun provideViewModelFactory(repository: Repository) : ViewModelProvider.Factory {
        return SecretsListViewModelFactory(repository)
    }
}