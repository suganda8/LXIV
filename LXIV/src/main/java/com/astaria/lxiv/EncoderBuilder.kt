package com.astaria.lxiv

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import java.io.ByteArrayOutputStream

class EncoderBuilder(
    private val context: Context? = null
) {
    private var input: Any? = null
    private var quality: Int? = null
    private var compressFormat: Bitmap.CompressFormat? = null
    private var flag: Int? = null

    companion object {
        inline fun fromBitmap(context: Context, block: (Encoder) -> Unit): String = Encoder(context).apply(block).bitmapBase64()
        inline fun fromUri(context: Context, block: (Encoder) -> Unit): String = Encoder(context).apply(block).uriBase64()

        fun Encoder.bitmapBase64() : String {
            if (input == null) throw IllegalArgumentException("Set input source first from either Bitmap or Uri.")
            if (input !is Bitmap) throw IllegalArgumentException("Set input source from Bitmap.")
            val baos = ByteArrayOutputStream()
            (input as Bitmap).compress(compressFormat, quality ?: throw IllegalArgumentException("Quality should not be null."), baos)
            val imageBytes = baos.toByteArray()

            return Base64.encodeToString(imageBytes, flag ?: Base64.DEFAULT)
        }

        fun Encoder.uriBase64() : String {
            if (input == null) throw IllegalArgumentException("Set input source first from either Bitmap or Uri.")
            if (input !is Uri) throw IllegalArgumentException("Set Drawable.")
            val inputStream = context.contentResolver.openInputStream(input as Uri)
            val imageBitmap = BitmapFactory.decodeStream(inputStream, null, null) ?: throw NullPointerException()

            val baos = ByteArrayOutputStream()
            imageBitmap.compress(compressFormat, quality ?: throw IllegalArgumentException("Quality should not be null."), baos)
            val imageByteArray = baos.toByteArray()

            return Base64.encodeToString(imageByteArray, flag ?: Base64.DEFAULT)
        }
    }

    fun setBitmap(bitmap: Bitmap?): EncoderBuilder {
        if (bitmap == null) throw IllegalArgumentException("Bitmap should not be null.")
        this.input = bitmap
        return this
    }

    fun setUri(uri: Uri?): EncoderBuilder {
        if (uri == null) throw IllegalArgumentException("Uri should not be null.")
        this.input = uri
        return this
    }

    fun setQuality(quality: Int): EncoderBuilder {
        if (quality < 0 || quality > 100) throw IllegalArgumentException("Quality should be around 0 to 100.")
        this.quality = quality
        return this
    }

    fun setCompressFormat(compressFormat: Bitmap.CompressFormat): EncoderBuilder {
        this.compressFormat = compressFormat
        return this
    }

    fun setFlag(flag: Int?): EncoderBuilder {
        this.flag = flag
        return this
    }

    fun build(): String {
        return when (input) {
            is Bitmap -> Encoder(context ?: throw IllegalArgumentException("Context should not be null."),
                input ?: throw IllegalArgumentException("Either Bitmap or Uri should not be null. Please use setBitmap or setUri method first."),
                quality ?: throw IllegalArgumentException("Quality should not be null."),
                compressFormat ?: throw IllegalArgumentException("Compress Format should not be null."),
                flag).bitmapBase64()
            is Uri -> Encoder(context ?: throw IllegalArgumentException("Context should not be null."),
                input ?: throw IllegalArgumentException("Either Bitmap or Uri should not be null. Please use setBitmap or setUri method first."),
                quality ?: throw IllegalArgumentException("Quality should not be null."),
                compressFormat ?: throw IllegalArgumentException("Compress Format should not be null."),
                flag).uriBase64()
            else -> throw IllegalArgumentException("Please use either method setBitmap or setUri.")
        }
    }
}