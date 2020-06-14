package net.synapticweb.passman.secretslist

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import net.synapticweb.passman.PmApp
import net.synapticweb.passman.R
import javax.inject.Inject

class SecretsListFragment : Fragment() {
    @Inject
    lateinit var viewModel : SecretsListViewModel

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
        return inflater.inflate(R.layout.secrets_list_fragment, container, false)
    }
}