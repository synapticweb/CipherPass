package net.synapticweb.cipherpass.entrydetail.di

import dagger.Subcomponent
import net.synapticweb.cipherpass.entrydetail.EntryDetailFragment

@Subcomponent(modules = [EntryDetailModule::class])
interface EntryDetailComponent {
    @Subcomponent.Factory
    interface Factory {
        fun create() : EntryDetailComponent
    }

    fun inject(fragment: EntryDetailFragment)
}