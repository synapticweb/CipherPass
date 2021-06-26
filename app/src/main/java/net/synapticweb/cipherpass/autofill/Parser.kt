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
                    FieldType.UNKNOWN
                )
                if(isUsername(viewNode))
                    nodeDescription.fieldType = FieldType.USERNAME
                if(isPassword(viewNode))
                    nodeDescription.fieldType = FieldType.PASSWORD

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

    private fun isUsername(node : ViewNode) : Boolean {
        if (node.autofillHints?.any {
                it == View.AUTOFILL_HINT_USERNAME ||
                        it == View.AUTOFILL_HINT_EMAIL_ADDRESS ||
                        it == View.AUTOFILL_HINT_PHONE
            } == true)
            return true


        val pattern = Regex("user|email|e-mail|phone|tel", RegexOption.IGNORE_CASE)
        val properties = listOf(node.hint, node.idEntry, node.contentDescription, node.text)

        properties.forEach {
            it?.let {
                if (pattern.containsMatchIn(it)) {
                    return true
                }
            }
        }

        return false
    }

    private fun isPassword(node : ViewNode) : Boolean {
        if (node.autofillHints?.any {
                it == View.AUTOFILL_HINT_PASSWORD
            } == true) {
            return true
        }

        var pattern = Regex("pass", RegexOption.IGNORE_CASE)
        val relevantProperties =
            listOf(node.hint, node.idEntry, node.contentDescription, node.text)

        relevantProperties.forEach {
            it?.let {
                if (pattern.containsMatchIn(it))
                    return true
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
            pattern = Regex("search|find|query", RegexOption.IGNORE_CASE)
            relevantProperties.forEach {
                it?.let {
                    if (pattern.containsMatchIn(it))
                        return false

                }
            }

            return true
        }

        if (node.htmlInfo?.attributes?.any {
                it.first == "type" && it.second == "password"
            } == true)
            return true

        return false
    }

}