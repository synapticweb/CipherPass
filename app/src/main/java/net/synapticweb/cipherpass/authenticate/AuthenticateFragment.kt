/*
 * Copyright (C) 2021 Eugen Rădulescu <synapticwebb@gmail.com>
 * This file is part of the CipherPass project ( https://github.com/synapticweb/CipherPass ).
 * See the LICENSE file in the project root for license terms.
 */

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
import com.google.android.material.snackbar.Snackbar
import net.synapticweb.cipherpass.*
import net.synapticweb.cipherpass.autofill.AutofillActivity
import net.synapticweb.cipherpass.autofill.INSERTED_BY_AUTOFILL
import net.synapticweb.cipherpass.autofill.MatchedEntriesFragment
import net.synapticweb.cipherpass.databinding.AuthenticateFragmentBinding
import net.synapticweb.cipherpass.util.*
import java.util.*
import javax.inject.Inject

const val LOCK_ACTIVITY_CODE = 0

class AuthenticateFragment : Fragment() {
    @Inject
    lateinit var viewModelFactory : ViewModelProvider.Factory

    private val _viewModel by viewModels<AuthenticateViewModel> { viewModelFactory }
    private val activityViewModel by activityViewModels<ActivityViewModel> {viewModelFactory}
    private val lastBackPress = LastBackPress()

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
    ): View {
        val fragment = this
        binding = AuthenticateFragmentBinding.inflate(inflater, container, false).apply {
            viewModel = _viewModel
            lifecycleOwner = fragment
        }
        setupSendPass()
        setupSystemAuth()
        setupPasswordFields(binding.passLayout, arrayOf(binding.passphrase,
            binding.passphraseRetype))

        when(_viewModel.getApplockPref()) {
            getString(R.string.applock_system_value) -> showSystemAuthDialog()

            getString(R.string.applock_nolock_value) -> {
                binding.passLayout.visibility = View.GONE
                binding.sendPass.visibility = View.GONE
                _viewModel.getPassphrase()
            }
        }

        _viewModel.passwd.observe(viewLifecycleOwner, {
            if(it != null)
                _viewModel.authenticate(it)
            else
                Snackbar.make(requireActivity().findViewById(android.R.id.content),
                        getString(R.string.system_lock_unavailable),
                    Snackbar.LENGTH_LONG).show()
        })


        _viewModel.authResult.observe(viewLifecycleOwner, EventObserver {
            when(it) {
                AUTH_OK -> {
                    binding.passphrase.text!!.clear()
                    binding.passphraseRetype.text.clear()
                    navigateAway()
                }

                R.string.pass_incorect ->
                    Toast.makeText(requireContext(), getString(it), Toast.LENGTH_SHORT).show()

                R.string.error_setting_pass -> Snackbar.make(requireView(),
                    getString(R.string.error_setting_pass), Snackbar.LENGTH_SHORT).show()
            }
        })

//  dacă aplicația este trimisă în background în timp ce authfrg este activ și stă mai mult de 30 de
//  sec, cînd se întoarce lockstate setează flagul unauthorized. Deoarece authfrg nu îl observă flagul
//  rămîne activ și este consumat de entrieslistfrg, ceea ce face ca activitatea să se întoarcă la
//  authfrg.
        activityViewModel.unauthorized.observe(viewLifecycleOwner, EventObserver {
            if(!it)
                navigateAway()
        })

        if(!isFromAutofill())
            handleBackPressed(lastBackPress)
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
        val appToolbar = requireActivity().findViewById<Toolbar>(getAppbarId())
        val authToolbar = requireActivity().findViewById<Toolbar>(R.id.auth_toolbar)
        appToolbar.visibility = View.GONE
        (requireActivity() as AppCompatActivity).setSupportActionBar(authToolbar)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        val appToolbar = requireActivity().findViewById<Toolbar>(getAppbarId())
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
            startActivityForResult(intent, LOCK_ACTIVITY_CODE)
        }
            ?: run {
                //necesar pentru că încă nu s-a încheiat onCreateView()
                Snackbar.make(requireActivity().findViewById(android.R.id.content), getString(R.string.system_lock_unavailable),
                    Snackbar.LENGTH_LONG).show()
            }
    }

    private fun isFromAutofill() : Boolean {
        return arguments?.getBoolean(INSERTED_BY_AUTOFILL) ?: false
    }

    private fun getAppbarId() : Int {
        return if(isFromAutofill())
            R.id.autofill_toolbar
        else
            R.id.app_toolbar
    }

    private fun navigateAway() {
        if(isFromAutofill())
            (activity as AutofillActivity).insertFragment(MatchedEntriesFragment())
        else
            findNavController().navigate(
                AuthenticateFragmentDirections.
                actionAuthenticateFragmentToEntriesListFragment()
            )
    }
}