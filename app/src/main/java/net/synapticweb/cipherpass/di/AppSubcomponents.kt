/*
 * Copyright (C) 2021 Eugen Rădulescu <synapticwebb@gmail.com>
 * This file is part of the CipherPass project ( https://github.com/synapticweb/CipherPass ).
 * See the LICENSE file in the project root for license terms.
 */

package net.synapticweb.cipherpass.di

import dagger.Module
import net.synapticweb.cipherpass.addeditentry.di.AddeditEntryComponent
import net.synapticweb.cipherpass.authenticate.di.AuthenticateComponent
import net.synapticweb.cipherpass.autofill.di.AutofillComponent
import net.synapticweb.cipherpass.entrydetail.di.EntryDetailComponent
import net.synapticweb.cipherpass.entrieslist.di.EntriesListComponent
import net.synapticweb.cipherpass.settings.di.SettingsComponent

@Module(subcomponents = [EntriesListComponent::class, AuthenticateComponent::class,
    SettingsComponent::class, AddeditEntryComponent::class, EntryDetailComponent::class,
    AutofillComponent::class])
class AppSubcomponents