package net.synapticweb.passman.entrydetail

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters

class CleanClipboard(appContext: Context, workerParams: WorkerParameters) :
    Worker(appContext, workerParams) {

    override fun doWork(): Result {
        val clipboard = applicationContext.
        getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(null, null)
        clipboard.setPrimaryClip(clip)

        return Result.success()
    }
}