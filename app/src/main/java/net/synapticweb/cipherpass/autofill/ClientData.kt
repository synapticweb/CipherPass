/*
 * Copyright (C) 2021 Eugen Rădulescu <synapticwebb@gmail.com>
 * This file is part of the CipherPass project ( https://github.com/synapticweb/CipherPass ).
 * See the LICENSE file in the project root for license terms.
 */

package net.synapticweb.cipherpass.autofill

class ClientData {
    val nodes: MutableList<NodeDescription> = ArrayList()
    val webDomain = StringBuilder()
    var packageName: String? = null

    val hasInterestingNodes : Boolean
        get() = interestingNodes.isNotEmpty()

    private var _interestingNodes : List<NodeDescription>? = null
    val interestingNodes : List<NodeDescription>
        get() = _interestingNodes ?: run {
            val loginIds = nodes.filter { it.isLoginId }.toMutableList()
            val passwds = nodes.filter { it.isPassword }

            if (loginIds.isEmpty() && passwds.isEmpty()) {
                _interestingNodes = listOf()
                return listOf()
            }

            //dacă avem o parolă și niciun username vom considera că nodul care precede parola este username
            if (loginIds.isEmpty()) {
                val firstPass = passwds[0]
                val iterator = nodes.listIterator()

                var previous: NodeDescription? = null
                while (iterator.hasNext()) {
                    val current = iterator.next()

                    if (current.autofillId == firstPass.autofillId) {
                        if (previous != null) {  //dacă parola nu e cumva primul nod...
                            previous.fieldType = FieldType.USERNAME
                            loginIds.add(previous)
                        }

                        break //cînd găsim parola ne oprim, chiar dacă sîntem la începutul listei
                    }
                    previous = current
                }
            }
            _interestingNodes = loginIds + passwds
            return _interestingNodes as List<NodeDescription>
        }

}