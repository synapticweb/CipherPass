package net.synapticweb.passman.di

import dagger.Module
import net.synapticweb.passman.addeditcredential.di.AddeditCredComponent
import net.synapticweb.passman.authenticate.di.AuthenticateComponent
import net.synapticweb.passman.credentialslist.di.CredListComponent
import net.synapticweb.passman.settings.di.SettingsComponent

@Module(subcomponents = [CredListComponent::class, AuthenticateComponent::class,
    SettingsComponent::class, AddeditCredComponent::class])
class AppSubcomponents