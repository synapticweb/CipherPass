package net.synapticweb.passman.secretslist.di

import androidx.lifecycle.ViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import net.synapticweb.passman.di.ViewModelKey
import net.synapticweb.passman.secretslist.SecretsListViewModel

//https://blog.kotlin-academy.com/understanding-dagger-2-multibindings-viewmodel-8418eb372848
@Module
abstract class SecretsListModule {
   @Binds
   @IntoMap
   @ViewModelKey(SecretsListViewModel::class)
   abstract fun bindViewModel(viewModel : SecretsListViewModel) : ViewModel
}