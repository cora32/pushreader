package io.alexarix.pushreader

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Icon
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle


fun getBitmapFromIcon(context: Context, icon: Icon): Bitmap? = try {
    icon.loadDrawable(context)?.let {
        (it as? BitmapDrawable)?.bitmap
    }
} catch (e: Exception) {
    e.printStackTrace()
    null
}

fun underlinedInfoText(name: String, value: String?) = buildAnnotatedString {
    withStyle(style = SpanStyle(textDecoration = TextDecoration.Underline)) {
        append(name)
    }
    append(": $value")
}