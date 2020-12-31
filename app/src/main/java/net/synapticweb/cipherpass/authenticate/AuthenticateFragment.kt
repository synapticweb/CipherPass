package net.synapticweb.cipherpass.authenticate

import android.app.Activity
import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.viewModels
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.google.android.material.snackbar.Snackbar
import net.synapticweb.cipherpass.*
import net.synapticweb.cipherpass.databinding.AuthenticateFragmentBinding
import net.synapticweb.cipherpass.util.*
import java.util.*
import javax.inject.Inject

class AuthenticateFragment : Fragment() {
    @Inject
    lateinit var viewModelFactory : ViewModelProvider.Factory

    private val _viewModel by viewModels<AuthenticateViewModel> { viewModelFactory }
    private val lockState by activityViewModels<LockStateViewModel> {viewModelFactory}

    private lateinit var binding : AuthenticateFragmentBinding

    override fun onAttach(context: Context) {
        super.onAttach(context)
        val parentActivity : AppCompatActivity = context as AppCompatActivity
        val app : CipherPassApp = parentActivity.application as CipherPassApp
        app.appComponent.authenticateComponent().create().inject(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val fragment = this
        binding = AuthenticateFragmentBinding.inflate(inflater, container, false).apply {
            viewModel = _viewModel
            lifecycleOwner = fragment
        }
        setupSendPass()

        when(_viewModel.getApplockPref()) {
            APPLOCK_SYSTEM_VALUE -> showSystemAuthDialog()

            APPLOCK_NOLOCK_VALUE -> {
                binding.passLayout.visibility = View.GONE
                binding.sendPass.visibility = View.GONE
                _viewModel.getPassphrase()
            }
        }

        _viewModel.passwd.observe(viewLifecycleOwner, Observer {
            if(it != null)
                _viewModel.authenticate(it)
            else
                Snackbar.make(requireActivity().findViewById(android.R.id.content),
                        getString(R.string.system_lock_unavailable),
                    Snackbar.LENGTH_LONG).show()
        })

        setupPasswordFields(binding.passLayout, arrayOf(binding.passphrase,
            binding.passphraseRetype))

        _viewModel.authResult.observe(viewLifecycleOwner, EventObserver {
            when(it) {
                AUTH_OK -> {
                    lockState.isInUnlockActivity = false
                    binding.passphrase.text!!.clear()
                    binding.passphraseRetype.text.clear()
                    findNavController().navigate(
                        AuthenticateFragmentDirections.actionAuthenticateFragmentToEntriesListFragment()
                    )
                }

                R.string.pass_incorect ->
                    Toast.makeText(requireContext(), getString(it), Toast.LENGTH_SHORT).show()

                R.string.error_setting_pass -> Snackbar.make(requireView(),
                    getString(R.string.error_setting_pass), Snackbar.LENGTH_SHORT).show()
            }
        })


        handleBackPressed(lockState)
        setupSystemAuth()
        return binding.root
    }

    private fun setupSendPass() {
        binding.sendPass.setOnClickListener {
            if (binding.passphrase.text!!.isEmpty()) {
                Toast.makeText(requireContext(), getString(R.string.pass_empty), Toast.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            }
            val pass = editableToCharArray(binding.passphrase.text!!)
            val rePass = editableToCharArray(binding.passphraseRetype.text)

            if (!_viewModel.isPassSet() && !pass.contentEquals(rePass)) {
                Toast.makeText(requireContext(), getString(R.string.pass_no_match), Toast.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            }

            Arrays.fill(rePass, 0.toChar())
            binding.passphrase.text?. let {
                _viewModel.authenticate(pass)
             }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if(requestCode == LOCK_ACTIVITY_CODE && resultCode == Activity.RESULT_OK) {
            binding.passLayout.visibility = View.GONE
            binding.sendPass.visibility = View.GONE
            binding.systemAuth.visibility = View.GONE
            _viewModel.getPassphrase()
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val appToolbar = requireActivity().findViewById<Toolbar>(R.id.app_toolbar)
        val authToolbar = requireActivity().findViewById<Toolbar>(R.id.auth_toolbar)
        appToolbar.visibility = View.GONE
        (requireActivity() as AppCompatActivity).setSupportActionBar(authToolbar)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        val appToolbar = requireActivity().findViewById<Toolbar>(R.id.app_toolbar)
        appToolbar.visibility = View.VISIBLE
        (requireActivity() as AppCompatActivity).setSupportActionBar(appToolbar)
    }

    private fun setupSystemAuth() {
        binding.systemAuth.setOnClickListener {
            showSystemAuthDialog()
        }
    }

    private fun showSystemAuthDialog() {
        val keyMan =
            requireActivity().getSystemService(Context.KEYGUARD_SERVICE) as
                    KeyguardManager
        val authIntent =
            keyMan.createConfirmDeviceCredentialIntent(getString(R.string.app_name), getString(R.string.auth_subtitle))
        authIntent?.also { intent ->
            lockState.isInUnlockActivity = true
            startActivityForResult(intent, LOCK_ACTIVITY_CODE)
        }
            ?: run {
                //necesar pentru că încă nu s-a încheiat onCreateView()
                Snackbar.make(requireActivity().findViewById(android.R.id.content), getString(R.string.system_lock_unavailable),
                    Snackbar.LENGTH_LONG).show()
            }
    }
}