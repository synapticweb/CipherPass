package net.synapticweb.passman.authenticate

import android.app.Activity
import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
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
import net.synapticweb.passman.util.*
import java.util.*
import javax.inject.Inject

class AuthenticateFragment : Fragment() {
    @Inject
    lateinit var viewModelFactory : ViewModelProvider.Factory

    private val viewModelFrg by viewModels<AuthenticateViewModel> { viewModelFactory }
    private val lockState by activityViewModels<LockStateViewModel> {viewModelFactory}

    private lateinit var binding : AuthenticateFragmentBinding

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
        binding = AuthenticateFragmentBinding.inflate(inflater, container, false).apply {
            viewModel = viewModelFrg
            lifecycleOwner = fragment
        }
        setupSendPass()

        when(viewModelFrg.getApplockPref()) {
            APPLOCK_SYSTEM_VALUE -> {
                val keyMan =
                    requireActivity().getSystemService(Context.KEYGUARD_SERVICE) as
                            KeyguardManager
                val authIntent =
                    keyMan.createConfirmDeviceCredentialIntent(getString(R.string.app_name), getString(R.string.auth_subtitle))
                authIntent?.also { intent ->
                    lockState.startedUnlockActivity = true
                    startActivityForResult(intent, LOCK_ACTIVITY_CODE)
                }
                    ?: run {
                        //necesar pentru că încă nu s-a încheiat onCreateView()
                        Snackbar.make(requireActivity().findViewById(android.R.id.content), getString(R.string.system_lock_unavailable),
                            Snackbar.LENGTH_LONG).show()
                    }
            }
            APPLOCK_NOLOCK_VALUE -> {
                binding.passLayout.visibility = View.GONE
                binding.sendPass.visibility = View.GONE
                viewModelFrg.getPassphrase()
            }
        }

        viewModelFrg.passwd.observe(viewLifecycleOwner, Observer {
            if(it != null)
                viewModelFrg.authenticate(it)
            else
                Snackbar.make(requireActivity().findViewById(android.R.id.content), getString(R.string.system_lock_unavailable),
                    Snackbar.LENGTH_LONG).show()
        })

        setupPasswordFields(binding.passLayout, arrayOf(binding.passphrase,
            binding.passphraseRetype))

        viewModelFrg.authResult.observe(viewLifecycleOwner, EventObserver {
            lockState.startedUnlockActivity = false
            when(it) {
                AUTH_OK -> {
                    binding.passphrase.text!!.clear()
                    binding.passphraseRetype.text.clear()
                    findNavController().navigate(
                        AuthenticateFragmentDirections.actionAuthenticateFragmentToSecretsListFragment()
                    )
                }

                R.string.pass_incorect ->
                    binding.passLayout.error = getString(R.string.pass_incorect)

                R.string.error_setting_pass -> Snackbar.make(requireView(),
                    getString(R.string.error_setting_pass), Snackbar.LENGTH_SHORT).show()
            }
        })

        binding.passphrase.addTextChangedListener {
            binding.passLayout.error = null
        }

        handleBackPressed(lockState)
        return binding.root
    }

    private fun setupSendPass() {
        binding.sendPass.setOnClickListener {
            if (binding.passphrase.text!!.isEmpty()) {
                binding.passLayout.error = getString(R.string.pass_empty)
                return@setOnClickListener
            }
            val pass = editableToCharArray(binding.passphrase.text!!)
            val rePass = editableToCharArray(binding.passphraseRetype.text)

            if (!viewModelFrg.isPassSet() && !pass.contentEquals(rePass)) {
                binding.passLayout.error = getString(R.string.pass_no_match)
                return@setOnClickListener
            }

            Arrays.fill(rePass, 0.toChar())
            binding.passphrase.text?. let {
                viewModelFrg.authenticate(pass)
             }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if(requestCode == LOCK_ACTIVITY_CODE && resultCode == Activity.RESULT_OK) {
            binding.passLayout.visibility = View.GONE
            binding.sendPass.visibility = View.GONE
            viewModelFrg.getPassphrase()
        }
    }
}