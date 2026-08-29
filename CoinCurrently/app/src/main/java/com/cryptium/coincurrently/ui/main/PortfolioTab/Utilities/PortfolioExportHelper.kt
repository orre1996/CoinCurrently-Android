package com.cryptium.coincurrently.ui.main.PortfolioTab.Utilities

import android.content.ClipData
import android.content.Context
import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import androidx.core.content.FileProvider
import java.io.File

object PortfolioExportHelper {
    fun shareExport(
        context: Context,
        content: String,
        fileName: String,
        extension: String,
        mimeType: String,
        launcher: ActivityResultLauncher<Intent>? = null,
    ): Boolean {
        return try {
            val file = File(context.cacheDir, "$fileName.$extension")
            file.writeText(content)

            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file,
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = mimeType
                putExtra(Intent.EXTRA_STREAM, uri)
                clipData = ClipData.newUri(context.contentResolver, fileName, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val chooser = Intent.createChooser(shareIntent, "Export portfolio")
            if (launcher != null) {
                launcher.launch(chooser)
            } else {
                context.startActivity(chooser)
            }
            true
        } catch (_: Exception) {
            false
        }
    }
}
