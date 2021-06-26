/*
 * Copyright (C) 2021 Eugen Rădulescu <synapticwebb@gmail.com>
 * This file is part of the CipherPass project ( https://github.com/synapticweb/CipherPass ).
 * See the LICENSE file in the project root for license terms.
 */

package net.synapticweb.cipherpass.autofill


import android.view.autofill.AutofillId

class NodeDescription(
    val autofillId : AutofillId,
    var fieldType : FieldType
) {
    val isUsername : Boolean
        get() = fieldType == FieldType.USERNAME

    val isPassword : Boolean
        get() = fieldType == FieldType.PASSWORD
}