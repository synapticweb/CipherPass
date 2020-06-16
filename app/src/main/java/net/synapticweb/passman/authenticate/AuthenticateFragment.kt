package net.synapticweb.passman.authenticate

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import net.synapticweb.passman.Authorizer
import net.synapticweb.passman.R

class AuthenticateFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.authenticate_fragment, container, false)
        val enterPass = rootView.findViewById<Button>(R.id.enter_pass)
        enterPass?.setOnClickListener {
            Authorizer.setPassphrase("vasile")
            findNavController().navigate(AuthenticateFragmentDirections.actionAuthenticateFragmentToSecretsListFragment())
        }

        return rootView
    }
}