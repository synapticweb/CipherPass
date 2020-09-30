package net.synapticweb.passman.addeditcredential.di

import dagger.Subcomponent
import net.synapticweb.passman.addeditcredential.AddeditCredFragment

@Subcomponent(modules = [AddeditCredModule::class])
interface AddeditCredComponent {
    @Subcomponent.Factory
    interface Factory {
        fun create() : AddeditCredComponent
    }

    fun inject(fragment : AddeditCredFragment)
}