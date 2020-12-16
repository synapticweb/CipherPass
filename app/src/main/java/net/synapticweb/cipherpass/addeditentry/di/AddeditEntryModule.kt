package net.synapticweb.cipherpass.addeditentry.di

import androidx.lifecycle.ViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import net.synapticweb.cipherpass.addeditentry.AddeditEntryViewModel
import net.synapticweb.cipherpass.di.ViewModelKey

@Module
abstract class AddeditEntryModule {
    @Binds
    @IntoMap
    @ViewModelKey(AddeditEntryViewModel::class)
    abstract fun bindViewModel(viewModel : AddeditEntryViewModel) : ViewModel
}