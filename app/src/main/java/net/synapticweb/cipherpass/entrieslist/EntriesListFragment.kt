/*
 * Copyright (C) 2021 Eugen Rădulescu <synapticwebb@gmail.com>
 * This file is part of the CipherPass project ( https://github.com/synapticweb/CipherPass ).
 * See the LICENSE file in the project root for license terms.
 */

package net.synapticweb.cipherpass.entrieslist

import android.app.Activity
import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.SearchView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.list.listItemsSingleChoice
import net.synapticweb.cipherpass.*
import net.synapticweb.cipherpass.databinding.EntriesListFragmentBinding
import net.synapticweb.cipherpass.util.EventObserver
import net.synapticweb.cipherpass.util.LastBackPress
import net.synapticweb.cipherpass.util.PrefWrapper
import net.synapticweb.cipherpass.util.handleBackPressed
import org.matomo.sdk.extra.TrackHelper
import javax.inject.Inject

class EntriesListFragment : Fragment() {
    @Inject
    lateinit var viewModelFactory : ViewModelProvider.Factory

    private val _viewModel by viewModels<EntriesListViewModel> { viewModelFactory }
    private val activityViewModel by activityViewModels<ActivityViewModel> {viewModelFactory}
    private lateinit var binding : EntriesListFragmentBinding
    private lateinit var adapter: EntriesAdapter
    private lateinit var searchPopup : SearchPopup
    private lateinit var createJsonFileLauncher : ActivityResultLauncher<Intent>
    private lateinit var openJsonFileLauncher: ActivityResultLauncher<Intent>
    private val lastBackPress = LastBackPress()

    override fun onAttach(context: Context) {
        super.onAttach(context)
        val parentActivity : Activity = context as Activity
        val app : CipherPassApp = parentActivity.application as CipherPassApp
        app.appComponent.entriesListComponent().create().inject(this)

        createJsonFileLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult())
        {
            if(it.resultCode == Activity.RESULT_OK) {
                it.data?.data?.let { uri ->
                    _viewModel.exportJson(uri)
                }
            }
        }

        openJsonFileLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {
            if(it.resultCode == Activity.RESULT_OK) {
                it.data?.data?.let { uri ->
                    _viewModel.readJsonData(uri)
                }
            }
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = EntriesListFragmentBinding.inflate(inflater, container, false)

        _viewModel.entries.observe(viewLifecycleOwner, {
            binding.noEntriesText.visibility = if(it.isEmpty())
                View.VISIBLE else View.GONE
            adapter.submitList(it)
        })

        setHasOptionsMenu(true)

        val prefWrapper = PrefWrapper.getInstance(requireContext())
        val acceptsTracking = prefWrapper.getBoolean(getString(R.string.allow_usage_data_key)) == true

        if(!BuildConfig.DEBUG && acceptsTracking) {
            val tracker = (requireActivity().application as CipherPassApp).getTracker()
            TrackHelper.track().download().version(BuildConfig.VERSION_NAME).with(tracker)
            TrackHelper.track().screen(requireActivity()).with(tracker)
        }
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setupRecycler()
        setupFab()
        setupNavigation()
        setupSearch()
        setupSerialize()
        handleBackPressed(lastBackPress)
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
                override fun onQueryTextChange(newText: String?): Boolean {
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

        val prefWrapper = PrefWrapper.getInstance(requireContext())
        if(prefWrapper.getString(getString(R.string.applock_key)) == getString(R.string.applock_nolock_value))
            menu.findItem(R.id.lock).isVisible = false
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId) {
            R.id.show_settings -> {
                findNavController().navigate(
                    EntriesListFragmentDirections.actionEntriesListFragmentToSettingsFragment()
                )
                true
            }
            R.id.sort -> {
                MaterialDialog(requireContext()).show {
                    val prefs = PrefWrapper.getInstance(requireContext())
                    val initialSel = prefs.getString(SORT_ORDER_KEY) ?: getString(R.string.sort_creation_desc_name)
                    val sortOrders = resources.getStringArray(R.array.sort_orders)

                    listItemsSingleChoice(
                        R.array.sort_order_names,
                        initialSelection = sortOrders.indexOf(initialSel)
                    ) { _, index, _ ->
                        prefs.setPrefSync(SORT_ORDER_KEY, sortOrders[index])
                        _viewModel.loadEntries()
                    }
                }
                false
            }
            R.id.lock -> {
               _viewModel.lockAndReauth()
                false
            }
            R.id.export_json -> {
                MaterialDialog(requireContext()).show {
                    title(R.string.warning_title)
                    message(R.string.warning_serialize)
                    negativeButton(android.R.string.cancel) {}

                    positiveButton(android.R.string.ok) {
                        createJsonFile()
                    }
                }
                false
            }
            R.id.import_json -> {
                chooseJsonFile()
                false
            }
            R.id.about -> {
                findNavController().navigate(
                    EntriesListFragmentDirections.actionEntriesListFragmentToAboutFragment()
                )
                false
            }
            else -> false
        }
    }

    private fun setupNavigation() {
        _viewModel.openEntryEvent.observe(viewLifecycleOwner, EventObserver {
            if (::searchPopup.isInitialized && searchPopup.isShowing)
                searchPopup.dismiss()

            findNavController().navigate(
                EntriesListFragmentDirections.actionEntriesListFragmentToEntryDetailFragment(it)
            )
        })

        activityViewModel.unauthorized.observe(viewLifecycleOwner,
            EventObserver {
                if (it)
                    findNavController().navigate(
                        EntriesListFragmentDirections.actionEntriesListFragmentToAuthenticateFragment()
                    )
            })

        activityViewModel.authorized.observe(viewLifecycleOwner, EventObserver {})

        _viewModel.unauthorized.observe(viewLifecycleOwner, EventObserver {
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
                DividerItemDecoration.VERTICAL
            )
        )
    }

    private fun setupSearch() {
        _viewModel.searchResults.observe(viewLifecycleOwner, EventObserver {
            if (::searchPopup.isInitialized && searchPopup.isShowing)
                searchPopup.dismiss()

            if (it.isNotEmpty()) {
                searchPopup = SearchPopup(requireContext(), _viewModel)
                searchPopup.apply {
                    setList(it)
                    //în lollipop nu apare fereastra. https://stackoverflow.com/a/50533895/6192350
                    contentView.measure(resources.displayMetrics.widthPixels,
                        resources.displayMetrics.heightPixels)
                    height = contentView.measuredHeight
                    showAsDropDown(requireActivity().findViewById(R.id.search))
                }
            }
        })
    }

    private fun createJsonFile() {
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/json"
            putExtra(Intent.EXTRA_TITLE, "cipherpass.json")
        }
        createJsonFileLauncher.launch(intent)
    }

    private fun chooseJsonFile() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "*/*"
        }
        openJsonFileLauncher.launch(intent)
    }

    private fun setupSerialize() {
        _viewModel.serializeResults.observe(viewLifecycleOwner, EventObserver {
            Toast.makeText(requireContext(), getString(it), Toast.LENGTH_SHORT).show()
        })

        _viewModel.finishedImport.observe(viewLifecycleOwner, EventObserver {
            Toast.makeText(requireContext(), getString(R.string.import_success), Toast.LENGTH_SHORT).show()
        })

        _viewModel.hasEntries.observe(viewLifecycleOwner, EventObserver {
            MaterialDialog(requireContext()).show {
                title(R.string.warning_title)
                message(R.string.replace_entries_title)
                positiveButton(R.string.yes) {
                    _viewModel.importEntries(true)
                }
                negativeButton(R.string.no) {
                    _viewModel.importEntries(false)
                }
            }
        })
    }
}