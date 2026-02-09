package com.ai.image.gen.data.worker

import kotlin.jvm.java

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.util.Base64
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.ai.image.gen.BuildConfig
import com.ai.image.gen.MainActivity
import com.ai.image.gen.data.HuggingFaceApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import kotlin.math.min

class ImageEditWorker(
    private val context: Context,
    workerParams: WorkerParameters,
    private val api: HuggingFaceApi
) : CoroutineWorker(context, workerParams) {

    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val prompt = inputData.getString(KEY_PROMPT) ?: return@withContext Result.failure()
        val imageUriString = inputData.getString(KEY_IMAGE_URI) ?: return@withContext Result.failure()

        // 1. PROMOTE TO FOREGROUND SERVICE
        // This prevents the OS from killing the worker after 10s.
        val foregroundInfo = createForegroundInfo("Optimizing pixels...", prompt)
        setForeground(foregroundInfo)

        try {
            // Load & Resize (Max 512px for bandwidth safety)
            val base64Image = loadAndResizeImage(Uri.parse(imageUriString))

            val jsonBody = JSONObject().apply {
                put("inputs", prompt)
                put("image", base64Image)

                put("parameters", JSONObject().apply {
                    // CRITICAL for Lightning Models:
                    put("num_inference_steps", 4) // Exact match for the 4-step model
                    put("guidance_scale", 1.0)    // Keep low for Lightning
                    put("strength", 0.75)         // Style transfer strength
                    put("negative_prompt", "blurry, low quality, distorted, ugly, pixelated")
                })
            }

            val requestBody = jsonBody.toString().toRequestBody("application/json".toMediaTypeOrNull())

            val response = api.editImage(
                token = "Bearer ${BuildConfig.HF_TOKEN}",
                inputs = requestBody
            )

            val bytes = response.bytes()
            val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)

            if (bitmap == null) {
                val errorMsg = String(bytes)
                // If 503, retry.
                if (errorMsg.contains("loading", ignoreCase = true)) return@withContext Result.retry()

                showFinalNotification("Editing Failed", "Server Error: Model busy or invalid format.", 2)
                return@withContext Result.failure()
            }

            val savedPath = saveBitmapToStorage(bitmap)

            // 2. Final Success Notification (Removes the persistent progress bar)
            showFinalNotification("Edit Complete!", "Tap to view your masterpiece.", 3, savedPath)

            Result.success(workDataOf(KEY_RESULT_PATH to savedPath))

        } catch (e: Exception) {
            e.printStackTrace()
            if (e is retrofit2.HttpException && e.code() == 503) {
                // Update notification to let user know we are retrying
                setForeground(createForegroundInfo("Waking up GPU...", "This takes a few seconds"))
                Result.retry()
            } else {
                showFinalNotification("Editing Failed", "Error: ${e.localizedMessage}", 2)
                Result.failure()
            }
        }
    }

    // --- Helper Methods ---

    private fun createForegroundInfo(title: String, subtitle: String): ForegroundInfo {
        val channelId = "ai_image_editor_processing"
        createChannel(channelId, "Active Edits")

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_menu_gallery) // Replace with your ic_stat_name
            .setContentTitle(title)
            .setContentText(subtitle)
            .setTicker(title)
            .setOngoing(true) // Persistent
            .setProgress(0, 0, true) // Indeterminate progress bar
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        // FOREGROUND_SERVICE_TYPE_DATA_SYNC is required for Android 14+ if defined in Manifest,
        // otherwise default 0 is fine for older versions, but let's be safe.
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ForegroundInfo(101, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)
        } else {
            ForegroundInfo(101, notification)
        }
    }

    private fun showFinalNotification(title: String, content: String, id: Int, path: String? = null) {
        val channelId = "ai_image_editor_results"
        createChannel(channelId, "Edit Results")

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            if (path != null) {
                putExtra(KEY_RESULT_PATH, path)
            }
        }
        val pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_menu_gallery)
            .setContentTitle(title)
            .setContentText(content)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            NotificationManagerCompat.from(context).notify(id, builder.build())
        }
    }

    private fun createChannel(id: String, name: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(id, name, NotificationManager.IMPORTANCE_DEFAULT)
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun loadAndResizeImage(uri: Uri): String {
        val inputStream = context.contentResolver.openInputStream(uri) ?: throw Exception("Cannot open URI")
        val originalBitmap = BitmapFactory.decodeStream(inputStream)
        inputStream.close()

        val ratio = min(512.0 / originalBitmap.width, 512.0 / originalBitmap.height)
        val width = (originalBitmap.width * ratio).toInt()
        val height = (originalBitmap.height * ratio).toInt()
        val resized = Bitmap.createScaledBitmap(originalBitmap, width, height, true)

        val outputStream = ByteArrayOutputStream()
        resized.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
        return Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)
    }

    private fun saveBitmapToStorage(bitmap: Bitmap): String {
        val filename = "edited_${System.currentTimeMillis()}.jpg"
        val file = File(context.filesDir, filename)
        FileOutputStream(file).use { out -> bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out) }
        return file.absolutePath
    }

    companion object {
        const val KEY_PROMPT = "prompt"
        const val KEY_IMAGE_URI = "image_uri"
        const val KEY_RESULT_PATH = "result_path"
    }
}