/*
 * Copyright (C) 2021 Eugen Rădulescu <synapticwebb@gmail.com>
 * This file is part of the CipherPass project ( https://github.com/synapticweb/CipherPass ). 
 * See the LICENSE file in the project root for license terms. 
 */

package net.synapticweb.cipherpass.entrydetail

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Build
import androidx.work.Worker
import androidx.work.WorkerParameters

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