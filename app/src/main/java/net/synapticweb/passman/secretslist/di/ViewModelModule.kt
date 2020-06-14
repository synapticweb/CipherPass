package net.synapticweb.passman.secretslist.di

import android.app.Application
import dagger.Module
import dagger.Provides
import net.synapticweb.passman.model.Repository
import net.synapticweb.passman.secretslist.SecretsListViewModel
import net.synapticweb.passman.secretslist.SecretsListViewModelFactory


@Module
class ViewModelModule {
    @Provides
    fun provideViewModel(repository: Repository, application: Application) : SecretsListViewModel {
           return SecretsListViewModelFactory(repository, application).
                create(SecretsListViewModel::class.java)
    }
}