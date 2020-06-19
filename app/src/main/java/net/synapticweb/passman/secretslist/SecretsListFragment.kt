package net.synapticweb.passman.secretslist

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import javax.inject.Inject
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import net.synapticweb.passman.*

class SecretsListFragment : Fragment() {
    @Inject
    lateinit var viewModelFactory : ViewModelProvider.Factory

    private val viewModel by viewModels<SecretsListViewModel> { viewModelFactory }
    private val lockState by activityViewModels<LockStateViewModel> {viewModelFactory}

    override fun onAttach(context: Context) {
        super.onAttach(context)
        val parentActivity : AppCompatActivity = context as AppCompatActivity
        val app : PmApp = parentActivity.application as PmApp
        app.appComponent.secretsListComponent().create().inject(this)
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.secrets_list_fragment, container, false)
        val insert = rootView.findViewById<Button>(R.id.insert_data)
        insert.setOnClickListener { viewModel.insertSecret() }


        viewModel.secrets.observe(viewLifecycleOwner, Observer {
            when  {
                it.isEmpty() -> rootView.findViewById<TextView>(R.id.secrets_list)?.text = "Empty list"

                else -> {
                    var text = ""
                    for(secret in it) {
                        text += (secret.accountId + " " + secret.password + "\n")
                    }
                    rootView.findViewById<TextView>(R.id.secrets_list)?.text = text
                }
            }
        })

        lockState.unauthorized.observe(viewLifecycleOwner, EventObserver {
            findNavController().navigate(SecretsListFragmentDirections.
                    actionSecretsListFragmentToAuthenticateFragment())
        })

        handleBackPressed(lockState)

        return rootView
    }
}