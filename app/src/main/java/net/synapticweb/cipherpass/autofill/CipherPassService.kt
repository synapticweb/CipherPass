/*
 * Copyright (C) 2021 Eugen Rădulescu <synapticwebb@gmail.com>
 * This file is part of the CipherPass project ( https://github.com/synapticweb/CipherPass ).
 * See the LICENSE file in the project root for license terms.
 */

package net.synapticweb.cipherpass.autofill

import android.app.PendingIntent
import android.content.Intent
import android.content.IntentSender
import android.os.Build
import android.os.CancellationSignal
import android.service.autofill.*
import android.view.autofill.AutofillId
import android.widget.RemoteViews
import androidx.annotation.RequiresApi
import kotlinx.coroutines.*
import mozilla.components.lib.publicsuffixlist.PublicSuffixList
import net.synapticweb.cipherpass.CipherPassApp
import net.synapticweb.cipherpass.R
import net.synapticweb.cipherpass.data.Repository
import net.synapticweb.cipherpass.data.UnencryptedDatabase
import net.synapticweb.cipherpass.model.Entry
import javax.inject.Inject

const val CLIENT_DOMAIN_NAME = "client_domain_name"

@RequiresApi(Build.VERSION_CODES.O)
class CipherPassService : AutofillService() {
    @Inject
    lateinit var repository: Repository
    private lateinit var parser: Parser

    override fun onCreate() {
        super.onCreate()
        (application as CipherPassApp).appComponent.autofillComponent().create().inject(this)
    }

    override fun onFillRequest(
        request: FillRequest,
        cancellationSignal: CancellationSignal,
        callback: FillCallback
    ) {
        val fillContext = request.fillContexts
        val structure = fillContext[fillContext.size - 1].structure
        parser = Parser(structure)

        val response = generateResponse()
        callback.onSuccess(response)
    }

    override fun onSaveRequest(p0: SaveRequest, p1: SaveCallback) {

    }

    private fun generateResponse(): FillResponse? {
        val builder = FillResponse.Builder()
        val clientData = parser.parse()
        if (isClientIgnored(clientData) || clientData.nodes.isEmpty())
            return null

        val datasets = generateDatasets(clientData)
        for (dataset in datasets)
            builder.addDataset(dataset)

        addAuthDataset(clientData, builder)
        return builder.build()
    }


    private fun isClientIgnored(clientData: ClientData): Boolean {
        val job = Job()
        val scope = CoroutineScope(Dispatchers.IO + job)
        val dao = UnencryptedDatabase.getInstance(applicationContext, scope).dao

        var ignoredClients: List<String>
        runBlocking(Dispatchers.IO) {
            ignoredClients = dao.getIgnoredClientsSync().map { it.client }
        }
        job.cancel()

        if (ignoredClients.any {
                it == clientData.packageName || it == clientData.webDomain.toString()
            })
            return true

        return false
    }

    private fun generateDatasets(clientData: ClientData): List<Dataset> {
        val datasets = mutableListOf<Dataset>()
        if(!repository.isUnlocked())
            return datasets

        if(clientData.webDomain.isNotEmpty()) {
           val nameToSearch = parseWebDomain(clientData.webDomain.toString())
           var entries : List<Entry>
           runBlocking(Dispatchers.IO) {
               entries = repository.queryDb(listOf(nameToSearch))
           }

            for(entry in entries) {

            }
        }

        return  datasets
    }

    private fun addAuthDataset(clientData: ClientData, builder: FillResponse.Builder) {
        if(clientData.nodes.isEmpty())
            return

        val authPresentation = RemoteViews(packageName, R.layout.autofill_service_list)
        val authIntent = Intent(this, AutofillActivity::class.java).apply {
            putExtra(CLIENT_DOMAIN_NAME, clientData.webDomain.toString())
        }
        val intentSender: IntentSender = PendingIntent.getActivity(
            this,
            1001,
            authIntent,
            PendingIntent.FLAG_CANCEL_CURRENT
        ).intentSender

        val ids = mutableListOf<AutofillId>()
        clientData.nodes.forEach {
            ids.add(it.autofillId)
        }

        builder.setAuthentication(
            ids.toTypedArray(),
            intentSender, authPresentation
        )
    }

    private fun parseWebDomain(domain : String) : String {
        val suffixList = PublicSuffixList(applicationContext)
        var noTld : String

        runBlocking {
            noTld = suffixList.stripPublicSuffix(domain).await()
        }
        val segments = noTld.split(".")

        return if(segments.size > 1)
            segments.last()
        else
            segments.first()
    }
}