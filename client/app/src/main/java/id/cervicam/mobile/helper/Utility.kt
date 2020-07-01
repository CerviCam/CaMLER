package id.cervicam.mobile.helper

import android.content.Context
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.ExifInterface
import android.net.Uri
import android.provider.OpenableColumns
import android.view.Window
import android.view.WindowManager
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import id.cervicam.mobile.R
import id.cervicam.mobile.activities.CameraActivity
import java.io.*


class Utility {
    companion object {
        fun hideStatusBar(window: Window) {
            window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        }

        fun setStatusBarColor(window: Window, context: Context, @ColorRes color: Int) {
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.statusBarColor = ContextCompat.getColor(context, color)
        }

        fun getOutputDirectory(context: CameraActivity, resources: Resources): File {
            val mediaDir = context!!.externalMediaDirs.firstOrNull()?.let {
                File(it, resources.getString(R.string.app_name)).apply { mkdirs() } }

            return if (mediaDir != null && mediaDir.exists()) mediaDir else context!!.filesDir
        }

        fun getBasename(path: String): String {
            return path.substring(path.lastIndexOf(File.separator) + 1)
        }

        private fun getFileName(context: Context, uri: Uri): String {
            var name = ""
            val returnCursor = context.contentResolver.query(uri, null, null, null, null)
            if (returnCursor != null) {
                val nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                returnCursor.moveToFirst()
                name = returnCursor.getString(nameIndex)
                returnCursor.close()
            }

            return name
        }

        fun getFile(context: Context, uri: Uri): File? {
            val parcelFileDescriptor = context.contentResolver.openFileDescriptor(uri, "r", null)
            var file: File? = null
            parcelFileDescriptor?.let {
                val inputStream = FileInputStream(parcelFileDescriptor.fileDescriptor)
                file = File(context.cacheDir, getFileName(context, uri))
                val outputStream = FileOutputStream(file)
                inputStream.copyTo(out = outputStream)
            }
            return file
        }

        fun compressImage(
            path: String,
            quality: Int,
            extension: Bitmap.CompressFormat = Bitmap.CompressFormat.JPEG
        ): Unit {
            val file = File(path)
            if (!file.exists()) return

            val exifOrientation = ExifInterface(path).getAttribute(ExifInterface.TAG_ORIENTATION)
            var outStream: OutputStream? = null
            try {
                val imageBitmap: Bitmap? = BitmapFactory.decodeFile(file.absolutePath)
                outStream = BufferedOutputStream(FileOutputStream(file))
                imageBitmap?.compress(extension, quality, outStream)

                val currentExif = ExifInterface(path)
                currentExif.setAttribute(ExifInterface.TAG_ORIENTATION, exifOrientation)
                currentExif.saveAttributes()
            } catch(e: IOException) {
                throw e
            } finally {
                outStream?.close()
            }
        }
    }
}