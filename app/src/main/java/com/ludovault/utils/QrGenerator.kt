package com.ludovault.utils

import android.graphics.Bitmap
import android.graphics.Color
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter

/**
 * Utility for generating UPI payment QR codes.
 */
object QrGenerator {

    /**
     * Generates a UPI payment QR bitmap.
     *
     * @param upiId The recipient UPI ID.
     * @param amount Payment amount.
     * @param name Recipient name (optional).
     * @param size Bitmap size in pixels.
     * @return QR code bitmap or null on failure.
     */
    fun generateUpiQr(
        upiId: String,
        amount: String,
        name: String = "Ludo Vault User",
        size: Int = 512
    ): Bitmap? {
        return try {
            val upiUri = buildString {
                append("upi://pay?pa=$upiId")
                append("&pn=${name.replace(" ", "%20")}")
                append("&am=$amount")
                append("&cu=INR")
            }
            val hints = mapOf(EncodeHintType.MARGIN to 1)
            val writer = QRCodeWriter()
            val bitMatrix = writer.encode(upiUri, BarcodeFormat.QR_CODE, size, size, hints)
            val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
            for (x in 0 until size) {
                for (y in 0 until size) {
                    bitmap.setPixel(x, y, if (bitMatrix[x, y]) Color.BLACK else Color.WHITE)
                }
            }
            bitmap
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
