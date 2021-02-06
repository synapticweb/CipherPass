/*
 * Copyright (C) 2021 Eugen Rădulescu <synapticwebb@gmail.com>
 * This file is part of the CipherPass project ( https://github.com/synapticweb/CipherPass ).
 * See the LICENSE file in the project root for license terms.
 */

package net.synapticweb.cipherpass.util

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager

class PrefWrapper private constructor(context: Context) {
    private val settings: SharedPreferences =
        PreferenceManager.getDefaultSharedPreferences(context)

    companion object {
        @Volatile
        private var INSTANCE : PrefWrapper? = null

        fun getInstance(context: Context): PrefWrapper {
            var instance = INSTANCE
            if (instance == null) {
                instance = PrefWrapper(context)
                INSTANCE = instance
            }
            return instance
        }
    }

    fun setPref(key: String, value: Any) {
        val editor = settings.edit()
        when(value) {
            is String -> editor.putString(key, value)
            is Boolean -> editor.putBoolean(key, value)
            else -> throw IllegalArgumentException("No such preference type!")
        }
        editor.apply()
    }

    fun setPrefSync(key: String, value: Any): Boolean {
        val editor = settings.edit()
        when(value) {
            is String -> editor.putString(key, value)
            is Boolean -> editor.putBoolean(key, value)
            else -> throw IllegalArgumentException("No such preference type!")
        }
        return editor.commit()
    }

    fun getBoolean(key : String) : Boolean? {
        if (!settings.contains(key))
            return null
        return settings.getBoolean(key, false)
    }

    fun getString(key : String) : String? {
        if (!settings.contains(key))
            return null
        return settings.getString(key, null)
    }

    fun removePref(key: String) {
        val editor = settings.edit()
        if (settings.contains(key))
            editor.remove(key)
        editor.apply()
    }

    fun getAll() : Map<String, *> {
        return settings.all
    }

    fun restore(prefs : Map<String, *>) {
        val editor = settings.edit()
        editor.clear()
        for(pref in prefs) {
            if(pref.value is Boolean)
                editor.putBoolean(pref.key, pref.value as Boolean)
            else if(pref.value is String)
                editor.putString(pref.key, pref.value as String)
        }
        editor.apply()
    }
}