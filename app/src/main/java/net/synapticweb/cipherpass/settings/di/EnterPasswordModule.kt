package net.synapticweb.cipherpass.settings.di

import androidx.lifecycle.ViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import net.synapticweb.cipherpass.di.ViewModelKey
import net.synapticweb.cipherpass.settings.EnterPasswordViewModel

@Module
abstract class EnterPasswordModule {
    @Binds
    @IntoMap
    @ViewModelKey(EnterPasswordViewModel::class)
    abstract fun bindViewModel(viewModel : EnterPasswordViewModel) : ViewModel
}