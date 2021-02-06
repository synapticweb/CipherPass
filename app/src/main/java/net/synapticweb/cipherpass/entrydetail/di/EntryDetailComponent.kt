/*
 * Copyright (C) 2021 Eugen Rădulescu <synapticwebb@gmail.com>
 * This file is part of the CipherPass project ( https://github.com/synapticweb/CipherPass ).
 * See the LICENSE file in the project root for license terms.
 */

package net.synapticweb.cipherpass.entrydetail.di

import dagger.Subcomponent
import net.synapticweb.cipherpass.entrydetail.EntryDetailFragment

@Subcomponent(modules = [EntryDetailModule::class])
interface EntryDetailComponent {
    @Subcomponent.Factory
    interface Factory {
        fun create() : EntryDetailComponent
    }

    fun inject(fragment: EntryDetailFragment)
}