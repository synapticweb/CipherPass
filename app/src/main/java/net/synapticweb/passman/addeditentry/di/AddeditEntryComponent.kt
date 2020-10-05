package net.synapticweb.passman.addeditentry.di

import dagger.Subcomponent
import net.synapticweb.passman.addeditentry.AddeditEntryFragment

@Subcomponent(modules = [AddeditEntryModule::class])
interface AddeditEntryComponent {
    @Subcomponent.Factory
    interface Factory {
        fun create() : AddeditEntryComponent
    }

    fun inject(fragment : AddeditEntryFragment)
}