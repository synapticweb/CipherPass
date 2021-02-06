/*
 * Copyright (C) 2021 Eugen Rădulescu <synapticwebb@gmail.com>
 * This file is part of the CipherPass project ( https://github.com/synapticweb/CipherPass ).
 * See the LICENSE file in the project root for license terms.
 */

package net.synapticweb.cipherpass.addeditentry.di

import dagger.Subcomponent
import net.synapticweb.cipherpass.addeditentry.AddeditEntryFragment
import net.synapticweb.cipherpass.addeditentry.SetIconFragment

@Subcomponent(modules = [AddeditEntryModule::class, SetIconModule::class])
interface AddeditEntryComponent {
    @Subcomponent.Factory
    interface Factory {
        fun create() : AddeditEntryComponent
    }

    fun inject(fragment : AddeditEntryFragment)
    fun inject(fragment : SetIconFragment)
}