package com.cryptium.coincurrently.sharedui.components
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.util.AttributeSet
import android.util.Log
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.BaseRequestOptions
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.cryptium.coincurrently.R
import com.cryptium.coincurrently.widgets.receivers.getDominantColor


class CustomImageView: androidx.appcompat.widget.AppCompatImageView {

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    fun setImage(url: String?, context: Context, options: BaseRequestOptions<*>? = null, fromNews: Boolean = false) {
        if (options != null) {
            try {
                Glide.with(context)
                    .load(url)
                    .apply(options)
                    .placeholder(if (fromNews) R.drawable.news_card_background else R.drawable.skeleton_background)
                    .error(if (fromNews) R.drawable.news_card_background else R.drawable.skeleton_background)
                    .fallback(if (fromNews) R.drawable.news_card_background else R.drawable.skeleton_background)
                    .into(this)
            } catch (e: Exception) {
                return
            }

        } else {

            try {
                Glide.with(context)
                    .load(url)
                    .apply(RequestOptions().override(48, 48))
                    .into(this)
            } catch (e: Exception) {
                return
            }
        }
    }

    fun setImageWithCompletion(
        url: String?,
        context: Context,
        fromNews: Boolean = false,
        callback: (Int?, Bitmap?) -> Unit,
    ) {
        try {

            Glide.with(context)
                .asBitmap()
                .load(url)
                .placeholder(if (fromNews) R.drawable.news_card_background else R.drawable.skeleton_background)
                .error(if (fromNews) R.drawable.news_card_background else R.drawable.skeleton_background)
                .listener(object : RequestListener<Bitmap> {
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<Bitmap>?,
                        isFirstResource: Boolean
                    ): Boolean {
                        callback.invoke(null, null)
                        return false
                    }

                    override fun onResourceReady(
                        resource: Bitmap?,
                        model: Any?,
                        target: Target<Bitmap>?,
                        dataSource: DataSource?,
                        isFirstResource: Boolean
                    ): Boolean {
                        if (resource == null || resource.isRecycled) {
                            callback.invoke(null, null)
                            return false
                        }

                        val color = getDominantColor(resource, context)

                        /**
                         * Copy before returning — Glide recycles pooled bitmaps after this listener;
                         * reading from the ImageView later yields blank/recycled pixels.
                         */
                        val detachedCopy =
                            resource.copy(Bitmap.Config.ARGB_8888, false)

                        setImageBitmap(detachedCopy)
                        callback.invoke(color, detachedCopy)
                        return false
                    }

                })
                .preload()
        } catch (_: Exception) {
            callback.invoke(null, null)
        }
    }

    override fun setImageBitmap(bm: Bitmap?) {
        try {
            if (bm?.isRecycled == false) {
                super.setImageBitmap(bm)
            }
        } catch (e: Exception) {
            Log.d("Bitmap error ", e.localizedMessage ?: "Couldn't set image bitmap, unknown error")
            return
        }
    }
}