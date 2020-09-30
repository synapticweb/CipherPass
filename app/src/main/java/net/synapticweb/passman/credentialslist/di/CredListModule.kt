package net.synapticweb.passman.credentialslist.di

import androidx.lifecycle.ViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import net.synapticweb.passman.di.ViewModelKey
import net.synapticweb.passman.credentialslist.CredListViewModel

//https://blog.kotlin-academy.com/understanding-dagger-2-multibindings-viewmodel-8418eb372848
@Module
abstract class CredListModule {
   @Binds
   @IntoMap
   @ViewModelKey(CredListViewModel::class)
   abstract fun bindViewModel(viewModel : CredListViewModel) : ViewModel
}