/*
 * Copyright (C) 2021 Eugen Rădulescu <synapticwebb@gmail.com>
 * This file is part of the CipherPass project ( https://github.com/synapticweb/CipherPass ).
 * See the LICENSE file in the project root for license terms.
 */

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
