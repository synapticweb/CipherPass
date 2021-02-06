/*
 * Copyright (C) 2021 Eugen Rădulescu <synapticwebb@gmail.com>
 * This file is part of the CipherPass project ( https://github.com/synapticweb/CipherPass ).
 * See the LICENSE file in the project root for license terms.
 */

package net.synapticweb.cipherpass

import net.synapticweb.cipherpass.di.AppComponent
import net.synapticweb.cipherpass.di.DaggerTestAppComponent

class TestCipherPassApp : CipherPassApp() {
    override fun initializeComponent(): AppComponent {
        return DaggerTestAppComponent.factory().create(applicationContext, this)
    }

}