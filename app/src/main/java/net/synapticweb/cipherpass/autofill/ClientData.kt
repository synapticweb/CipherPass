/*
 * Copyright (C) 2021 Eugen Rădulescu <synapticwebb@gmail.com>
 * This file is part of the CipherPass project ( https://github.com/synapticweb/CipherPass ).
 * See the LICENSE file in the project root for license terms.
 */

package net.synapticweb.cipherpass.autofill

import android.view.autofill.AutofillId

class ClientData {
    val nodes : MutableList<NodeDescription> = ArrayList()
    val webDomain = StringBuilder()
    var clientPackage : String? = null

    fun getAutofillIds() : Array<AutofillId> {
        val ids = arrayListOf<AutofillId>()
        for(node in nodes)
            ids.add(node.autofillId)

        return ids.toTypedArray()
    }

    class NodeDescription(
        val autofillId : AutofillId,
        val autofillHints : List<String>?,
        val hint : String?,
        val autofillType : Int,
        val inputType : Int,
        val idEntry : String?,
        val contentDescription : String?
    )
}