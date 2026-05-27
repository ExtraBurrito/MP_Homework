package com.example.contacts

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.UUID

class PhotoRepository(private val context: Context) {
    suspend fun saveFromUri(source: Uri): String = withContext(Dispatchers.IO) {
        val resolver = context.contentResolver
        
        // Попытаемся получить права на чтение, если это возможно
        try {
            val takeFlags: Int = Intent.FLAG_GRANT_READ_URI_PERMISSION
            resolver.takePersistableUriPermission(source, takeFlags)
        } catch (e: Exception) {
            // Игнорируем, если провайдер не поддерживает persistable permissions
        }

        val bitmap = try {
            // Прямое чтение через поток обычно надежнее в нестабильных окружениях
            resolver.openInputStream(source)?.use { stream ->
                if (Build.VERSION.SDK_INT >= 28) {
                    val buffer = stream.readBytes()
                    val src = ImageDecoder.createSource(java.nio.ByteBuffer.wrap(buffer))
                    ImageDecoder.decodeBitmap(src) { decoder, _, _ ->
                        decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
                    }
                } else {
                    BitmapFactory.decodeStream(stream)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        } ?: throw IOException("Не удалось прочитать изображение по URI: $source")

        val fileName = "contact_${UUID.randomUUID()}.jpg"
        val file = File(context.filesDir, fileName)
        
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
        }
        
        file.absolutePath
    }
}
