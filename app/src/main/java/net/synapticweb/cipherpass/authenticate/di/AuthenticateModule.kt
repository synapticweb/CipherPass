package net.synapticweb.cipherpass.authenticate.di

import androidx.lifecycle.ViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import net.synapticweb.cipherpass.authenticate.AuthenticateViewModel
import net.synapticweb.cipherpass.di.ViewModelKey

@Module
abstract class AuthenticateModule {
    @Binds
    @IntoMap
    @ViewModelKey(AuthenticateViewModel::class)
    abstract fun bindViewModel(viewModel : AuthenticateViewModel) : ViewModel
}