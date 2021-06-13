/*
 * Copyright (C) 2021 Eugen Rădulescu <synapticwebb@gmail.com>
 * This file is part of the CipherPass project ( https://github.com/synapticweb/CipherPass ).
 * See the LICENSE file in the project root for license terms.
 */

package net.synapticweb.cipherpass.autofill.di

import dagger.Subcomponent
import net.synapticweb.cipherpass.autofill.CipherPassService
import net.synapticweb.cipherpass.autofill.MatchedEntriesFragment

@Subcomponent(modules = [MatchedEntriesModule::class])
interface AutofillComponent {
    @Subcomponent.Factory
    interface Factory {
        fun create() : AutofillComponent
    }

    fun inject(fragment : MatchedEntriesFragment)
    fun inject(service : CipherPassService)
}