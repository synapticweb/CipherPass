package net.synapticweb.passman.addeditcredential

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import net.synapticweb.passman.CryptoPassApp
import net.synapticweb.passman.LockStateViewModel
import net.synapticweb.passman.R
import net.synapticweb.passman.databinding.AddeditCredFragmentBinding
import net.synapticweb.passman.util.EventObserver
import net.synapticweb.passman.util.setupPasswordFields
import javax.inject.Inject

class AddeditCredFragment : Fragment() {
    @Inject
    lateinit var viewModelFactory : ViewModelProvider.Factory

    private val _viewModel by viewModels<AddeditCredViewModel> { viewModelFactory }
    private val lockState by activityViewModels<LockStateViewModel> {viewModelFactory}

    private val args : AddeditCredFragmentArgs by navArgs()
    private lateinit var binding: AddeditCredFragmentBinding

    override fun onAttach(context: Context) {
        super.onAttach(context)
        val parentActivity : Activity = context as Activity
        val app : CryptoPassApp = parentActivity.application as CryptoPassApp
        app.appComponent.addCredentialComponent().create().inject(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.addedit_cred_fragment, container, false)
        binding = AddeditCredFragmentBinding.bind(root).apply {
            viewModel = _viewModel
        }

        binding.lifecycleOwner = this.viewLifecycleOwner
        setupPasswordFields(binding.passLayout, arrayOf(binding.pass, binding.repass))
        setHasOptionsMenu(true)

        setupFab()

        lockState.unauthorized.observe(viewLifecycleOwner,
            EventObserver {
                if (it)
                    findNavController().navigate(
                        AddeditCredFragmentDirections.actionAddeditCredFragmentToAuthenticateFragment()
                    )
            })

        if(args.credentialId != 0L)
            _viewModel.populate(args.credentialId)

        _viewModel.working.observe(viewLifecycleOwner, Observer {
            if(!it)
                findNavController().navigate(
                    AddeditCredFragmentDirections.actionAddeditCredFragmentToCredListFragment()
                )
        })

        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.addedit_menu, menu)
    }


    private fun setupFab() {
        binding.save.setOnClickListener {
            if(binding.name.text!!.isEmpty()) {
                Toast.makeText(requireContext(), getString(R.string.addedit_name_empty), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if(binding.pass.text!!.isEmpty()) {
                Toast.makeText(requireContext(), getString(R.string.addedit_pass_empty), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if(binding.pass.text!!.toString() != binding.repass.text!!.toString()) {
                Toast.makeText(requireContext(), getString(R.string.addedit_pass_nomatch), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            _viewModel.saveCredential(binding.name.text.toString(),
                if(binding.id.text!!.isBlank()) null else binding.id.text.toString(),
                binding.pass.text.toString(),
                if(binding.url.text!!.isBlank()) null else binding.url.text.toString(),
                if(binding.comment.text!!.isBlank()) null else binding.comment.text.toString(),
                if(args.credentialId == 0L) null else args.credentialId
            )
        }
    }

}