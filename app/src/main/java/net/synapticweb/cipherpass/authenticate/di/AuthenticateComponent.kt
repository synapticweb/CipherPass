/*
 * Copyright (C) 2021 Eugen Rădulescu <synapticwebb@gmail.com>
 * This file is part of the CipherPass project ( https://github.com/synapticweb/CipherPass ).
 * See the LICENSE file in the project root for license terms.
 */

package net.synapticweb.cipherpass.authenticate.di

import dagger.Subcomponent
import net.synapticweb.cipherpass.authenticate.AuthenticateFragment

@Subcomponent(modules = [AuthenticateModule::class])
interface AuthenticateComponent {
    @Subcomponent.Factory
    interface Factory {
        fun create() : AuthenticateComponent
    }

    fun inject(fragment: AuthenticateFragment)
}