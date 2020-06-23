package net.synapticweb.passman.settings.di

import androidx.lifecycle.ViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import net.synapticweb.passman.di.ViewModelKey
import net.synapticweb.passman.settings.SettingsViewModel
import net.synapticweb.passman.settings.SystemLockViewModel

@Module
abstract class SystemLockModule {
    @Binds
    @IntoMap
    @ViewModelKey(SystemLockViewModel::class)
    abstract fun bindViewModel(viewModel : SystemLockViewModel) : ViewModel
}