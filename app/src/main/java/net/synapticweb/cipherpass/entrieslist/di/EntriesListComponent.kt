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