package net.synapticweb.cipherpass.di

import dagger.Module
import net.synapticweb.cipherpass.addeditentry.di.AddeditEntryComponent
import net.synapticweb.cipherpass.authenticate.di.AuthenticateComponent
import net.synapticweb.cipherpass.entrydetail.di.EntryDetailComponent
import net.synapticweb.cipherpass.entrieslist.di.EntriesListComponent
import net.synapticweb.cipherpass.settings.di.SettingsComponent

@Module(subcomponents = [EntriesListComponent::class, AuthenticateComponent::class,
    SettingsComponent::class, AddeditEntryComponent::class, EntryDetailComponent::class])
class AppSubcomponents