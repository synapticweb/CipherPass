package net.synapticweb.passman.authenticate

import android.app.Activity
import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.viewModels
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.google.android.material.snackbar.Snackbar
import net.synapticweb.passman.*
import net.synapticweb.passman.databinding.AuthenticateFragmentBinding
import net.synapticweb.passman.util.EventObserver
import net.synapticweb.passman.util.editableToByteArray
import net.synapticweb.passman.util.editableToCharArray
import net.synapticweb.passman.util.handleBackPressed
import javax.inject.Inject

class AuthenticateFragment : Fragment() {
    @Inject
    lateinit var viewModelFactory : ViewModelProvider.Factory

    private val viewModelFrg by viewModels<AuthenticateViewModel> { viewModelFactory }
    private val lockState by activityViewModels<LockStateViewModel> {viewModelFactory}

    private lateinit var viewDataBinding : AuthenticateFragmentBinding

    override fun onAttach(context: Context) {
        super.onAttach(context)
        val parentActivity : AppCompatActivity = context as AppCompatActivity
        val app : CryptoPassApp = parentActivity.application as CryptoPassApp
        app.appComponent.authenticateComponent().create().inject(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val fragment = this
        viewDataBinding = AuthenticateFragmentBinding.inflate(inflater, container, false).apply {
            viewModel = viewModelFrg
            lifecycleOwner = fragment
            lockStateViewModel = lockState
        }

        when(viewModelFrg.getApplockPref()) {
            APPLOCK_PASSWD_VALUE -> setupSendPass()
            APPLOCK_SYSTEM_VALUE -> {
                val keyMan =
                    requireActivity().getSystemService(Context.KEYGUARD_SERVICE) as
                            KeyguardManager
                val authIntent =
                    keyMan.createConfirmDeviceCredentialIntent("Auth", "Authentication required")
                authIntent?.also { intent ->
                    lockState.startedUnlockActivity = true
                    startActivityForResult(intent, LOCK_ACTIVITY_CODE)
                }
                    ?: run {
                        setupSendPass()
                        //necesar pentru că încă nu s-a încheiat onCreateView()
                        Snackbar.make(requireActivity().findViewById(android.R.id.content), getString(R.string.system_lock_unavailable),
                            Snackbar.LENGTH_LONG).show()
                    }
            }
            APPLOCK_NOLOCK_VALUE -> {
                viewDataBinding.passLayout.visibility = View.GONE
                viewDataBinding.sendPass.visibility = View.GONE
                viewModelFrg.getPassphrase()
            }
        }

        viewModelFrg.passwd.observe(viewLifecycleOwner, Observer {
            lockState.unlockRepo(it)
        })

        val passphrase = viewDataBinding.passphrase
        val rePassphrase = viewDataBinding.passphraseRetype

        //https://medium.com/@droidbyme/show-hide-password-in-edittext-in-android-c4c3db44f734
        viewDataBinding.passLayout.setEndIconOnClickListener {
            if(passphrase.transformationMethod is PasswordTransformationMethod) {
                passphrase.transformationMethod = HideReturnsTransformationMethod.getInstance()
                rePassphrase.transformationMethod = HideReturnsTransformationMethod.getInstance()
            }
            else {
                passphrase.transformationMethod = PasswordTransformationMethod.getInstance()
                rePassphrase.transformationMethod = PasswordTransformationMethod.getInstance()
            }
        }

        lockState.unlockSuccess.observe(viewLifecycleOwner,
            EventObserver {
                if (it) {
                    if (!viewModelFrg.isPassSet()) {
                        viewModelFrg.setPassSet()
                        passphrase.text ?.let { editable ->
                            viewModelFrg.createPassHash(editableToCharArray(editable))
                            editable.clear()
                            rePassphrase.text.clear()
                        }
                    }
                    findNavController().navigate(
                        AuthenticateFragmentDirections.actionAuthenticateFragmentToSecretsListFragment()
                    )
                } else
                    viewDataBinding.passLayout.error = getString(R.string.pass_incorect)
            })

        passphrase.addTextChangedListener {
            viewDataBinding.passLayout.error = null
        }

        handleBackPressed(lockState)
        return viewDataBinding.root
    }

    private fun setupSendPass() {
        viewDataBinding.sendPass.setOnClickListener {
            if (viewModelFrg.passEmpty()) {
                viewDataBinding.passLayout.error = getString(R.string.pass_empty)
                return@setOnClickListener
            }
            if (!viewModelFrg.isPassSet() && !viewModelFrg.passMatch()) {
                viewDataBinding.passLayout.error = getString(R.string.pass_no_match)
                return@setOnClickListener
            }

            viewDataBinding.passphrase.text?. let {
                lockState.unlockRepo(editableToByteArray(it))
             }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if(requestCode == LOCK_ACTIVITY_CODE && resultCode == Activity.RESULT_OK) {
            viewDataBinding.passLayout.visibility = View.GONE
            viewDataBinding.sendPass.visibility = View.GONE
            viewModelFrg.getPassphrase()
        }
        else if(requestCode == LOCK_ACTIVITY_CODE && resultCode == Activity.RESULT_CANCELED) {
            setupSendPass()
        }
    }
}