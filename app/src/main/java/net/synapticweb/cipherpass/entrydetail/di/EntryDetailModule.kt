package net.synapticweb.cipherpass.entrydetail.di;

import androidx.lifecycle.ViewModel;

import net.synapticweb.cipherpass.entrydetail.EntryDetailViewModel;
import net.synapticweb.cipherpass.di.ViewModelKey;

import dagger.Binds;
import dagger.Module;
import dagger.multibindings.IntoMap;

@Module
abstract class EntryDetailModule {
    @Binds
    @IntoMap
    @ViewModelKey(EntryDetailViewModel::class)
    abstract fun bindViewModel(viewModel : EntryDetailViewModel) : ViewModel
}
