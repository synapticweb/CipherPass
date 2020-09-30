package net.synapticweb.passman.addeditcredential.di

import androidx.lifecycle.ViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import net.synapticweb.passman.addeditcredential.AddeditCredViewModel
import net.synapticweb.passman.di.ViewModelKey

@Module
abstract class AddeditCredModule {
    @Binds
    @IntoMap
    @ViewModelKey(AddeditCredViewModel::class)
    abstract fun bindViewModel(viewModel : AddeditCredViewModel) : ViewModel
}