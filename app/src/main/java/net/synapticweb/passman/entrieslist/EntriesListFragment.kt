package net.synapticweb.passman.entrieslist

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
import net.synapticweb.passman.*
import net.synapticweb.passman.databinding.EntriesListFragmentBinding
import net.synapticweb.passman.util.EventObserver
import net.synapticweb.passman.util.handleBackPressed

class EntriesListFragment : Fragment() {
    @Inject
    lateinit var viewModelFactory : ViewModelProvider.Factory

    private val _viewModel by viewModels<EntriesListViewModel> { viewModelFactory }
    private val lockState by activityViewModels<LockStateViewModel> {viewModelFactory}
    private lateinit var binding : EntriesListFragmentBinding
    private lateinit var adapter: EntriesAdapter

    override fun onAttach(context: Context) {
        super.onAttach(context)
        val parentActivity : AppCompatActivity = context as AppCompatActivity
        val app : CryptoPassApp = parentActivity.application as CryptoPassApp
        app.appComponent.entriesListComponent().create().inject(this)
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = EntriesListFragmentBinding.inflate(inflater, container, false).apply {
            viewModel = _viewModel
        }

        _viewModel.entries.observe(viewLifecycleOwner, Observer {
            adapter.submitList(it)
        })

        setHasOptionsMenu(true)

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setupAdapter()
        setupFab()
        setupNavigation()
        handleBackPressed(lockState)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.entries_list_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId) {
            R.id.show_settings -> {
                findNavController().navigate(EntriesListFragmentDirections.
                    actionEntriesListFragmentToSettingsFragment())
                true
            }
            else -> false
        }
    }

    private fun setupNavigation() {
        _viewModel.openEntryEvent.observe(viewLifecycleOwner, EventObserver {
            findNavController().navigate(
                EntriesListFragmentDirections.actionEntriesListFragmentToEntryDetailFragment(it.first, it.second)
            )
        })

        lockState.unauthorized.observe(viewLifecycleOwner,
            EventObserver {
                if (it)
                    findNavController().navigate(
                        EntriesListFragmentDirections.actionEntriesListFragmentToAuthenticateFragment()
                    )
            })
    }

    private fun setupFab() {
       binding.addEntry.let {
            it.setOnClickListener {
                val action = EntriesListFragmentDirections.actionEntriesListFragmentToAddeditEntryFragment(
                    0L,
                    resources.getString(R.string.new_entry)
                )
                findNavController().navigate(action)
            }
        }
    }

    private fun setupAdapter() {
        val viewModel = binding.viewModel
        if (viewModel != null) {
            adapter = EntriesAdapter(viewModel)
            binding.entriesList.adapter = adapter
        }
    }
}