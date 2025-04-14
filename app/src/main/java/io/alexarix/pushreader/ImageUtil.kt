package io.alexarix.pushreader

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import java.io.ByteArrayOutputStream


@Throws(IllegalArgumentException::class)
fun convert(base64Str: String): Bitmap? {
    val decodedBytes: ByteArray = Base64.decode(
        base64Str.substring(base64Str.indexOf(",") + 1),
        Base64.DEFAULT
    )

    return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
}

fun Bitmap.toBase64(): String {
    return ByteArrayOutputStream().use { outputStream ->
        compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
        Base64.encodeToString(outputStream.toByteArray(), Base64.DEFAULT)
    }
}