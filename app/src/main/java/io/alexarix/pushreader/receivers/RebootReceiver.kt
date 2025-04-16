package io.alexarix.pushreader.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dagger.hilt.android.AndroidEntryPoint
import io.alexarix.pushreader.services.PushReaderService2
import io.alexarix.pushreader.viewmodels.e

@AndroidEntryPoint
class RebootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            "Device booted. Restarting PushReaderService.".e
            val serviceIntent = Intent(context, PushReaderService2::class.java)
            // On Android O and above, you need to use startForegroundService
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                context.startForegroundService(serviceIntent)
            } else {
                context.startService(serviceIntent)
            }
        }
    }
}