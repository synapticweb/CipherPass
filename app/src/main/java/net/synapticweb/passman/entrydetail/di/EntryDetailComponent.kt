package net.synapticweb.passman.entrydetail.di

import dagger.Subcomponent
import net.synapticweb.passman.entrydetail.EntryDetailFragment

@Subcomponent(modules = [EntryDetailModule::class])
interface EntryDetailComponent {
    @Subcomponent.Factory
    interface Factory {
        fun create() : EntryDetailComponent
    }

    fun inject(fragment: EntryDetailFragment)
}