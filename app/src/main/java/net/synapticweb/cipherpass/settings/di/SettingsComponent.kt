/*
 * Copyright (C) 2021 Eugen Rădulescu <synapticwebb@gmail.com>
 * This file is part of the CipherPass project ( https://github.com/synapticweb/CipherPass ).
 * See the LICENSE file in the project root for license terms.
 */

package net.synapticweb.cipherpass.settings.di

import dagger.Subcomponent
import net.synapticweb.cipherpass.settings.SettingsFragment

@Subcomponent(modules = [SettingsModule::class])
interface SettingsComponent {
    @Subcomponent.Factory
    interface Factory {
        fun create() : SettingsComponent
    }

    fun inject(fragment: SettingsFragment)
}