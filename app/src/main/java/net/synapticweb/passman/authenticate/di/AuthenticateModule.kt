package net.synapticweb.passman.authenticate.di

import androidx.lifecycle.ViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import net.synapticweb.passman.authenticate.AuthenticateViewModel
import net.synapticweb.passman.di.ViewModelKey

@Module
abstract class AuthenticateModule {
    @Binds
    @IntoMap
    @ViewModelKey(AuthenticateViewModel::class)
    abstract fun bindViewModel(viewModel : AuthenticateViewModel) : ViewModel
}