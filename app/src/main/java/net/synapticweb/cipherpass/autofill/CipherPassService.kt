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
import android.util.Log
import android.view.autofill.AutofillId
import android.view.autofill.AutofillValue
import android.widget.RemoteViews
import androidx.annotation.RequiresApi
import kotlinx.coroutines.*
import mozilla.components.lib.publicsuffixlist.PublicSuffixList
import net.synapticweb.cipherpass.APP_TAG
import net.synapticweb.cipherpass.CipherPassApp
import net.synapticweb.cipherpass.R
import net.synapticweb.cipherpass.data.Repository
import net.synapticweb.cipherpass.data.UnencryptedDatabase
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
        if (!shouldGenerateResponse(clientData))
            return null

        val datasets = generateDatasets(clientData)
        for (dataset in datasets)
            builder.addDataset(dataset)

        addAuthDataset(clientData, builder)
        return builder.build()
    }

    private fun shouldGenerateResponse(clientData: ClientData) : Boolean {
        if(isClientIgnored(clientData) )
            return false

//după operațiile din Parser::postParsingProcess, dacă găsim 1 parolă sau 1 username
//(dacă e un username fără parolă atunci e un username izolat) putem să generăm un răspuns.
        return clientData.nodes.any {
            it.fieldType == FieldType.PASSWORD
        } || clientData.nodes.any {
            it.fieldType == FieldType.USERNAME //o idee de test: username-ul este izolat
        }
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
        val loginIdType = clientData.loginIdType
        //idee de test: nr de loginIds = nr parole
        val loginIds = clientData.nodes.filter { it.isLoginId }
        val passwds = clientData.nodes.filter { it.isPassword }

        if((loginIds.size > 1 || passwds.size > 1) &&
            loginIds.size != passwds.size) {
            Log.e(APP_TAG, "Number of loginIds is not equal with number of passwds.")
            return datasets
        }

        if(!repository.isUnlocked() || loginIdType == null)
            return datasets

        fun entriesToDatasets(searchToken : String) {
            val entries = runBlocking(Dispatchers.IO) {
                repository.queryDb(listOf(searchToken))
            }

            for(entry in entries) {
                if(entry.username == null && entry.password == null)
                    continue
                if(loginIdType == FieldType.EMAIL && !entry.isUsernameEmail)
                    continue
                if(loginIdType == FieldType.PHONE && !entry.isUsernamePhone)
                    continue

                val presentation = RemoteViews(packageName, R.layout.autofill_service_list)
                presentation.setTextViewText(R.id.autofill_dataset_title, entry.entryName)
                val builder = Dataset.Builder()

                for(loginId in loginIds)
                    entry.username?.let {
                        builder.setValue(
                            loginId.autofillId,
                            AutofillValue.forText(it),
                            presentation
                        )
                    }

                for(passwd in passwds)
                    entry.password?.let {
                        builder.setValue(
                            passwd.autofillId,
                            AutofillValue.forText(entry.password),
                            presentation
                        )
                    }
                datasets.add(builder.build())
            }
        }

        if(clientData.webDomain.isNotEmpty()) { //avem de-a face cu o pagină web, nu ne va interesa packetul care provine de la browser
            val nameToSearch = parseWebDomain(clientData.webDomain.toString())
            entriesToDatasets(nameToSearch)
            return  datasets
        }

        //e vorba de o aplicație, căutăm după pachet.
        val nameToSearch = parsePackageName(clientData.packageName)
        entriesToDatasets(nameToSearch)
        return datasets
    }

    private fun addAuthDataset(clientData: ClientData, builder: FillResponse.Builder) {
        val presentation = RemoteViews(packageName, R.layout.autofill_service_list)
        presentation.setTextViewText(R.id.autofill_dataset_title, getString(R.string.autofill_auth_title))

        val datasetBuilder = Dataset.Builder(presentation)
        val authIntent = Intent(this, AutofillActivity::class.java).apply {
            putExtra(CLIENT_DOMAIN_NAME, clientData.webDomain.toString())
        }
        val intentSender : IntentSender = PendingIntent.getActivity(
            this,
            1001,
            authIntent,
            PendingIntent.FLAG_CANCEL_CURRENT
        ).intentSender

        datasetBuilder.setAuthentication(intentSender)

        for(node in clientData.nodes) {
            datasetBuilder.setValue(node.autofillId, AutofillValue.forText("placeholder"))
        }

        builder.addDataset(datasetBuilder.build())
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

    private fun parsePackageName(packageName : String) : String {
        var segments = packageName.split(".")
        if(segments.size == 1)
            return packageName

        segments = segments.reversed()
        val domain = segments.joinToString(".")
        val suffixList = PublicSuffixList(applicationContext)
        var noTld : String
        runBlocking {
            noTld = suffixList.stripPublicSuffix(domain).await()
        }
        segments = noTld.split(".")

        return if(segments.size > 1)
            segments.last()
        else
            segments.first()
    }

}