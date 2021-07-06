/*
 * Copyright (C) 2021 Eugen Rădulescu <synapticwebb@gmail.com>
 * This file is part of the CipherPass project ( https://github.com/synapticweb/CipherPass ).
 * See the LICENSE file in the project root for license terms.
 */

package net.synapticweb.cipherpass.autofill

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.fragment.app.activityViewModels
import net.synapticweb.cipherpass.ActivityViewModel
import net.synapticweb.cipherpass.CipherPassApp
import net.synapticweb.cipherpass.authenticate.AuthenticateFragment
import net.synapticweb.cipherpass.databinding.MatchedEntriesFragmentBinding
import net.synapticweb.cipherpass.util.EventObserver
import javax.inject.Inject

const val INSERTED_BY_AUTOFILL = "inserted_by_autofill"

class MatchedEntriesFragment : Fragment() {
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private val viewModel by viewModels<MatchedEntriesViewModel> {viewModelFactory}
    private val activityViewModel by activityViewModels<ActivityViewModel> { viewModelFactory }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        val parentActivity : Activity = context as Activity
        val app : CipherPassApp = parentActivity.application as CipherPassApp
        app.appComponent.autofillComponent().create().inject(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = MatchedEntriesFragmentBinding.inflate(inflater)
        val clientDomain = activity?.intent?.getStringExtra(CLIENT_DOMAIN_NAME)
        clientDomain?.let {
            viewModel.checkDomainExists(clientDomain)
        }

        activityViewModel.unauthorized.observe(viewLifecycleOwner, EventObserver {
            val authFragment = AuthenticateFragment()
            val args = Bundle()
            args.putBoolean(INSERTED_BY_AUTOFILL, true)
            authFragment.arguments = args
            (activity as AutofillActivity).insertFragment(authFragment)
        })

        activityViewModel.authorized.observe(viewLifecycleOwner, EventObserver {})
        return binding.root
    }
}