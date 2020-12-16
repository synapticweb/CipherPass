package net.synapticweb.cipherpass.settings.di

import androidx.lifecycle.ViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import net.synapticweb.cipherpass.di.ViewModelKey
import net.synapticweb.cipherpass.settings.SystemLockViewModel

@Module
abstract class SystemLockModule {
    @Binds
    @IntoMap
    @ViewModelKey(SystemLockViewModel::class)
    abstract fun bindViewModel(viewModel : SystemLockViewModel) : ViewModel
}