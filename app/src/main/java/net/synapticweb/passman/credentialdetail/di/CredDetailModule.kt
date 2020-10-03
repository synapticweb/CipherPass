package net.synapticweb.passman.credentialdetail.di;

import androidx.lifecycle.ViewModel;

import net.synapticweb.passman.credentialdetail.CredDetailViewModel;
import net.synapticweb.passman.di.ViewModelKey;

import dagger.Binds;
import dagger.Module;
import dagger.multibindings.IntoMap;

@Module
abstract class CredDetailModule {
    @Binds
    @IntoMap
    @ViewModelKey(CredDetailViewModel::class)
    abstract fun bindViewModel(viewModel : CredDetailViewModel) : ViewModel
}
