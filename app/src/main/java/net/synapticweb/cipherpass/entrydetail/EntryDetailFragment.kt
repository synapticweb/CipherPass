/*
 * Copyright (C) 2021 Eugen Rădulescu <synapticwebb@gmail.com>
 * This file is part of the CipherPass project ( https://github.com/synapticweb/CipherPass ).
 * See the LICENSE file in the project root for license terms.
 */

package net.synapticweb.cipherpass.entrydetail

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.view.*
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.afollestad.materialdialogs.MaterialDialog
import net.synapticweb.cipherpass.CipherPassApp
import net.synapticweb.cipherpass.ActivityViewModel
import net.synapticweb.cipherpass.R
import net.synapticweb.cipherpass.databinding.EntryDetailFragmentBinding
import net.synapticweb.cipherpass.util.EventObserver
import javax.inject.Inject

class EntryDetailFragment : Fragment() {
    @Inject
    lateinit var viewModelFactory : ViewModelProvider.Factory
    private val _viewModel by viewModels<EntryDetailViewModel> { viewModelFactory }
    private val activityViewModel by activityViewModels<ActivityViewModel> {viewModelFactory}
    private val args : EntryDetailFragmentArgs by navArgs()
    private lateinit var binding: EntryDetailFragmentBinding

    override fun onAttach(context: Context) {
        super.onAttach(context)
        val parentActivity : Activity = context as Activity
        val app : CipherPassApp = parentActivity.application as CipherPassApp
        app.appComponent.entryDetailComponent().create().inject(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
       val root = inflater.inflate(R.layout.entry_detail_fragment, container, false)
        binding = EntryDetailFragmentBinding.bind(root).apply {
            viewModel = _viewModel
            fragment = this@EntryDetailFragment
        }
        binding.lifecycleOwner = viewLifecycleOwner
        _viewModel.load(args.entryId)
        setHasOptionsMenu(true)

        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.detail_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId) {
            R.id.delete -> {
                MaterialDialog(requireContext()).show {
                    title(R.string.confirm_deletion_title)
                    message(R.string.confirm_deletion_message)
                    positiveButton(android.R.string.ok) {
                        _viewModel.deleteEntry()
                    }
                    negativeButton(android.R.string.cancel) {  }
                }
                true
            }
            else -> false
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setupNavigation()
        setupCopyToast()
        setupCustomFieldsRecycler()
        setTitle()
    }

    private fun setupNavigation() {
        activityViewModel.unauthorized.observe(viewLifecycleOwner,
            EventObserver {
                if (it)
                    findNavController().navigate(
                        EntryDetailFragmentDirections.actionEntryDetailFragmentToAuthenticateFragment()
                    )
            })

        _viewModel.unauthorized.observe(viewLifecycleOwner, EventObserver {
            if (it)
                findNavController().navigate(
                    EntryDetailFragmentDirections.actionEntryDetailFragmentToAuthenticateFragment()
                )
        })

        _viewModel.finishDeletion.observe(viewLifecycleOwner, EventObserver {
            Toast.makeText(requireContext(), getString(R.string.deletion_ok), Toast.LENGTH_SHORT).show()
            val action = EntryDetailFragmentDirections.actionEntryDetailFragmentToEntriesListFragment()
            findNavController().navigate(action)
        })
    }

    fun togglePassword(password : EditText, icon : ImageButton) {
        if(password.transformationMethod is PasswordTransformationMethod) {
            password.transformationMethod = HideReturnsTransformationMethod.getInstance()
            icon.setImageResource(R.drawable.eye_cut)
        }
        else {
            password.transformationMethod = PasswordTransformationMethod.getInstance()
            icon.setImageResource(R.drawable.eye)
        }
    }

    fun fabListener(entryId : Long, title : String) {
        val action = EntryDetailFragmentDirections
            .actionEntryDetailFragmentToAddeditEntryFragment(entryId, title)
        findNavController().navigate(action)
    }

    private fun setupCopyToast() {
        _viewModel.finishCopy.observe(viewLifecycleOwner, EventObserver {
            Toast.makeText(requireContext(), it + " " + getString(R.string.copied_to_clipboard), Toast.LENGTH_SHORT).show()
        })
    }

    private fun setTitle() {
        _viewModel.entry.observe(viewLifecycleOwner, Observer {
            val activity = requireActivity()
            //necesar pentru că altfel în testele care folosesc launchFragmentinContainer apare eroarea:
            //androidx.fragment.app.testing.FragmentScenario$EmptyFragmentActivity cannot be cast to
            // androidx.appcompat.app.AppCompatActivity
            if(activity is AppCompatActivity)
                activity.supportActionBar?.title = it.entryName
        })
    }

    private fun setupCustomFieldsRecycler() {
        val adapter = CustomFieldsAdapter(_viewModel, this)
        binding.customFields.adapter = adapter
        binding.customFields.isNestedScrollingEnabled = false
        _viewModel.customFields.observe(viewLifecycleOwner, Observer {
            adapter.submitList(it)
        })
    }
}