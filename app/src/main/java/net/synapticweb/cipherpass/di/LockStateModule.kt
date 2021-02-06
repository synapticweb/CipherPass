/*
 * Copyright (C) 2021 Eugen Rădulescu <synapticwebb@gmail.com>
 * This file is part of the CipherPass project ( https://github.com/synapticweb/CipherPass ).
 * See the LICENSE file in the project root for license terms.
 */

package net.synapticweb.cipherpass.di

import androidx.lifecycle.ViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import net.synapticweb.cipherpass.LockStateViewModel
import javax.inject.Singleton

@Module
abstract class LockStateModule {
    @Binds
    @IntoMap
    @Singleton
    @ViewModelKey(LockStateViewModel::class)
    abstract fun bindViewModel(viewModel : LockStateViewModel) : ViewModel
}
