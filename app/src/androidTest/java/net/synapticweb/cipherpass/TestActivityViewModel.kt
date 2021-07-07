/*
 * Copyright (C) 2021 Eugen Rădulescu <synapticwebb@gmail.com>
 * This file is part of the CipherPass project ( https://github.com/synapticweb/CipherPass ).
 * See the LICENSE file in the project root for license terms.
 */

package net.synapticweb.cipherpass

import android.app.Application
import net.synapticweb.cipherpass.data.Repository
import javax.inject.Inject

class TestActivityViewModel
@Inject constructor(repository: Repository, application: Application)
    : ActivityViewModel(repository, application) {

    override fun onAppBackgrounded() {}

    override fun onAppForegrounded() {}
}