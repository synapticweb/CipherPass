/*
 * Copyright (C) 2021 Eugen Rădulescu <synapticwebb@gmail.com>
 * This file is part of the CipherPass project ( https://github.com/synapticweb/CipherPass ).
 * See the LICENSE file in the project root for license terms.
 */

package net.synapticweb.cipherpass.autofill

import android.os.Bundle
import android.os.PersistableBundle
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import net.synapticweb.cipherpass.R

class AutofillActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.autofill_activity)
        window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
        setSupportActionBar(findViewById(R.id.auth_toolbar))

        val fragment = MatchedEntriesFragment()
        insertFragment(fragment)
    }

    fun insertFragment(fragment: Fragment) {
        val fm = supportFragmentManager
            fm.beginTransaction().
                    replace(R.id.autofill_fragment, fragment).
                    commit()
    }
}