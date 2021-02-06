package net.synapticweb.cipherpass.settings

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.preference.PreferenceViewHolder
import androidx.preference.SwitchPreference
import net.synapticweb.cipherpass.R



class CustomSwitchPreference(context: Context, attrs : AttributeSet) : SwitchPreference(context, attrs) {
    var privLinkListener : View.OnClickListener? = null
    @SuppressLint("RestrictedApi")
    override fun performClick(v : View) {
        super.performClick()
    }

    init {
        //putea fi folosit și android:layout în preferences.xml
        layoutResource = R.layout.privacy_switch_pref
    }

    override fun onBindViewHolder(holder: PreferenceViewHolder?) {
        super.onBindViewHolder(holder)
        val itemView = holder?.itemView
        //Avem o problemă pentru că dacă apasă pe privacy link sau în jur se dezactivează switch-ul.
        //Mai jos punem mai întîi un click listener dummy pentru toată preferința.
        itemView?.setOnClickListener {  }
        val widget = itemView?.findViewById<LinearLayout>(android.R.id.widget_frame)
        //apoi setăm click listenerul corect doar pentru widget. (vezi Preference::onBindViewHolder() )
        widget?.setOnClickListener { v -> performClick(v) }

        val privPol = itemView?.findViewById<TextView>(R.id.priv_policy_link)
        privPol?.setOnClickListener { privLinkListener?.onClick(it) }
        privPol?.paintFlags = Paint.UNDERLINE_TEXT_FLAG
    }
}