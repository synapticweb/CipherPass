/*
 * Copyright (C) 2021 Eugen Rădulescu <synapticwebb@gmail.com>
 * This file is part of the CipherPass project ( https://github.com/synapticweb/CipherPass ).
 * See the LICENSE file in the project root for license terms.
 */

package net.synapticweb.cipherpass.autofill

import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import net.synapticweb.cipherpass.ActivityViewModel
import net.synapticweb.cipherpass.CipherPassApp
import net.synapticweb.cipherpass.R
import javax.inject.Inject

class AutofillActivity : AppCompatActivity() {
    @Inject
    lateinit var viewModelFactory : ViewModelProvider.Factory
    private lateinit var activityViewModel : ActivityViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        (application as CipherPassApp).appComponent.inject(this)
        activityViewModel = ViewModelProvider(this, viewModelFactory).get(ActivityViewModel::class.java)

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