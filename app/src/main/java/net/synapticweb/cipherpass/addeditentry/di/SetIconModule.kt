package net.synapticweb.cipherpass.addeditentry.di

import androidx.lifecycle.ViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import net.synapticweb.cipherpass.addeditentry.SetIconViewModel
import net.synapticweb.cipherpass.di.ViewModelKey

@Module
abstract class SetIconModule {
    @Binds
    @IntoMap
    @ViewModelKey(SetIconViewModel::class)
    abstract fun bindViewModel(viewModel : SetIconViewModel) : ViewModel
}