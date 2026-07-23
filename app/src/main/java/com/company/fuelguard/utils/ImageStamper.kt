package com.company.fuelguard.utils

import android.content.ContentValues
import android.content.Context
import android.graphics.*
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

data class StampInfo(val lines: List<String>)

object ImageStamper {
    private const val FOLDER_NAME = "FuelGuard"

    suspend fun stampImage(context: Context, originalFile: File, info: StampInfo): Uri? = withContext(Dispatchers.IO) {
        try {
            val originalBitmap = BitmapFactory.decodeFile(originalFile.absolutePath)
            val mutableBitmap = originalBitmap.copy(Bitmap.Config.ARGB_8888, true)
            originalBitmap.recycle()
            val canvas = Canvas(mutableBitmap)
            val textSize = 36f
            val textPaint = Paint().apply {
                isAntiAlias = true
                color = Color.WHITE
                style = Paint.Style.FILL
                this.textSize = textSize
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                setShadowLayer(5f, 2f, 2f, Color.BLACK)
            }
            val xMargin = 40f
            val yMargin = 40f
            val lineSpacing = 10f
            var yPos = mutableBitmap.height - yMargin - (info.lines.size - 1) * (textSize + lineSpacing)
            info.lines.forEach { line ->
                canvas.drawText(line, xMargin, yPos, textPaint)
                yPos += (textSize + lineSpacing)
            }
            val fileName = "STAMPED_${originalFile.name}"
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    put(MediaStore.MediaColumns.RELATIVE_PATH, "DCIM/$FOLDER_NAME")
                    put(MediaStore.MediaColumns.IS_PENDING, 1)
                }
            }
            val resolver = context.contentResolver
            val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
            uri?.let {
                resolver.openOutputStream(it)?.use { stream -> mutableBitmap.compress(Bitmap.CompressFormat.JPEG, 95, stream) }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    contentValues.clear()
                    contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
                    resolver.update(it, contentValues, null, null)
                }
            }
            mutableBitmap.recycle()
            uri
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
