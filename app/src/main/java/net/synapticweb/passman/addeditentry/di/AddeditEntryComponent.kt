package net.synapticweb.passman.addeditentry.di

import dagger.Subcomponent
import net.synapticweb.passman.addeditentry.AddeditEntryFragment
import net.synapticweb.passman.addeditentry.SetIconFragment

@Subcomponent(modules = [AddeditEntryModule::class, SetIconModule::class])
interface AddeditEntryComponent {
    @Subcomponent.Factory
    interface Factory {
        fun create() : AddeditEntryComponent
    }

    fun inject(fragment : AddeditEntryFragment)
    fun inject(fragment : SetIconFragment)
}