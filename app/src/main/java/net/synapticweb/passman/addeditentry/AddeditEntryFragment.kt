package net.synapticweb.passman.addeditentry

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import android.widget.Toast
import androidx.activity.addCallback
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.afollestad.materialdialogs.MaterialDialog
import net.synapticweb.passman.CryptoPassApp
import net.synapticweb.passman.LockStateViewModel
import net.synapticweb.passman.R
import net.synapticweb.passman.databinding.AddeditEntryFragmentBinding
import net.synapticweb.passman.util.EventObserver
import net.synapticweb.passman.util.setupPasswordFields
import javax.inject.Inject

class AddeditEntryFragment : Fragment() {
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private val _viewModel by viewModels<AddeditEntryViewModel> { viewModelFactory }
    private val lockState by activityViewModels<LockStateViewModel> { viewModelFactory }

    private val args: AddeditEntryFragmentArgs by navArgs()
    private lateinit var binding: AddeditEntryFragmentBinding

    private var dirty: Boolean = false

    override fun onAttach(context: Context) {
        super.onAttach(context)
        val parentActivity: Activity = context as Activity
        val app: CryptoPassApp = parentActivity.application as CryptoPassApp
        app.appComponent.addEntryComponent().create().inject(this)
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

        if (args.entryId != 0L)
            _viewModel.populate(args.entryId)

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setupPasswordFields(binding.passLayout, arrayOf(binding.pass, binding.repass))
        setupFab()
        setupNavigation()
        setupChangeListeners(arrayOf(binding.name, binding.id, binding.pass,
            binding.repass, binding.url, binding.comment ))
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
        lockState.unauthorized.observe(viewLifecycleOwner,
            EventObserver {
                if (it)
                    findNavController().navigate(
                        AddeditEntryFragmentDirections.actionAddeditEntryFragmentToAuthenticateFragment()
                    )
            })

        _viewModel.result.observe(viewLifecycleOwner, EventObserver {
            val imm: InputMethodManager =
                requireActivity().getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(requireView().windowToken, 0)

            if (it == R.string.addedit_save_ok)
                Toast.makeText(requireContext(), getString(R.string.addedit_save_ok),
                    Toast.LENGTH_SHORT).show()

            if (it == 0)
                findNavController().navigate(
                    AddeditEntryFragmentDirections.actionAddeditEntryFragmentToEntriesListFragment()
                )

            else if(it == R.string.addedit_save_ok)
                findNavController().popBackStack()
        })
    }

    private fun setupFab() {
        binding.save.setOnClickListener {
            if(!dirty) {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.addedit_nochange),
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            if (binding.name.text!!.isEmpty()) {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.addedit_name_empty),
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            if (binding.pass.text!!.isEmpty()) {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.addedit_pass_empty),
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
                if (binding.id.text!!.isBlank()) null else binding.id.text.toString(),
                binding.pass.text.toString(),
                if (binding.url.text!!.isBlank()) null else binding.url.text.toString(),
                if (binding.comment.text!!.isBlank()) null else binding.comment.text.toString(),
                args.entryId
            )
        }
    }

    //dirty este setat doar cînd userul scrie ceva. Dacă aș fi folosit addTextChangeTextListener {code}
    //ar fi fost apelată afterTextChanged, adică dirty ar fi fost setat la popularea cîmpurilor.
    private fun setupChangeListeners(editTexts : Array<TextView>) {
        for(editText in editTexts) {
            editText.addTextChangedListener(
                beforeTextChanged = { _,_,_,_ ->
                if(editText.text!!.isNotEmpty())
                    dirty = true
            })
        }
    }

    private fun backWhenDirty() {
        fun goUp() {
            dirty = false
            //Aici am încerat întîi să folosesc navigația clasică cu direcții, etc. Rezultatul era
            //că eram prins într-un ciclu infinit între addedit și detail.
            findNavController().popBackStack()
        }
        if (dirty)
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
}