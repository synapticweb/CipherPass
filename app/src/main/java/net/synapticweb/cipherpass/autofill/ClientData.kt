/*
 * Copyright (C) 2021 Eugen Rădulescu <synapticwebb@gmail.com>
 * This file is part of the CipherPass project ( https://github.com/synapticweb/CipherPass ).
 * See the LICENSE file in the project root for license terms.
 */

package net.synapticweb.cipherpass.autofill

class ClientData {
    var nodes : MutableList<NodeDescription> = mutableListOf()
    val webDomain = StringBuilder()
    var packageName : String = ""

    val loginIdType : FieldType?
        get() {
            val loginIds = nodes.filter { it.isLoginId }
            var previousType : FieldType? = null
            for(loginId in loginIds) {
                previousType?. let {
                    if (loginId.fieldType != it)
                        return null
                }
                previousType = loginId.fieldType
            }
            return previousType
        }
}