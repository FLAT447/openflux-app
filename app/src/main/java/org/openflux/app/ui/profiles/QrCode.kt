package org.openflux.app.ui.profiles

import android.graphics.Bitmap
import android.graphics.Color
import com.google.zxing.BarcodeFormat
import com.google.zxing.WriterException
import com.google.zxing.qrcode.QRCodeWriter

/**
 * Renders content as a black-on-white QR bitmap, or null if it doesn't fit
 * (WriterException - e.g. an unusually long MAX-transport token pushing the
 * payload past what a QR code can hold). zxing's `core` artifact is used
 * for encoding only, no camera/scanning dependency.
 */
fun generateQrBitmap(content: String, sizePx: Int): Bitmap? {
    val matrix = try {
        QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, sizePx, sizePx)
    } catch (e: WriterException) {
        return null
    }
    val bitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.RGB_565)
    for (x in 0 until sizePx) {
        for (y in 0 until sizePx) {
            bitmap.setPixel(x, y, if (matrix[x, y]) Color.BLACK else Color.WHITE)
        }
    }
    return bitmap
}
