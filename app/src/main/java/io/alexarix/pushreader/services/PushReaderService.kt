package io.alexarix.pushreader.services

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.drawable.VectorDrawable
import android.os.Build
import android.os.Bundle
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.graphics.drawable.toBitmap
import dagger.hilt.android.AndroidEntryPoint
import io.alexarix.pushreader.R
import io.alexarix.pushreader.repo.Repo
import io.alexarix.pushreader.toBase64
import javax.inject.Inject


@AndroidEntryPoint
class PushReaderService @Inject constructor(
    repo: Repo
) : NotificationListenerService() {
    val channelId = "pushreader_notification_channel"

    override fun onListenerConnected() {
        super.onListenerConnected()

        Log.e("PushReader", "Notification listener connected")
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun getSmallIcon(externalResources: Resources, notification: Notification): Bitmap? {
        return try {
            val id = notification.smallIcon.resId
            val drawable = externalResources.getDrawable(id) as? VectorDrawable
            drawable?.toBitmap()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    @RequiresApi(Build.VERSION_CODES.S)
    @SuppressLint("UseCompatLoadingForDrawables")
    private fun getLargeIconBig(extras: Bundle): Bitmap? {
        return try {
            if (extras.containsKey(Notification.EXTRA_LARGE_ICON_BIG)) {
                // this bitmap contain the picture attachment
                extras.get(Notification.EXTRA_LARGE_ICON_BIG) as Bitmap?
            } else null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    @RequiresApi(Build.VERSION_CODES.S)
    @SuppressLint("UseCompatLoadingForDrawables")
    private fun getLargeIcon(extras: Bundle): Bitmap? {
        return try {
            if (extras.containsKey(Notification.EXTRA_LARGE_ICON)) {
                // this bitmap contain the picture attachment
                extras.get(Notification.EXTRA_LARGE_ICON) as Bitmap?
            } else null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun getExtraPicture(extras: Bundle): Bitmap? {
        return try {
            if (extras.containsKey(Notification.EXTRA_PICTURE)) {
                // this bitmap contain the picture attachment
                extras.get(Notification.EXTRA_PICTURE) as Bitmap?
            } else null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        super.onNotificationPosted(sbn)
        val packageName = sbn.packageName
        val resources: Resources = packageManager.getResourcesForApplication(packageName)

        val notification: Notification = sbn.notification
        val extras = notification.extras
        val tickerText = notification.tickerText?.toString()
        val smallIconStr = getSmallIcon(resources, notification)?.toBase64()
        val largeIconStr1 = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            getLargeIcon(extras = extras)?.toBase64()
        } else {
            null
        }
        val largeIconStr2 = notification.largeIcon?.toBase64()
        val largeIconBig = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            getLargeIconBig(extras)?.toBase64()
        } else {
            null
        }
        val title = extras?.getString(Notification.EXTRA_TITLE)
        val bigTitle = extras?.getString(Notification.EXTRA_TITLE_BIG)
        val text = extras?.getCharSequence(Notification.EXTRA_TEXT)?.toString()
        val bigText = extras?.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString()
        val extraPictureStr = getExtraPicture(extras = extras)?.toBase64()
        val actions = notification.actions?.map {it.title}
        val category = notification.category

        Log.e("PushReader", "Notification Posted:")
        Log.e("PushReader", "  Package: $packageName")
        Log.e("PushReader", "  Ticker: $tickerText")
        Log.e("PushReader", "  Title: $title")
        Log.e("PushReader", "  bigTitle: $bigTitle")
        Log.e("PushReader", "  Text: $text")
        Log.e("PushReader", "  bigText: $bigText")
        Log.e("PushReader", "  smallIconStr: ${smallIconStr?.length}")
        Log.e("PushReader", "  largeIconStr1: ${largeIconStr1?.length}")
        Log.e("PushReader", "  largeIconStr2: ${largeIconStr2?.length}")
        Log.e("PushReader", "  largeIconBig: ${largeIconBig?.length}")
        Log.e("PushReader", "  extraPictureStr: ${extraPictureStr?.length}")
        Log.e("PushReader", "  actions: $actions")
        Log.e("PushReader", "  category: $category")
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        super.onNotificationRemoved(sbn)
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        Log.e("PushReader", "Notification listener disconnected")
    }

    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int
    ): Int {
        Log.e("PushReader", "onStartCommand")

        return START_STICKY
    }

    override fun onCreate() {
        super.onCreate()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                channelId,
                "PushReader Service Channel",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)
        }

        val notification: Notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("PushReader is Active")
            .setContentText("PushReader is monitoring notifications.")
            .setSmallIcon(R.drawable.ic_stat_name)
            .build()

        startForeground(1, notification)
    }
}