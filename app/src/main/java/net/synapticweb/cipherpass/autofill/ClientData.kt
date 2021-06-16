/*
 * Copyright (C) 2021 Eugen Rădulescu <synapticwebb@gmail.com>
 * This file is part of the CipherPass project ( https://github.com/synapticweb/CipherPass ).
 * See the LICENSE file in the project root for license terms.
 */

package net.synapticweb.cipherpass.autofill

import android.os.Build
import android.text.InputType
import android.view.View
import android.view.ViewStructure
import android.view.autofill.AutofillId
import androidx.annotation.RequiresApi

class ClientData {
    val nodes : MutableList<NodeDescription> = ArrayList()
    val webDomain = StringBuilder()
    var packageName : String? = null

    @RequiresApi(Build.VERSION_CODES.O)
    fun getUsernameAndPasswdIds() : Set<AutofillId> {
        val set = mutableSetOf<AutofillId>()
        var previous : NodeDescription? = null
        for(node in nodes) {
            if(isUsername(node))
                set.add(node.autofillId)

            if(isPassword(node)) {
                set.add(node.autofillId)
                if(previous != null)
                    set.add(previous.autofillId)
            }
            previous = node
        }
        return set
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun isPassword(node : NodeDescription) : Boolean {
        if(node.autofillHints?.any {
                it == View.AUTOFILL_HINT_PASSWORD
            } == true)
            return true

        var pattern = Regex("password|passphrase", RegexOption.IGNORE_CASE)
        val relevantProperties = listOf(node.hint, node.idEntry, node.contentDescription)

        relevantProperties.forEach {
            it?.let {
                if (pattern.containsMatchIn(it)) {
                    return true
                }
            }
        }

        if((node.inputType or InputType.TYPE_TEXT_VARIATION_PASSWORD) ==
                node.inputType ||
                (node.inputType or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD) ==
                node.inputType ||
                (node.inputType or InputType.TYPE_NUMBER_VARIATION_PASSWORD) ==
                node.inputType ||
                node.inputType or InputType.TYPE_TEXT_VARIATION_WEB_PASSWORD ==
                node.inputType ) {

            //edittextul pentru căutare în browsere are inputtype = password...
            pattern = Regex("search|find|query", RegexOption.IGNORE_CASE)
            relevantProperties.forEach {
                it?.let {
                    if (pattern.containsMatchIn(it)) {
                        return false
                    }
                }
            }

            return true
        }

        if(node.htmlInfo?.attributes?.any {
                it.first == "type" && it.second == "password"
            } == true)
                return true

        return false
    }

    private fun isUsername(node : NodeDescription) : Boolean {
        if(node.autofillHints?.any {
                it == View.AUTOFILL_HINT_USERNAME ||
                        it == View.AUTOFILL_HINT_EMAIL_ADDRESS ||
                        it == View.AUTOFILL_HINT_PHONE
            } == true)
            return true

        val pattern = Regex("username|email|e-mail|phone|tel", RegexOption.IGNORE_CASE)
        val properties = listOf(node.hint, node.idEntry, node.contentDescription)

        properties.forEach {
            it?.let {
                if (pattern.containsMatchIn(it)) {
                    return true
                }
            }
        }

        return false
    }

    data class NodeDescription(
        val autofillId : AutofillId,
        val autofillHints : List<String>?,
        val hint : String?,
        val autofillType : Int,
        val inputType : Int,
        val idEntry : String?,
        val contentDescription : CharSequence?,
        val htmlInfo: ViewStructure.HtmlInfo?
    )
}