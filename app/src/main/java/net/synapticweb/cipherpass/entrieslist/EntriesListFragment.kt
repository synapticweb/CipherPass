package net.synapticweb.cipherpass.entrieslist

import android.app.Activity
import android.app.SearchManager
import android.content.Context
import android.os.Bundle
import android.view.*
import android.widget.SearchView
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import javax.inject.Inject
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.DividerItemDecoration
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.list.listItemsSingleChoice
import net.synapticweb.cipherpass.*
import net.synapticweb.cipherpass.databinding.EntriesListFragmentBinding
import net.synapticweb.cipherpass.util.EventObserver
import net.synapticweb.cipherpass.util.PrefWrapper
import net.synapticweb.cipherpass.util.handleBackPressed

class EntriesListFragment : Fragment() {
    @Inject
    lateinit var viewModelFactory : ViewModelProvider.Factory

    private val _viewModel by viewModels<EntriesListViewModel> { viewModelFactory }
    private val lockState by activityViewModels<LockStateViewModel> {viewModelFactory}
    private lateinit var binding : EntriesListFragmentBinding
    private lateinit var adapter: EntriesAdapter
    private lateinit var searchPopup : SearchPopup

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
        binding = EntriesListFragmentBinding.inflate(inflater, container, false)

        _viewModel.entries.observe(viewLifecycleOwner, Observer {
            adapter.submitList(it)
        })

        setHasOptionsMenu(true)

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setupRecycler()
        setupFab()
        setupNavigation()
        setupSearch()
        handleBackPressed(lockState)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.entries_list_menu, menu)

        val searchManager = requireActivity().getSystemService(Context.SEARCH_SERVICE)
                as SearchManager
        (menu.findItem(R.id.search).actionView as SearchView).apply {
            setSearchableInfo(searchManager.getSearchableInfo(requireActivity().componentName))
            //https://stackoverflow.com/questions/18063103/searchview-in-optionsmenu-not-full-width
            maxWidth = Integer.MAX_VALUE

            setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextChange(newText : String?): Boolean {
                    newText?.apply {
                        _viewModel.search(newText)
                    }
                    return true
                }

                override fun onQueryTextSubmit(p0: String?): Boolean {
                    TODO("Not yet implemented")
                }
            })
        }

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId) {
            R.id.show_settings -> {
                findNavController().navigate(EntriesListFragmentDirections.
                    actionEntriesListFragmentToSettingsFragment())
                true
            }
            R.id.sort -> {
                MaterialDialog(requireContext()).show {
                    val prefs = PrefWrapper.getInstance(requireContext())
                    val initialSel = prefs.getString(SORT_ORDER_KEY) ?: SORT_CREATION_DESC
                    val sortOrders = resources.getStringArray(R.array.sort_orders)

                    listItemsSingleChoice(R.array.sort_order_names,
                        initialSelection = sortOrders.indexOf(initialSel)) {_, index, _ ->
                        prefs.setPrefSync(SORT_ORDER_KEY, sortOrders[index])
                       _viewModel.loadEntries()
                    }
                }
                false
            }
            else -> false
        }
    }

    private fun setupNavigation() {
        _viewModel.openEntryEvent.observe(viewLifecycleOwner, EventObserver {
            if(::searchPopup.isInitialized && searchPopup.isShowing)
                searchPopup.dismiss()

            findNavController().navigate(
                EntriesListFragmentDirections.actionEntriesListFragmentToEntryDetailFragment(it)
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

    private fun setupRecycler() {
        adapter = EntriesAdapter(_viewModel)
        binding.entriesList.adapter = adapter
        binding.entriesList.addItemDecoration(
            DividerItemDecoration(
                context,
            DividerItemDecoration.VERTICAL)
        )
    }

    private fun setupSearch() {
        _viewModel.searchResults.observe(viewLifecycleOwner, EventObserver {
            if(::searchPopup.isInitialized && searchPopup.isShowing)
                searchPopup.dismiss()

            if(it.isNotEmpty()) {
                searchPopup = SearchPopup(requireContext(), _viewModel)
                searchPopup.setList(it)
                searchPopup.showAsDropDown(requireActivity().findViewById(R.id.search))
            }
        })
    }
}