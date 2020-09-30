package net.synapticweb.passman.credentialslist.di

import dagger.Subcomponent
import net.synapticweb.passman.credentialslist.CredListFragment

@Subcomponent(modules = [CredListModule::class])
interface CredListComponent {
    @Subcomponent.Factory
    interface Factory {
        fun create() : CredListComponent
    }

    fun inject(fragment: CredListFragment)
}