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
    GENERIC_LOGINID,
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

        removeUnkownNodes(clientData)
        return clientData
    }


    private fun traverseNode(viewNode : ViewNode, clientData: ClientData) {
        parseWebDomain(viewNode, clientData)

        viewNode.autofillId?.let {
            if(shouldDescribe(viewNode)) {
                val node = NodeDescription(
                    it,
                    getFieldType(viewNode)
                )
                clientData.nodes.add(node)
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

        val patterns = mapOf(FieldType.USERNAME to Regex("username", RegexOption.IGNORE_CASE),
            FieldType.PHONE to Regex("phone|tel", RegexOption.IGNORE_CASE),
            FieldType.EMAIL to Regex("e-mail|email", RegexOption.IGNORE_CASE),
            FieldType.PASSWORD to Regex("password|passphrase|passwd", RegexOption.IGNORE_CASE))

        val negativePatterns = Regex("search|find|query|url|enter text", RegexOption.IGNORE_CASE)

        for(type in FieldType.values()) {
            if (node.autofillHints?.any { hint ->
                    hint == autofillHints[type]
                } == true)
                return type

            properties.forEach {
                it?.let {
                    if (patterns[type]?.containsMatchIn(it) == true) { //pattern[unkown] dă null
                        return if(!negativePatterns.containsMatchIn(it))
                            type
                        else
                            FieldType.UNKNOWN
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
            //în chrome (brave), cînd se adaugă un bookmark, cîmpul ptr url are hint=URL și inpuType
            //conține TYPE_NUMBER_VARIATION_PASSWORD | TYPE_CLASS_TEXT.
            //Cîmpul pentru numele bookmarkului are inputType=TYPE_TEXT_FLAG_AUTO_CORRECT
            //| TYPE_TEXT_FLAG_CAP_SENTENCES | TYPE_CLASS_TEXT
            properties.forEach {
                it?.let {
                    if (negativePatterns.containsMatchIn(it))
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

    private fun removeUnkownNodes(clientData: ClientData) {
        val loginIds = clientData.nodes.filter { it.isLoginId }
        val passwds = clientData.nodes.filter { it.isPassword }

        //dacă un loginId este urmat de un cîmp care nu e parolă (inclusiv dacă e ultimul în listă)
        //,atunci probabil că nu e loginId. Singura excepție de la această regulă este cînd există
        // un singur nod în listă și acela e loginid.
        if(clientData.nodes.size > 1) {
            for (loginId in loginIds) {
                val currentIndex = clientData.nodes.indexOf(loginId)
                try {
                    val next = clientData.nodes[currentIndex + 1]
                    if(next.fieldType != FieldType.PASSWORD)
                        clientData.nodes[currentIndex].fieldType = FieldType.UNKNOWN
                }
                catch (e : IndexOutOfBoundsException) { //e ultimul și deci nu e urmat de parolă
                    clientData.nodes[currentIndex].fieldType = FieldType.UNKNOWN
                }
            }
        }

        //dacă o parolă e precedată de un cîmp necunoscut, atunci acela probabil e un loginId.
        for(passwd in passwds) {
            val currentIndex = clientData.nodes.indexOf(passwd)
            if(currentIndex > 0) {
                val previous = clientData.nodes[currentIndex - 1]
                if(previous.fieldType == FieldType.UNKNOWN)
                    previous.fieldType = FieldType.GENERIC_LOGINID
            }
        }

       clientData.nodes = clientData.nodes.filter {
           it.fieldType != FieldType.UNKNOWN
       }.toMutableList()
    }
}