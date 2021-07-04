/*
 * Copyright (C) 2021 Eugen Rădulescu <synapticwebb@gmail.com>
 * This file is part of the CipherPass project ( https://github.com/synapticweb/CipherPass ).
 * See the LICENSE file in the project root for license terms.
 */

package net.synapticweb.cipherpass

import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import javax.inject.Inject

class MainActivity : AppCompatActivity() {
    @Inject
    lateinit var viewModelFactory : ViewModelProvider.Factory
    private lateinit var activityViewModel : ActivityViewModel
    private lateinit var appBarConfiguration: AppBarConfiguration

    override fun onCreate(savedInstanceState: Bundle?) {
        (application as CipherPassApp).appComponent.inject(this)
        activityViewModel = ViewModelProvider(this, viewModelFactory).get(ActivityViewModel::class.java)
        super.onCreate(if(activityViewModel.isDbUnlocked()) savedInstanceState else null)
        //Pasăm null pentru a împiedica recrearea fragmentului entrieslist (și
        //mai multy ca sigur a altora) în onStart după ce aplicația este distrusă de sistem ca urmare
        //a inactivității. Dacă se întîmplă, avem crash în repository cu database uninitialized.
        //Deoarece onStart rulează înainte de onResume, mecanismul din lockstateviewmodel nu ajunge
        //niciodată să ruleze.
        //vezi și https://stackoverflow.com/questions/15519214/prevent-fragment-recovery-in-android
        setContentView(R.layout.main_activity)
        window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)

        setSupportActionBar(findViewById(R.id.app_toolbar))
        val navController = findNavController(R.id.nav_host_fragment)
        appBarConfiguration = AppBarConfiguration(setOf(R.id.authenticateFragment, R.id.entriesListFragment))
        setupActionBarWithNavController(navController, appBarConfiguration)

        lifecycle.addObserver(activityViewModel)
    }

    override fun onSupportNavigateUp() : Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        return navController.navigateUp(appBarConfiguration)
                || super.onSupportNavigateUp()
    }
}