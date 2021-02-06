/*
 * Copyright (C) 2021 Eugen Rădulescu <synapticwebb@gmail.com>
 * This file is part of the CipherPass project ( https://github.com/synapticweb/CipherPass ).
 * See the LICENSE file in the project root for license terms.
 */

package net.synapticweb.cipherpass.entrieslist.di

import dagger.Subcomponent
import net.synapticweb.cipherpass.entrieslist.AboutFragment
import net.synapticweb.cipherpass.entrieslist.EntriesListFragment
import net.synapticweb.cipherpass.entrieslist.PrivPolicyFragment

@Subcomponent(modules = [EntriesListModule::class])
interface EntriesListComponent {
    @Subcomponent.Factory
    interface Factory {
        fun create() : EntriesListComponent
    }

    fun inject(fragment: EntriesListFragment)
    fun inject(fragment : AboutFragment)
    fun inject(fragment: PrivPolicyFragment)
}