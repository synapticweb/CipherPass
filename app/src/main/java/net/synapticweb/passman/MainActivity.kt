package net.synapticweb.passman

import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import javax.inject.Inject

class MainActivity : AppCompatActivity() {
    @Inject
    lateinit var viewModelFactory : ViewModelProvider.Factory
    private lateinit var lockState : LockStateViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        (application as PmApp).appComponent.inject(this)
        lockState = ViewModelProvider(this, viewModelFactory).get(LockStateViewModel::class.java)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)
        window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
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