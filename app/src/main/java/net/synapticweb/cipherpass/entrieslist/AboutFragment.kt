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
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import net.synapticweb.cipherpass.CipherPassApp
import net.synapticweb.cipherpass.LockStateViewModel
import net.synapticweb.cipherpass.databinding.AboutFragmentBinding
import net.synapticweb.cipherpass.util.EventObserver
import javax.inject.Inject

class AboutFragment : Fragment() {
    @Inject
    lateinit var viewModelFactory : ViewModelProvider.Factory
    private val lockState by activityViewModels<LockStateViewModel> {viewModelFactory}

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
    ): View {
        val binding = AboutFragmentBinding.inflate(inflater, container, false)
        binding.packageInfo = requireContext().packageManager
                .getPackageInfo(requireContext().packageName, 0)
        lockState.unauthorized.observe(viewLifecycleOwner,
            EventObserver {
                if (it)
                    findNavController().navigate(
                        AboutFragmentDirections.actionAboutFragmentToAuthenticateFragment()
                    )
            })
        return binding.root
    }
}