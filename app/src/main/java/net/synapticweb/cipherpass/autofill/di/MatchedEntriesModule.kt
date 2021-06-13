/*
 * Copyright (C) 2021 Eugen Rădulescu <synapticwebb@gmail.com>
 * This file is part of the CipherPass project ( https://github.com/synapticweb/CipherPass ).
 * See the LICENSE file in the project root for license terms.
 */

package net.synapticweb.cipherpass.autofill.di

import androidx.lifecycle.ViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import net.synapticweb.cipherpass.autofill.MatchedEntriesViewModel
import net.synapticweb.cipherpass.di.ViewModelKey

@Module
abstract class MatchedEntriesModule {
    @Binds
    @IntoMap
    @ViewModelKey(MatchedEntriesViewModel::class)
    abstract fun bindViewModel(viewModel : MatchedEntriesViewModel) : ViewModel
}