package io.alexarix.pushreader

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Icon


fun getBitmapFromIcon(context: Context, icon: Icon): Bitmap? = try {
    icon.loadDrawable(context)?.let {
        (it as? BitmapDrawable)?.bitmap
    }
} catch (e: Exception) {
    e.printStackTrace()
    null
}
