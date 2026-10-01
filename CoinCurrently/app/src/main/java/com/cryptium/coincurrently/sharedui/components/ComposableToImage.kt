package com.cryptium.coincurrently.sharedui.components
import android.app.Activity
import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Picture
import android.media.MediaScannerConnection
import android.net.Uri
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import androidx.activity.result.ActivityResultLauncher
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.ComposeView
import coil.ImageLoader
import com.cryptium.coincurrently.core.localization.Localization
import coil.compose.LocalImageLoader
import androidx.core.content.FileProvider
import androidx.core.view.drawToBitmap
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.File

private const val APP_SHARE_IOS_URL = "https://tinyurl.com/ygznuo76"
private const val APP_SHARE_ANDROID_URL = "https://tinyurl.com/yz5w8ozj"

fun appShareMessage(): String =
    listOf(
        Localization.string("Track crypto in real time with CoinCurrently."),
        Localization.string("Download the app for free."),
        Localization.format("iOS: %@", APP_SHARE_IOS_URL),
        Localization.format("Android: %@", APP_SHARE_ANDROID_URL),
    ).joinToString("\n")

fun assetShareMessage(trackedLabel: String, othersWord: String = "coins"): String {
    if (othersWord == "NFTs") {
        return listOf(
            Localization.format(
                "Track %@ and other NFTs in real time with CoinCurrently.",
                trackedLabel,
            ),
            Localization.string("Download the app for free."),
            Localization.format("iOS: %@", APP_SHARE_IOS_URL),
            Localization.format("Android: %@", APP_SHARE_ANDROID_URL),
        ).joinToString("\n")
    }
    return listOf(
        Localization.format(
            "Track %@ and other coins in real time with CoinCurrently.",
            trackedLabel,
        ),
        Localization.string("Download the app for free."),
        Localization.format("iOS: %@", APP_SHARE_IOS_URL),
        Localization.format("Android: %@", APP_SHARE_ANDROID_URL),
    ).joinToString("\n")
}

private val softwareBitmapCaptureImageLoaderLock = Any()
private var softwareBitmapCaptureImageLoader: ImageLoader? = null

/** Coil defaults to HARDWARE bitmaps; software Canvas (e.g. [View.drawToBitmap]) cannot draw them. */
private fun captureFriendlyImageLoader(context: Context): ImageLoader {
    synchronized(softwareBitmapCaptureImageLoaderLock) {
        softwareBitmapCaptureImageLoader?.let { return it }
        return ImageLoader.Builder(context.applicationContext)
            .allowHardware(false)
            .build()
            .also { softwareBitmapCaptureImageLoader = it }
    }
}

private const val DefaultLayoutSettleFrames = 3

fun renderComposableToBitmap(
    activity: Activity,
    composable: @Composable () -> Unit,
    onBitmapReady: (Bitmap) -> Unit,
    /**
     * After the tree first measures with non-zero size, wait this many extra pre-draw passes before
     * capturing, so Compose/Coil/the chart can settle. Use `0` to match the previous one-frame behavior.
     */
    layoutSettleFrames: Int = DefaultLayoutSettleFrames,
) {
    val host = activity.findViewById<ViewGroup>(android.R.id.content)
    val composeView = ComposeView(activity).apply {
        visibility = View.INVISIBLE
        layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        val hostContext = context
        setContent {
            CompositionLocalProvider(
                LocalImageLoader provides captureFriendlyImageLoader(hostContext)
            ) {
                composable()
            }
        }
    }

    val maxWidth = composeView.resources.displayMetrics.widthPixels
    val maxHeight = composeView.resources.displayMetrics.heightPixels

    host.addView(composeView)

    fun detach() {
        if (composeView.parent == host) {
            host.removeView(composeView)
        }
    }

    fun measureLayoutAndCapture() {
        composeView.measure(
            View.MeasureSpec.makeMeasureSpec(maxWidth, View.MeasureSpec.AT_MOST),
            View.MeasureSpec.makeMeasureSpec(maxHeight, View.MeasureSpec.AT_MOST)
        )
        val w = composeView.measuredWidth.coerceAtLeast(1)
        val h = composeView.measuredHeight.coerceAtLeast(1)
        composeView.measure(
            View.MeasureSpec.makeMeasureSpec(w, View.MeasureSpec.EXACTLY),
            View.MeasureSpec.makeMeasureSpec(h, View.MeasureSpec.EXACTLY)
        )
        composeView.layout(0, 0, w, h)
        val bitmap = composeView.drawToBitmap()
        onBitmapReady(bitmap)
    }

    var frameAttempts = 0
    var passesAfterSized = 0
    val preDrawListener = object : ViewTreeObserver.OnPreDrawListener {
        override fun onPreDraw(): Boolean {
            frameAttempts++
            val measuredWidth = composeView.measuredWidth
            val measuredHeight = composeView.measuredHeight

            val hasSize = measuredWidth > 0 && measuredHeight > 0
            if (hasSize) {
                passesAfterSized++
            }

            val settled =
                hasSize && passesAfterSized > layoutSettleFrames
            val forceCapture = frameAttempts >= 30
            val ready = settled || forceCapture

            if (!ready) {
                return true
            }

            composeView.viewTreeObserver.removeOnPreDrawListener(this)

            try {
                measureLayoutAndCapture()
            } finally {
                detach()
            }
            return true
        }
    }
    composeView.viewTreeObserver.addOnPreDrawListener(preDrawListener)
}

fun saveBitmapToCache(context: Context, bitmap: Bitmap, name: String): File {
    val file = File(context.cacheDir, "${name}.png")
    file.outputStream().use {
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
    }
    return file
}

fun shareBitmap(
    context: Context,
    bitmap: Bitmap,
    name: String,
    chooserLauncher: ActivityResultLauncher<Intent>? = null,
    trackedShareLabel: String? = null,
    /** Plural noun after \"other …\" (e.g. `"coins"` for crypto, `"NFTs"` for NFT detail). */
    trackedShareOthersWord: String = "coins",
) {
    val imageFile = saveBitmapToCache(context, bitmap, name)
    val uri = FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        imageFile
    )

    val text = trackedShareLabel?.let {
        assetShareMessage(it, othersWord = trackedShareOthersWord)
    } ?: appShareMessage()

    val shareIntent = Intent(Intent.ACTION_SEND).apply {
        type = "image/png"
        putExtra(Intent.EXTRA_STREAM, uri)
        putExtra(Intent.EXTRA_TEXT, text)
        // Sharesheet previews read ClipData; EXTRA_STREAM alone is often omitted from the chooser path.
        clipData = ClipData.newUri(context.contentResolver, null, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }

    val chooser = Intent.createChooser(shareIntent, Localization.string("Share image via"))
    if (chooserLauncher != null) {
        chooserLauncher.launch(chooser)
    } else {
        context.startActivity(chooser)
    }
}

private suspend fun Bitmap.saveToDisk(context: Context): Uri {
    val file = File(
       // Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
        "screenshot-${System.currentTimeMillis()}.png"
    )

    // file.writeBitmap(this, Bitmap.CompressFormat.PNG, 100)

    return scanFilePath(context, file.path) ?: throw Exception("File could not be saved")
}


private suspend fun scanFilePath(context: Context, filePath: String): Uri? {
    return suspendCancellableCoroutine { continuation ->
        MediaScannerConnection.scanFile(
            context,
            arrayOf(filePath),
            arrayOf("image/png")
        ) { _, scannedUri ->
            if (scannedUri == null) {
                continuation.cancel(Exception("File $filePath could not be scanned"))
            } else {
                //continuation.resume(scannedUri)
            }
        }
    }
}