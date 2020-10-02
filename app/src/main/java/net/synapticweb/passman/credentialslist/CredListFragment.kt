package net.synapticweb.passman.credentialslist

import android.content.Context
import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import javax.inject.Inject
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.google.android.material.floatingactionbutton.FloatingActionButton
import net.synapticweb.passman.*
import net.synapticweb.passman.databinding.CredListFragmentBinding
import net.synapticweb.passman.util.EventObserver
import net.synapticweb.passman.util.handleBackPressed

class CredListFragment : Fragment() {
    @Inject
    lateinit var viewModelFactory : ViewModelProvider.Factory

    private val _viewModel by viewModels<CredListViewModel> { viewModelFactory }
    private val lockState by activityViewModels<LockStateViewModel> {viewModelFactory}
    private lateinit var binding : CredListFragmentBinding
    private lateinit var adapter: CredentialsAdapter

    override fun onAttach(context: Context) {
        super.onAttach(context)
        val parentActivity : AppCompatActivity = context as AppCompatActivity
        val app : CryptoPassApp = parentActivity.application as CryptoPassApp
        app.appComponent.credListComponent().create().inject(this)
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = CredListFragmentBinding.inflate(inflater, container, false).apply {
            viewModel = _viewModel
        }

        _viewModel.credentials.observe(viewLifecycleOwner, Observer {
            adapter.submitList(it)
        })

        lockState.unauthorized.observe(viewLifecycleOwner,
            EventObserver {
                if (it)
                    findNavController().navigate(
                        CredListFragmentDirections.actionCredListFragmentToAuthenticateFragment()
                    )
            })

        handleBackPressed(lockState)
        setHasOptionsMenu(true)

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setupAdapter()
        setupFab()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.cred_list_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId) {
            R.id.show_settings -> {
                findNavController().navigate(CredListFragmentDirections.
                    actionCredListFragmentToSettingsFragment())
                true
            }
            else -> false
        }
    }

    private fun setupFab() {
        activity?.findViewById<FloatingActionButton>(R.id.add_credential)?.let {
            it.setOnClickListener {
                val action = CredListFragmentDirections.
                actionCredListFragmentToAddeditCredFragment(
                    null,
                    resources.getString(R.string.new_entry)
                )
                findNavController().navigate(action)
            }
        }
    }

    private fun setupAdapter() {
        val viewModel = binding.viewModel
        if (viewModel != null) {
            adapter = CredentialsAdapter(viewModel)
            binding.credentialsList.adapter = adapter
        }
    }
}