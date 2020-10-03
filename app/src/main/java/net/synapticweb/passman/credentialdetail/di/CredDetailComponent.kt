package net.synapticweb.passman.credentialdetail.di

import dagger.Subcomponent
import net.synapticweb.passman.credentialdetail.CredDetailFragment
import net.synapticweb.passman.credentialslist.di.CredListModule

@Subcomponent(modules = [CredDetailModule::class])
interface CredDetailComponent {
    @Subcomponent.Factory
    interface Factory {
        fun create() : CredDetailComponent
    }

    fun inject(fragment: CredDetailFragment)
}