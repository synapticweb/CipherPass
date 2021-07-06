/*
 * Copyright (C) 2021 Eugen Rădulescu <synapticwebb@gmail.com>
 * This file is part of the CipherPass project ( https://github.com/synapticweb/CipherPass ).
 * See the LICENSE file in the project root for license terms.
 */

package net.synapticweb.cipherpass.entrieslist

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import net.synapticweb.cipherpass.CipherPassApp
import net.synapticweb.cipherpass.ActivityViewModel
import net.synapticweb.cipherpass.R
import net.synapticweb.cipherpass.util.EventObserver
import javax.inject.Inject

class PrivPolicyFragment : Fragment() {
    @Inject
    lateinit var viewModelFactory : ViewModelProvider.Factory
    private val activityViewModel by activityViewModels<ActivityViewModel> {viewModelFactory}

    override fun onAttach(context: Context) {
        super.onAttach(context)
        val parentActivity : Activity = context as Activity
        val app : CipherPassApp = parentActivity.application as CipherPassApp
        app.appComponent.entriesListComponent().create().inject(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.priv_policy_fragment, container, false)
        root.findViewById<TextView>(R.id.priv_pol_intro).apply {
            text = String.format(getString(R.string.priv_pol_intro, getString(R.string.app_name)))
        }

        activityViewModel.unauthorized.observe(viewLifecycleOwner,
            EventObserver {
                if (it)
                    findNavController().navigate(
                       PrivPolicyFragmentDirections.actionPrivPolicyFragmentToAuthenticateFragment()
                    )
            }
        )
        activityViewModel.authorized.observe(viewLifecycleOwner, EventObserver {})

        return root
    }
}