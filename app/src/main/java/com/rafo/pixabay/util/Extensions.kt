package com.rafo.pixabay.util

import android.annotation.SuppressLint
import android.app.Activity
import android.app.WallpaperManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.view.View
import android.view.animation.TranslateAnimation
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.Toast
import androidx.lifecycle.LifecycleCoroutineScope
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.download.library.DownloadImpl
import com.download.library.DownloadListenerAdapter
import com.download.library.Extra
import com.obsez.android.lib.filechooser.ChooserDialog
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import java.io.File


fun View.visible() {
    visibility = View.VISIBLE
}

fun View.gone() {
    visibility = View.GONE
}

fun View.invisible() {
    visibility = View.INVISIBLE
}

fun View.isVisible() = visibility == View.VISIBLE

fun View.isGone() = visibility == View.GONE

fun View.isInvisible() = visibility == View.INVISIBLE

fun Context.hideKeyboard(view: View) {
    val inputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
    inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
}

fun ImageView.load(url: String) {
    Glide.with(this).load(url).into(this)
}

@SuppressLint("CheckResult")
fun String.setupWallpaper(context: Context) {
    val myWallpaperManager = WallpaperManager.getInstance(context)
    Glide.with(context)
        .asBitmap()
        .load(this)
        .into(object : CustomTarget<Bitmap>() {
            override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    myWallpaperManager.setBitmap(resource, null, true, WallpaperManager.FLAG_SYSTEM)
                    myWallpaperManager.setBitmap(resource, null, true, WallpaperManager.FLAG_LOCK)
                } else {
                    myWallpaperManager.setBitmap(resource)
                }
            }

            override fun onLoadCleared(placeholder: Drawable?) {
            }
        })
}

fun String.downloadImageNew(
    context: Context,
    filePath: String = Environment.DIRECTORY_PICTURES,
    loadingProcess: ((Long, Long) -> Unit),
    finished: (() -> Unit)
) {
    val file = File(filePath)
    file.mkdir()
    DownloadImpl.getInstance()
        .with(context)
        .target(file)
        .setRetry(4)
        .setUniquePath(false)
        .url(this)
        .enqueue(object : DownloadListenerAdapter() {
            override fun onStart(
                url: String?,
                userAgent: String?,
                contentDisposition: String?,
                mimetype: String?,
                contentLength: Long,
                extra: Extra?
            ) {
                super.onStart(url, userAgent, contentDisposition, mimetype, contentLength, extra)
            }

            override fun onProgress(url: String?, downloaded: Long, length: Long, usedTime: Long) {
                super.onProgress(url, downloaded, length, usedTime)
                loadingProcess.invoke(downloaded, length)
            }

            override fun onResult(
                throwable: Throwable?,
                path: Uri?,
                url: String?,
                extra: Extra?
            ): Boolean {
                finished.invoke()
                return super.onResult(throwable, path, url, extra)
            }
        })
}

fun View.slideBottomUp() {
    val animate = TranslateAnimation(0f, 0f, height.toFloat(), 0f)
    animate.duration = 500
    animate.fillAfter = true
    startAnimation(animate)
}

fun View.slideBottomDown() {
    val animate = TranslateAnimation(0f, 0f, 0f, height.toFloat())
    animate.duration = 500
    animate.fillAfter = true
    startAnimation(animate)
}

fun View.slideTopUp() {
    val animate = TranslateAnimation(0f, 0f, 0f, -height.toFloat())
    animate.duration = 500
    animate.fillAfter = true
    startAnimation(animate)
}

fun View.slideTopDown() {
    val animate = TranslateAnimation(0f, 0f, -height.toFloat(), 0f)
    animate.duration = 500
    animate.fillAfter = true
    startAnimation(animate)
}


fun <T> Flow<T>.launchWhenStarted(lifecycleScope: LifecycleCoroutineScope) {
    lifecycleScope.launchWhenStarted { this@launchWhenStarted.collect() }
}

fun showDirectoryPathDialog(
    context: Context,
    imageUrl: String,
    processListener: ((Int, Int) -> Unit),
    successListener: (() -> Unit)
) {
    val absolutePath = context.getExternalFilesDir(null)?.let {
        it.parent.split("/Andro")[0]
    } ?: Environment.getExternalStorageDirectory().absolutePath

    ChooserDialog()
        .with(context)
        .withFilter(true, false)
        .withStartFile(absolutePath) // to handle the result(s)
        .withChosenListener { path, _ ->
            if (imageUrl.isNotEmpty())
                imageUrl.downloadImageNew(
                    context,
                    path,
                    { downloaded, length ->

                        processListener.invoke(length.toInt(), downloaded.toInt())
                    }) {
                    successListener.invoke()
                    Toast.makeText(
                        context,
                        "image saved",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }
        .build()
        .show()
}
