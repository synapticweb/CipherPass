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
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)
        window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)

        val navController = findNavController(R.id.nav_host_fragment)
        appBarConfiguration = AppBarConfiguration(setOf(R.id.authenticateFragment, R.id.secretsListFragment))
        setupActionBarWithNavController(navController, appBarConfiguration)
    }

    override fun onSupportNavigateUp() : Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        return navController.navigateUp(appBarConfiguration)
                || super.onSupportNavigateUp()
    }

    override fun onPause() {
        super.onPause()
        lockState.setSleepTime()
    }

    override fun onResume() {
        super.onResume()
        lockState.checkIfAuthorized()
    }
}