package net.synapticweb.passman.di

import dagger.Module
import net.synapticweb.passman.authenticate.di.AuthenticateComponent
import net.synapticweb.passman.secretslist.di.SecretsListComponent
import net.synapticweb.passman.settings.di.SettingsComponent

@Module(subcomponents = [SecretsListComponent::class, AuthenticateComponent::class,
    SettingsComponent::class])
class AppSubcomponents