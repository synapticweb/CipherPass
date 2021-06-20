/*
 * Copyright (C) 2021 Eugen Rădulescu <synapticwebb@gmail.com>
 * This file is part of the CipherPass project ( https://github.com/synapticweb/CipherPass ).
 * See the LICENSE file in the project root for license terms.
 */

package net.synapticweb.cipherpass.autofill

import android.app.PendingIntent
import android.app.assist.AssistStructure
import android.app.assist.AssistStructure.ViewNode
import android.content.Intent
import android.content.IntentSender
import android.os.Build
import android.os.CancellationSignal
import android.service.autofill.*
import android.util.Log
import android.view.View
import android.widget.RemoteViews
import androidx.annotation.RequiresApi
import net.synapticweb.cipherpass.APP_TAG
import net.synapticweb.cipherpass.CipherPassApp
import net.synapticweb.cipherpass.data.Repository
import net.synapticweb.cipherpass.autofill.ClientData.NodeDescription
import javax.inject.Inject

const val CLIENT_DOMAIN_NAME = "client_domain_name"

@RequiresApi(Build.VERSION_CODES.O)
class CipherPassService : AutofillService() {
    @Inject
    lateinit var repository : Repository

    override fun onCreate() {
        super.onCreate()
        (application as CipherPassApp).appComponent.autofillComponent().create().inject(this)
    }

    override fun onFillRequest(request : FillRequest,
                               cancellationSignal : CancellationSignal,
                               callback : FillCallback) {
        val fillContext = request.fillContexts
        val structure = fillContext[fillContext.size - 1].structure

        val response = generateResponse(structure)
        callback.onSuccess(response)
    }

    override fun onSaveRequest(p0: SaveRequest, p1: SaveCallback) {

    }

    private fun generateResponse(structure : AssistStructure) : FillResponse? {
        val builder = FillResponse.Builder()
        val clientData = parseStructure(structure)

        if(repository.isUnlocked()) {
            Log.i(APP_TAG, "Unlocked")
        }
        else
            Log.i(APP_TAG, "Locked")

        val authPresentation = RemoteViews(packageName, android.R.layout.simple_list_item_1).apply {
            setTextViewText(android.R.id.text1, "requires authentication")
        }
        val authIntent = Intent(this, AutofillActivity::class.java).apply {
            putExtra(CLIENT_DOMAIN_NAME, clientData.webDomain.toString())
        }
        val intentSender : IntentSender = PendingIntent.getActivity(
            this,
            1001,
            authIntent,
            PendingIntent.FLAG_CANCEL_CURRENT
        ).intentSender

        return builder.setAuthentication(clientData.getAutofillIds(),
            intentSender, authPresentation)
            .build()
    }

    private fun parseStructure(structure : AssistStructure) : ClientData {
        val clientData = ClientData()
        clientData.clientPackage = structure.activityComponent.packageName

        val windowNodes: List<AssistStructure.WindowNode> =
            structure.run {
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
                        it, viewNode.autofillHints?.toList(),
                        viewNode.hint, viewNode.autofillType,
                        viewNode.inputType, viewNode.idEntry,
                        viewNode.contentDescription as String?
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
}