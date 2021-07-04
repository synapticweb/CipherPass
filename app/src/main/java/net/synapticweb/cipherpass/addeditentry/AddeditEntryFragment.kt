/*
 * Copyright (C) 2021 Eugen Rădulescu <synapticwebb@gmail.com>
 * This file is part of the CipherPass project ( https://github.com/synapticweb/CipherPass ).
 * See the LICENSE file in the project root for license terms.
 */

package net.synapticweb.cipherpass.addeditentry

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.addCallback
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.callbacks.onPreShow
import com.afollestad.materialdialogs.customview.customView
import net.synapticweb.cipherpass.*
import net.synapticweb.cipherpass.databinding.AddeditEntryFragmentBinding
import net.synapticweb.cipherpass.model.CustomField
import net.synapticweb.cipherpass.model.KEY_DRAWABLE_NAME
import net.synapticweb.cipherpass.util.EventObserver
import net.synapticweb.cipherpass.util.disablePositiveWhenEmpty
import net.synapticweb.cipherpass.util.setupPasswordFields
import javax.inject.Inject

const val SET_ICON_REQUEST_KEY = "icon_req_key"

class AddeditEntryFragment : Fragment(), CustomFieldsEditFragment {
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private val _viewModel by viewModels<AddeditEntryViewModel> { viewModelFactory }
    private val activityViewModel by activityViewModels<ActivityViewModel> { viewModelFactory }

    private val args: AddeditEntryFragmentArgs by navArgs()
    private lateinit var binding: AddeditEntryFragmentBinding
    private lateinit var adapter : CustomFieldsAdapter

    override fun onAttach(context: Context) {
        super.onAttach(context)
        val parentActivity: Activity = context as Activity
        val app: CipherPassApp = parentActivity.application as CipherPassApp
        app.appComponent.addEntryComponent().create().inject(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //a fost nevoie să mut aici populate() deoarece onCreateView() este apelată la întoarcerea
        //din backstack și suprascrie icoana setată de SetIconFragment.
        //https://bricolsoftconsulting.com/state-preservation-in-backstack-fragments/
        _viewModel.populate(args.entryId)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.addedit_entry_fragment, container, false)
        binding = AddeditEntryFragmentBinding.bind(root).apply {
            viewModel = _viewModel
        }

        binding.lifecycleOwner = this.viewLifecycleOwner
        setHasOptionsMenu(true)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setupPasswordFields(binding.passLayout, arrayOf(binding.pass, binding.repass))
        setupFab()
        setupNavigation()
        setupViewModelToasts()
        setupEditTextListeners(arrayOf(binding.name, binding.username, binding.pass,
            binding.repass, binding.url, binding.comment ))
        setupReceiveIcon()
        setupAddNewField()
        setupCustomFieldsRecycler()
        handleBackPress()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.addedit_menu, menu)
    }

    //https://stackoverflow.com/questions/57635268/how-to-handle-up-button-inside-fragment-using-navigation-components
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
       return when(item.itemId) {
            R.id.close -> {
                findNavController().popBackStack()
                true
            }
           else -> { //cînd apasă pe săgeata back. Nu am cum să determin itemId-ul.
               backWhenDirty()
               true
           }
        }
    }

    private fun setupNavigation() {
        activityViewModel.unauthorized.observe(viewLifecycleOwner,
            EventObserver {
                if (it)
                    findNavController().navigate(
                        AddeditEntryFragmentDirections.actionAddeditEntryFragmentToAuthenticateFragment()
                    )
            })

        _viewModel.saveResult.observe(viewLifecycleOwner, EventObserver {
            val imm: InputMethodManager =
                requireActivity().getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(requireView().windowToken, 0)

            if (it == INSERT_SUCCES)
                findNavController().navigate(
                    AddeditEntryFragmentDirections.actionAddeditEntryFragmentToEntriesListFragment()
                )

            else if(it == EDIT_SUCCESS)
                findNavController().popBackStack()
        })

        binding.setIcon.setOnClickListener {
            findNavController().navigate(AddeditEntryFragmentDirections.
               actionAddeditEntryFragmentToSetIconFragment())
        }
    }

    private fun setupFab() {
        binding.save.setOnClickListener {
            if(!_viewModel.isDirty()) {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.addedit_nochange),
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            if (binding.name.text!!.isBlank()) {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.addedit_name_empty),
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            if (binding.pass.text!!.toString() != binding.repass.text!!.toString()) {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.addedit_pass_nomatch),
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            _viewModel.saveEntry(
                binding.name.text.toString(),
                if (binding.username.text!!.isBlank()) null else binding.username.text.toString(),
                if (binding.pass.text!!.isBlank()) null else binding.pass.text.toString(),
                if (binding.url.text!!.isBlank()) null else binding.url.text.toString(),
                if (binding.comment.text!!.isBlank()) null else binding.comment.text.toString()
            )
        }
    }

    private fun setupEditTextListeners(editTexts : Array<TextView>) {
        for(editText in editTexts) {
            editText.setOnFocusChangeListener { _, hasFocus ->
                if(hasFocus)
                    editText.tag = "got-focus"
            }

            editText.addTextChangedListener(
                afterTextChanged = {
                    if(editText.tag == "got-focus")
                        _viewModel.setDirty(true)
            })
        }
    }

    private fun backWhenDirty() {
        fun goUp() {
            //Aici am încerat întîi să folosesc navigația clasică cu direcții, etc. Rezultatul era
            //că eram prins într-un ciclu infinit între addedit și detail.
            findNavController().popBackStack()
        }
        if (_viewModel.isDirty())
            MaterialDialog(requireContext()).show {
                title(R.string.confirm_discard_title)
                message(R.string.confirm_discard_message)
                positiveButton {
                    goUp()
                }
                negativeButton { }
            }
        else
            goUp()
    }

    private fun handleBackPress() {
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            backWhenDirty()
        }
    }

    private fun setupReceiveIcon() {
        parentFragmentManager.setFragmentResultListener(SET_ICON_REQUEST_KEY,
            viewLifecycleOwner,  { _: String, bundle: Bundle ->
                _viewModel.setIcon(bundle.getString(SET_ICON_BUNDLE_KEY, KEY_DRAWABLE_NAME))
                _viewModel.setDirty(true)
        })
    }

    private fun setupAddNewField() {
        binding.addNewField.setOnClickListener {
            MaterialDialog(requireContext()).show {
                customView(R.layout.add_custom_field_dialog)
                title(R.string.new_field_input_title)
                onPreShow { dialog ->
                   disablePositiveWhenEmpty(dialog, R.id.field_name_input)
                }
                positiveButton(android.R.string.ok) { dialog ->
                    val text = dialog.findViewById<EditText>(R.id.field_name_input).text.toString()
                    val isProtected = dialog.findViewById<CheckBox>(R.id.protected_field).isChecked
                    _viewModel.addCustomField(text, isProtected)
                    _viewModel.setDirty(true)
                }
                negativeButton(android.R.string.cancel)
            }
        }
    }

    private fun setupCustomFieldsRecycler() {
        adapter = CustomFieldsAdapter(this)
        binding.customFields.adapter = adapter
        binding.customFields.isNestedScrollingEnabled = false
        _viewModel.customFields.observe(viewLifecycleOwner, Observer {
            adapter.submitList(it)
            adapter.notifyDataSetChanged()
            //Se pare că dacă recycler-ul nu e conectat la o bază de date submitList() nu prea are efect.
            //Deocamdată nu stau să investighez workarounduri. Fallback la notifyDataSetChanged().
            //https://stackoverflow.com/questions/49726385/listadapter-not-updating-item-in-recyclerview
        })
    }

    override fun saveField(position : Int, value: String) {
        _viewModel.saveCustomField(position, value)
        _viewModel.setDirty(true)
    }

    override fun manageDeletion(position: Int) {
        MaterialDialog(requireContext()).show {
            title(R.string.delete_custom_field_title)
            message(R.string.delete_custom_field_message)
            positiveButton {
                _viewModel.deleteCustomField(position)
                _viewModel.setDirty(true)
            }
            negativeButton {}
        }
    }

    override fun manageEdit(position: Int, item : CustomField) {
        MaterialDialog(requireContext()).show {
            title(R.string.edit_custom_field)
            customView(R.layout.add_custom_field_dialog)
            onPreShow { dialog ->
                disablePositiveWhenEmpty(dialog, R.id.field_name_input)
                val fieldName = dialog.findViewById<EditText>(R.id.field_name_input)
                fieldName.setText(item.fieldName)
                val isProtected = dialog.findViewById<CheckBox>(R.id.protected_field)
                isProtected.isChecked = item.isProtected
            }
            positiveButton(android.R.string.ok) { dialog ->
                val fieldName = dialog.findViewById<EditText>(R.id.field_name_input).text.toString()
                val isProtected = dialog.findViewById<CheckBox>(R.id.protected_field).isChecked
                _viewModel.editCustomField(position, fieldName, isProtected)
                _viewModel.setDirty(true)
            }

            negativeButton {}
        }
    }

    private fun setupViewModelToasts() {
        _viewModel.toastMessages.observe(viewLifecycleOwner, EventObserver {
            Toast.makeText(requireContext(), getString(it),
                    Toast.LENGTH_SHORT).show()
        })
    }
}