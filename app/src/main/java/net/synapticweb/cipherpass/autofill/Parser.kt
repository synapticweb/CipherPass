/*
 * Copyright (C) 2021 Eugen Rădulescu <synapticwebb@gmail.com>
 * This file is part of the CipherPass project ( https://github.com/synapticweb/CipherPass ).
 * See the LICENSE file in the project root for license terms.
 */

package net.synapticweb.cipherpass.autofill

import android.app.assist.AssistStructure
import android.os.Build
import android.util.Log
import android.view.View
import androidx.annotation.RequiresApi
import net.synapticweb.cipherpass.APP_TAG

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
            val viewNode: AssistStructure.ViewNode? = windowNode.rootViewNode
            viewNode?.let {
                traverseNode(it, clientData)
            }
        }

        return clientData
    }

    private fun traverseNode(viewNode : AssistStructure.ViewNode, clientData : ClientData) {
        parseWebDomain(viewNode, clientData)
        viewNode.autofillId?.let {
            if(shouldDescribe(viewNode)) {
                val nodeDescription = ClientData.NodeDescription(
                    it,
                    viewNode.autofillHints?.toList(),
                    viewNode.hint,
                    viewNode.autofillType,
                    viewNode.inputType,
                    viewNode.idEntry,
                    viewNode.contentDescription,
                    viewNode.htmlInfo
                )
                clientData.nodes.add(nodeDescription)
            }
        }

        val children: List<AssistStructure.ViewNode> =
            viewNode.run {
                (0 until childCount).map { getChildAt(it) }
            }

        children.forEach { childNode: AssistStructure.ViewNode ->
            traverseNode(childNode, clientData)
        }
    }

    private fun parseWebDomain(node : AssistStructure.ViewNode, clientData: ClientData) {
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


    private fun shouldDescribe(node : AssistStructure.ViewNode) : Boolean {
        return node.autofillType == View.AUTOFILL_TYPE_TEXT
    }

}