package net.synapticweb.cipherpass.authenticate.di

import dagger.Subcomponent
import net.synapticweb.cipherpass.authenticate.AuthenticateFragment

@Subcomponent(modules = [AuthenticateModule::class])
interface AuthenticateComponent {
    @Subcomponent.Factory
    interface Factory {
        fun create() : AuthenticateComponent
    }

    fun inject(fragment: AuthenticateFragment)
}