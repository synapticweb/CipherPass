package net.synapticweb.cipherpass.entrieslist.di

import androidx.lifecycle.ViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import net.synapticweb.cipherpass.di.ViewModelKey
import net.synapticweb.cipherpass.entrieslist.EntriesListViewModel

//https://blog.kotlin-academy.com/understanding-dagger-2-multibindings-viewmodel-8418eb372848
@Module
abstract class EntriesListModule {
   @Binds
   @IntoMap
   @ViewModelKey(EntriesListViewModel::class)
   abstract fun bindViewModel(viewModel : EntriesListViewModel) : ViewModel
}