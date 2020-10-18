package net.synapticweb.passman

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
    private lateinit var lockState : LockStateViewModel
    private lateinit var appBarConfiguration: AppBarConfiguration

    override fun onCreate(savedInstanceState: Bundle?) {
        (application as CryptoPassApp).appComponent.inject(this)
        lockState = ViewModelProvider(this, viewModelFactory).get(LockStateViewModel::class.java)
        super.onCreate(if(lockState.isDbUnlocked()) savedInstanceState else null)
        //Pasăm null pentru a împiedica recrearea fragmentului entrieslist (și
        //mai multy ca sigur a altora) în onStart după ce aplicația este distrusă de sistem ca urmare
        //a inactivității. Dacă se întîmplă, avem crash în repository cu database uninitialized.
        //Deoarece onStart rulează înainte de onResume, mecanismul din lockstateviewmodel nu ajunge
        //niciodată să ruleze.
        //vezi și https://stackoverflow.com/questions/15519214/prevent-fragment-recovery-in-android
        setContentView(R.layout.main_activity)
        window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)

        val navController = findNavController(R.id.nav_host_fragment)
        appBarConfiguration = AppBarConfiguration(setOf(R.id.authenticateFragment, R.id.entriesListFragment))
        setupActionBarWithNavController(navController, appBarConfiguration)
        lifecycle.addObserver(lockState)
    }

    override fun onSupportNavigateUp() : Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        return navController.navigateUp(appBarConfiguration)
                || super.onSupportNavigateUp()
    }
}