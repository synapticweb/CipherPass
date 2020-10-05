package net.synapticweb.passman.addeditentry.di

import androidx.lifecycle.ViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import net.synapticweb.passman.addeditentry.AddeditEntryViewModel
import net.synapticweb.passman.di.ViewModelKey

@Module
abstract class AddeditEntryModule {
    @Binds
    @IntoMap
    @ViewModelKey(AddeditEntryViewModel::class)
    abstract fun bindViewModel(viewModel : AddeditEntryViewModel) : ViewModel
}