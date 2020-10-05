package net.synapticweb.passman.di

import dagger.Module
import net.synapticweb.passman.addeditentry.di.AddeditEntryComponent
import net.synapticweb.passman.authenticate.di.AuthenticateComponent
import net.synapticweb.passman.entrydetail.di.EntryDetailComponent
import net.synapticweb.passman.entrieslist.di.EntriesListComponent
import net.synapticweb.passman.settings.di.SettingsComponent

@Module(subcomponents = [EntriesListComponent::class, AuthenticateComponent::class,
    SettingsComponent::class, AddeditEntryComponent::class, EntryDetailComponent::class])
class AppSubcomponents