package net.synapticweb.cipherpass.settings.di

import androidx.lifecycle.ViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import net.synapticweb.cipherpass.di.ViewModelKey
import net.synapticweb.cipherpass.settings.SettingsViewModel

@Module
abstract class SettingsModule {
    @Binds
    @IntoMap
    @ViewModelKey(SettingsViewModel::class)
    abstract fun bindViewModel(viewModel : SettingsViewModel) : ViewModel
}