package net.synapticweb.passman.addeditentry.di

import androidx.lifecycle.ViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import net.synapticweb.passman.addeditentry.SetIconViewModel
import net.synapticweb.passman.di.ViewModelKey

@Module
abstract class SetIconModule {
    @Binds
    @IntoMap
    @ViewModelKey(SetIconViewModel::class)
    abstract fun bindViewModel(viewModel : SetIconViewModel) : ViewModel
}