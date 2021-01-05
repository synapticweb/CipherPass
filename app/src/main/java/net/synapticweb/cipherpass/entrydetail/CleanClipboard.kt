package net.synapticweb.cipherpass.entrydetail

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Build
import androidx.work.Worker
import androidx.work.WorkerParameters
import net.synapticweb.cipherpass.CLIPBOARD_LABEL_KEY

class CleanClipboard(appContext: Context, workerParams: WorkerParameters) :
    Worker(appContext, workerParams) {

    override fun doWork() : Result {
        val clipboard = applicationContext.
        getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val lastClip = clipboard.primaryClip
        lastClip?. let {
            if(it.description.label == CLIPBOARD_LABEL_KEY)
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.P)
                    clipboard.clearPrimaryClip()
                else
                    clipboard.setPrimaryClip(ClipData.newPlainText(null, null))
        }

        return Result.success()
    }
}