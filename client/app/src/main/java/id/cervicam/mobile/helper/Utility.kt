package id.cervicam.mobile.helper

import android.content.Context
import android.content.res.Resources
import android.database.Cursor
import android.provider.MediaStore
import android.view.Window
import android.view.WindowManager
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import id.cervicam.mobile.R
import java.io.File
import java.io.InputStream


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

        fun getOutputDirectory(context: Context, resources: Resources): File {
            val mediaDir = context!!.externalMediaDirs.firstOrNull()?.let {
                File(it, resources.getString(R.string.app_name)).apply { mkdirs() } }

            return if (mediaDir != null && mediaDir.exists()) mediaDir else context!!.filesDir
        }

        fun getBasename(path: String): String {
            return path.substring(path.lastIndexOf(File.separator) + 1)
        }
    }
}