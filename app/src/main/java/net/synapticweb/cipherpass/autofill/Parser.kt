/*
 * Copyright (C) 2021 Eugen Rădulescu <synapticwebb@gmail.com>
 * This file is part of the CipherPass project ( https://github.com/synapticweb/CipherPass ).
 * See the LICENSE file in the project root for license terms.
 */

package net.synapticweb.cipherpass.autofill

import android.app.assist.AssistStructure
import android.app.assist.AssistStructure.ViewNode;
import android.os.Build
import android.text.InputType
import android.util.Log
import android.view.View
import androidx.annotation.RequiresApi
import net.synapticweb.cipherpass.APP_TAG

enum class FieldType {
    USERNAME,
    EMAIL,
    PHONE,
    PASSWORD,
    UNKNOWN
}


@RequiresApi(Build.VERSION_CODES.O)
class Parser(private val lastStructure : AssistStructure) {

    fun parse() : ClientData {
        val clientData = ClientData()
        clientData.packageName = lastStructure.activityComponent.packageName

        val windowNodes: List<AssistStructure.WindowNode> =
            lastStructure.run {
                (0 until windowNodeCount).map { getWindowNodeAt(it) }
            }

        windowNodes.forEach { windowNode: AssistStructure.WindowNode ->
            val viewNode: ViewNode? = windowNode.rootViewNode
            viewNode?.let {
                traverseNode(it, clientData)
            }
        }

        return clientData
    }


    private fun traverseNode(viewNode : ViewNode, clientData : ClientData) {
        parseWebDomain(viewNode, clientData)
        viewNode.autofillId?.let {
            if(shouldDescribe(viewNode)) {
                val nodeDescription = NodeDescription(
                    it,
                    getFieldType(viewNode)
                )
                clientData.nodes.add(nodeDescription)
            }
        }

        val children: List<ViewNode> =
            viewNode.run {
                (0 until childCount).map { getChildAt(it) }
            }

        children.forEach { childNode: ViewNode ->
            traverseNode(childNode, clientData)
        }
    }

    private fun parseWebDomain(node : ViewNode, clientData: ClientData) {
        node.webDomain?.let {
            if(clientData.webDomain.isNotEmpty()) {
                if(it != clientData.webDomain.toString())
                    Log.i(APP_TAG, "Web domain: $it")
                else {}
            }
            else
                clientData.webDomain.append(it)
        }
    }


    private fun shouldDescribe(node : ViewNode) : Boolean {
        return node.autofillType == View.AUTOFILL_TYPE_TEXT
    }

    private fun getFieldType(node : ViewNode) : FieldType {
        val autofillHints = mapOf(FieldType.USERNAME to View.AUTOFILL_HINT_USERNAME,
                                FieldType.PHONE to View.AUTOFILL_HINT_PHONE,
                                FieldType.EMAIL to View.AUTOFILL_HINT_EMAIL_ADDRESS,
                                FieldType.PASSWORD to View.AUTOFILL_HINT_PASSWORD)

        val properties = mutableListOf(node.hint, node.idEntry, node.contentDescription, node.text)
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.P)
            properties.add(node.textIdEntry)

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
            properties.add(node.hintIdEntry)

        val patterns = mapOf(FieldType.USERNAME to Regex("username"),
            FieldType.PHONE to Regex("phone|tel"),
            FieldType.EMAIL to Regex("e-mail|email"),
            FieldType.PASSWORD to Regex("password|passphrase|passwd"))

        for(type in FieldType.values()) {
            if (node.autofillHints?.any { hint ->
                    hint == autofillHints[type]
                } == true)
                return type

            properties.forEach {
                it?.let {
                    if (patterns[type]?.containsMatchIn(it) == true) { //pattern[unkown] dă null
                        return type
                    }
                }
            }
        }

        if ((node.inputType or InputType.TYPE_TEXT_VARIATION_PASSWORD) ==
            node.inputType ||
            (node.inputType or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD) ==
            node.inputType ||
            (node.inputType or InputType.TYPE_NUMBER_VARIATION_PASSWORD) ==
            node.inputType ||
            node.inputType or InputType.TYPE_TEXT_VARIATION_WEB_PASSWORD ==
            node.inputType
        ) {

            //edittextul pentru căutare în browsere are inputtype = password...
            val pattern = Regex("search|find|query", RegexOption.IGNORE_CASE)
            properties.forEach {
                it?.let {
                    if (pattern.containsMatchIn(it))
                        return FieldType.UNKNOWN
                }
            }
            return FieldType.PASSWORD
        }

        if (node.htmlInfo?.attributes?.any {
                it.first == "type" && it.second == "password"
            } == true)
            return FieldType.PASSWORD

        return FieldType.UNKNOWN
    }

}