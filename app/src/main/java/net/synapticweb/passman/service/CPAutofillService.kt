package net.synapticweb.passman.service

import android.app.assist.AssistStructure
import android.app.assist.AssistStructure.ViewNode
import android.os.Build
import android.os.CancellationSignal
import android.service.autofill.*
import android.view.autofill.AutofillId
import android.view.autofill.AutofillValue
import android.widget.RemoteViews
import androidx.annotation.RequiresApi
import androidx.collection.ArrayMap
import net.synapticweb.passman.CryptoPassApp

@RequiresApi(Build.VERSION_CODES.O)
class CPAutofillService : AutofillService() {
    var type : DatasetType? = null
    var name : String? = null
    var data : List<String>? = null

    override fun onFillRequest(fillRequest : FillRequest, cancelation : CancellationSignal,
                               callback: FillCallback) {

        type = (application as CryptoPassApp).autoFillData.getType()
        name = (application as CryptoPassApp).autoFillData.getName()
        data = (application as CryptoPassApp).autoFillData.getData()

        if(type == null || name == null || data == null) {
            callback.onSuccess(null)
            return
        }

        val context: List<FillContext> = fillRequest.fillContexts
        val structure: AssistStructure = context[context.size - 1].structure

        val response = createResponse(getAutoFillables(structure))
        callback.onSuccess(response)
    }

    private fun createResponse(autoFillables : Map<AutofillId, String>) : FillResponse? {
        if(autoFillables.isEmpty())
            return null
        val response = FillResponse.Builder()
        val dataset = Dataset.Builder()

        for(fillable in autoFillables) {
            val presentation = RemoteViews(packageName, android.R.layout.simple_list_item_1)
            presentation.setTextViewText(android.R.id.text1, name)
            dataset.setValue(
                fillable.key,
                AutofillValue.forText(fillable.value),
                presentation
            )
        }
        response.addDataset(dataset.build())
        return response.build()
    }

    private fun getAutoFillables(structure: AssistStructure) : Map<AutofillId, String> {
        val fields : MutableMap<AutofillId, String> = ArrayMap()
        val foundHints = ArrayList<String>()
        val windowNodes: List<AssistStructure.WindowNode> =
            structure.run {
                (0 until windowNodeCount).map { getWindowNodeAt(it) }
            }

        windowNodes.forEach { windowNode: AssistStructure.WindowNode ->
            val viewNode: ViewNode = windowNode.rootViewNode
            addAutofillables(foundHints, fields, viewNode)
        }

        return fields
    }

    private fun addAutofillables(foundHints: MutableList<String>, fields : MutableMap<AutofillId, String>, node : ViewNode) {
        val datasetInfo = datasetsInfo[type]
        node.autofillHints?. let { hints ->
            var foundMatch = false
            for(hint in hints) {
                if(foundHints.contains(hint))
                    continue
                for(index in datasetInfo!!.indices) {
                    val fieldInfo : FieldInfo = datasetInfo[index]
                    if(fieldInfo.supportedHints.contains(hint)) {
                        node.autofillId?. let { id ->
                            fields[id] = data!![index]
                            foundMatch = true
                            foundHints.add(hint)
                        }
                        break
                    }
                }
            if(foundMatch) break
            }
        }

        val children: List<ViewNode>? =
            node.run {
                (0 until childCount).map { getChildAt(it) }
            }

        children?.forEach { childNode: ViewNode ->
            addAutofillables(foundHints, fields, childNode)
        }
    }


    override fun onSaveRequest(p0: SaveRequest, p1: SaveCallback) {
        TODO("Not yet implemented")
    }
}