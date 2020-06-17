package net.synapticweb.passman.authenticate.di

import dagger.Subcomponent
import net.synapticweb.passman.authenticate.AuthenticateFragment

@Subcomponent(modules = [AuthenticateModule::class])
interface AuthenticateComponent {
    @Subcomponent.Factory
    interface Factory {
        fun create() : AuthenticateComponent
    }

    fun inject(fragment: AuthenticateFragment)
}