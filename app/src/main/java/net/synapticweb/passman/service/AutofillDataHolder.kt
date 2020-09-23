package net.synapticweb.passman.service

import android.os.Build
import android.view.View
import androidx.annotation.RequiresApi
import net.synapticweb.passman.service.DatasetType.CREDENTIALS

enum class DatasetType {
    CREDENTIALS
}

class FieldInfo(
    val supportedHints : Array<String>
)

@RequiresApi(Build.VERSION_CODES.O)
val datasetsInfo = mapOf(
    CREDENTIALS to arrayOf(
        FieldInfo(
            arrayOf(
                View.AUTOFILL_HINT_EMAIL_ADDRESS,
                View.AUTOFILL_HINT_USERNAME
            )
        ),

        FieldInfo(arrayOf(View.AUTOFILL_HINT_PASSWORD))
    )
)


class AutofillDataHolder(
    private var type : DatasetType? = null,
    private var name : String? = null,
    private val data: MutableList<String> = mutableListOf()
) {

    fun putData(name: String, type : DatasetType, vararg fields : String) {
        this.type = type
        this.name = name
        for(i in fields.indices)
            data.add(fields[i])
    }

    fun getData() : List<String>? {
        if(data.isNotEmpty())
            return data
        return null
    }

    fun getType() : DatasetType? {
        return type
    }

    fun getName() : String? {
        return name
    }

    fun reset() {
        type = null
        name = null
        data.clear()
    }
}