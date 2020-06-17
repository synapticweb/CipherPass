package net.synapticweb.passman.di

import dagger.Module
import net.synapticweb.passman.authenticate.di.AuthenticateComponent
import net.synapticweb.passman.secretslist.di.SecretsListComponent

@Module(subcomponents = [SecretsListComponent::class, AuthenticateComponent::class])
class AppSubcomponents