/*
 * Copyright (C) 2021 Eugen Rădulescu <synapticwebb@gmail.com>
 * This file is part of the CipherPass project ( https://github.com/synapticweb/CipherPass ).
 * See the LICENSE file in the project root for license terms.
 */

package net.synapticweb.cipherpass.addeditentry

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import net.synapticweb.cipherpass.*
import net.synapticweb.cipherpass.databinding.SetIconFragmentBinding
import net.synapticweb.cipherpass.util.EventObserver
import net.synapticweb.cipherpass.util.getSetIconNumColumns
import javax.inject.Inject

const val SET_ICON_BUNDLE_KEY = "bundle_key"

class SetIconFragment : Fragment(){
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private val _viewModel by viewModels<SetIconViewModel> {viewModelFactory}
    private val lockState by activityViewModels<LockStateViewModel> { viewModelFactory }

    private lateinit var binding : SetIconFragmentBinding

            override fun onAttach(context: Context) {
        super.onAttach(context)
        val parentActivity: Activity = context as Activity
        val app: CipherPassApp = parentActivity.application as CipherPassApp
        app.appComponent.addEntryComponent().create().inject(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.set_icon_fragment, container, false)
        binding = SetIconFragmentBinding.bind(root)
        binding.lifecycleOwner = this.viewLifecycleOwner
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setupRecycler()
        setupChooseIcon()
        setupNavigation()
    }

    private fun setupChooseIcon() {
        _viewModel.icon.observe(viewLifecycleOwner, Observer {
            parentFragmentManager.setFragmentResult(SET_ICON_REQUEST_KEY,
                bundleOf(SET_ICON_BUNDLE_KEY to it))
            findNavController().popBackStack()
        })
    }

    private fun setupRecycler() {
        val adapter = SetIconAdapter(_viewModel, requireContext())
        val viewManager = GridLayoutManager(requireContext(), getSetIconNumColumns(requireContext()))
        binding.iconsGrid.layoutManager = viewManager
        binding.iconsGrid.adapter = adapter
    }

    private fun setupNavigation() {
        lockState.unauthorized.observe(viewLifecycleOwner,
            EventObserver {
                if (it)
                    findNavController().navigate(
                        SetIconFragmentDirections.actionSetIconFragmentToAuthenticateFragment()
                    )
            })
    }
}