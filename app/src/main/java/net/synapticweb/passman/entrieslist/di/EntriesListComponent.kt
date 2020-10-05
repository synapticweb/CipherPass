package net.synapticweb.passman.entrieslist.di

import dagger.Subcomponent
import net.synapticweb.passman.entrieslist.EntriesListFragment

@Subcomponent(modules = [EntriesListModule::class])
interface EntriesListComponent {
    @Subcomponent.Factory
    interface Factory {
        fun create() : EntriesListComponent
    }

    fun inject(fragment: EntriesListFragment)
}