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
    val nodes: MutableList<NodeDescription> = ArrayList()
    val webDomain = StringBuilder()
    var packageName: String? = null

    fun getUsernameAndPasswdNodes() : List<NodeDescription>? {
        val usernames = nodes.filter { it.isUsername } as MutableList<NodeDescription>
        val passwds = nodes.filter { it.isPassword }

        if(usernames.size > 1 || passwds.size > 2 ||
            (usernames.isEmpty() && passwds.isEmpty()))
                return null

        //dacă avem o parolă și niciun username vom considera că nodul care precede parola este username
        if(usernames.isEmpty()) {
            val firstPass = passwds[0]
            val iterator = nodes.listIterator()

            while(iterator.hasNext()) {
                val current = iterator.next()
                if(current.autofillId == firstPass.autofillId) {
                    //deoarece next a dus pointerul în sus apelăm previous doar ca să coborîm pointerul
                    //la elem. curent. Apoi apelăm previousIndex pentru a obține indexul elementului
                    //precedent, pe care îl suspectăm a fi username.
                    iterator.previous()
                    val previous = nodes[iterator.previousIndex()]
                    previous.isUsername = true
                    usernames.add(previous)
                    break
                }
            }
        }

        return usernames + passwds
    }

    class NodeDescription(
        val autofillId: AutofillId,
        private val autofillHints: List<String>?,
        private val hint: String?,
        private val autofillType: Int,
        private val inputType: Int,
        private val idEntry: String?,
        private val contentDescription: CharSequence?,
        private val htmlInfo: ViewStructure.HtmlInfo?
    ) {

        private var _isUsername: Boolean? = null
        var isUsername: Boolean
            set(value) {
                _isUsername = value
            }
            get() {
                if (_isUsername == null) {
                    if (this.autofillHints?.any {
                            it == View.AUTOFILL_HINT_USERNAME ||
                                    it == View.AUTOFILL_HINT_EMAIL_ADDRESS ||
                                    it == View.AUTOFILL_HINT_PHONE
                        } == true) {
                        _isUsername = true
                        return true
                    }

                    val pattern = Regex("username|email|e-mail|phone|tel", RegexOption.IGNORE_CASE)
                    val properties = listOf(this.hint, this.idEntry, this.contentDescription)

                    properties.forEach {
                        it?.let {
                            if (pattern.containsMatchIn(it)) {
                                _isUsername = true
                                return true
                            }
                        }
                    }

                    _isUsername = false
                    return false
                } else
                    return _isUsername as Boolean
            }

        private var _isPassword: Boolean? = null
        var isPassword : Boolean
            set(value) {
                _isPassword = value
            }
            @RequiresApi(Build.VERSION_CODES.O)
            get() {
                if (_isPassword == null) {
                    if (this.autofillHints?.any {
                            it == View.AUTOFILL_HINT_PASSWORD
                        } == true) {
                        _isPassword = true
                        return true
                    }

                    var pattern = Regex("password|passphrase", RegexOption.IGNORE_CASE)
                    val relevantProperties =
                        listOf(this.hint, this.idEntry, this.contentDescription)

                    relevantProperties.forEach {
                        it?.let {
                            if (pattern.containsMatchIn(it)) {
                                _isPassword = true
                                return true
                            }
                        }
                    }

                    if ((this.inputType or InputType.TYPE_TEXT_VARIATION_PASSWORD) ==
                        this.inputType ||
                        (this.inputType or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD) ==
                        this.inputType ||
                        (this.inputType or InputType.TYPE_NUMBER_VARIATION_PASSWORD) ==
                        this.inputType ||
                        this.inputType or InputType.TYPE_TEXT_VARIATION_WEB_PASSWORD ==
                        this.inputType
                    ) {

                        //edittextul pentru căutare în browsere are inputtype = password...
                        pattern = Regex("search|find|query", RegexOption.IGNORE_CASE)
                        relevantProperties.forEach {
                            it?.let {
                                if (pattern.containsMatchIn(it)) {
                                    _isPassword = false
                                    return false
                                }
                            }
                        }

                        _isPassword = true
                        return true
                    }

                    if (this.htmlInfo?.attributes?.any {
                            it.first == "type" && it.second == "password"
                        } == true) {
                        _isPassword = true
                        return true
                    }

                    _isPassword = false
                    return false
                } else
                    return _isPassword as Boolean
            }

    }

}