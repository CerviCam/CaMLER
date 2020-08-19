package id.cervicam.mobile.helper

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.ExifInterface
import android.net.Uri
import android.provider.OpenableColumns
import android.view.Window
import android.view.WindowManager
import android.widget.Toast
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import id.cervicam.mobile.R
import id.cervicam.mobile.activities.CameraActivity
import id.cervicam.mobile.services.MainService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import java.io.*
import java.util.*
import java.util.concurrent.CountDownLatch
import kotlin.collections.ArrayList
import kotlin.collections.HashMap


/**
 * Collection of common functions
 *
 */
class Utility {
    companion object {
        /**
         * Hide all notifications on status bar
         * It is helpful to avoid user from notifications and let them to focus on current activity
         *
         * @param window    Window of activity
         */
        fun hideStatusBar(window: Window) {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }

        /**
         * Set status bar color as you want by passing color id through argument
         *
         * @param window    Window of activity
         * @param context   context of activity
         * @param color     Color id
         */
        fun setStatusBarColor(window: Window, context: Context, @ColorRes color: Int) {
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.statusBarColor = ContextCompat.getColor(context, color)
        }

        /**
         * Get available output directory from media directory
         * Use file directory if media directory doesn't exist
         *
         * @param context       Context of activity
         * @return              File of directory
         */
        fun getOutputDirectory(context: CameraActivity): File {
            val mediaDir = context.externalMediaDirs.firstOrNull()?.let {
                File(it, context.getString(R.string.app_name)).apply { mkdirs() }
            }

            return if (mediaDir != null && mediaDir.exists()) mediaDir else context.filesDir
        }

        /**
         * Trim path name and return file name only
         *
         * @param path  Path name
         * @return      File name
         */
        fun getBasename(path: String): String {
            return path.substring(path.lastIndexOf(File.separator) + 1)
        }

        /**
         * Get filename from URI that has "content://" on its path
         *
         * @param context   Context of activity
         * @param uri       The targeted URI
         */
        private fun getFileNameFromUriContent(context: Context, uri: Uri): String {
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

        /**
         * Get file from Uri instance if exist
         *
         * @param context   Context of activity
         * @param uri       The Uri
         * @return          Whether return file or not, depends on file exist or not from given uri
         */
        fun getFile(context: Context, uri: Uri): File? {
            val parcelFileDescriptor = context.contentResolver.openFileDescriptor(uri, "r", null)
            var file: File? = null
            parcelFileDescriptor?.let {
                val inputStream = FileInputStream(parcelFileDescriptor.fileDescriptor)
                file = File(context.cacheDir, getFileNameFromUriContent(context, uri))
                val outputStream = FileOutputStream(file)
                inputStream.copyTo(out = outputStream)
            }
            return file
        }

        /**
         * Compress image with variant quality between 0 - 100 (worst - best)
         *
         * @param path          The path of image
         * @param quality       Quality of image (0 - 100)
         * @param extension     Bitmap compress format, default is JPEG
         */
        fun compressImage(
            path: String,
            quality: Int,
            extension: Bitmap.CompressFormat = Bitmap.CompressFormat.JPEG
        ) {
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
            } catch (e: IOException) {
                throw e
            } finally {
                outStream?.close()
            }
        }

        fun stringifyJSON(data: HashMap<String, Any>): String {
            val gson = Gson()
            return gson.toJson(data).toString()
        }

        /**
         * Parse JSON into generic object
         *
         * @param json  Serialized JSON string
         * @return      A list of key-value, represented by HashMap
         */
        fun parseJSON(json: String?): HashMap<String, Any> {
            val `object` = JsonParser.parseString(json) as JsonObject
            val set: Set<Map.Entry<String, JsonElement>> =
                `object`.entrySet()
            val iterator: Iterator<Map.Entry<String, JsonElement>> =
                set.iterator()
            val map = HashMap<String, Any>()
            while (iterator.hasNext()) {
                val entry: Map.Entry<String, JsonElement> =
                    iterator.next()
                val key = entry.key
                val value: JsonElement = entry.value
                if (!value.isJsonPrimitive) {
                    if (value.isJsonObject) {
                        map[key] = parseJSON(value.toString())
                    } else if (value.isJsonArray) {
                        val valueIsObject: Boolean = value.toString().contains(":")

                        map[key] = if (valueIsObject) {
                            val list: MutableList<HashMap<String, Any>> = ArrayList()
                            for (element in value.asJsonArray) {
                                list.add(parseJSON(element.toString()))
                            }
                            list
                        } else {
                            val list: MutableList<String> = ArrayList()
                            for (element in value.asJsonArray) {
                                list.add(element.toString())
                            }
                            list
                        }
                    }
                } else {
                    map[key] = value.asString
                }
            }
            return map
        }

        @Synchronized
        fun getAppId(context: Context): String {
            var id: String? = LocalStorage.get(context, LocalStorage.PreferenceKeys.ID.value)

            // If id doesn't exist then create new one and put the id into SharedPreference for future use
            if (id == null) {
                id = UUID.randomUUID().toString()
                LocalStorage.set(context, LocalStorage.PreferenceKeys.ID.value, id)
            }
            return id
        }

        private fun setToken(context: Context, username: String, password: String): Boolean {
            val waitUntilGetResponse = CountDownLatch(1)
            var foundToken = false
            MainService.getToken(
                context,
                username = username,
                password = password,
                callback = object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        e.printStackTrace()
                        waitUntilGetResponse.countDown()
                    }

                    override fun onResponse(call: Call, response: Response) {
                        if (response.isSuccessful) {
                            if (response.code() == 200) {
                                val body = parseJSON(response.body()?.string())
                                foundToken = try {
                                    (body["non_field_errors"] as ArrayList<*>)[0].toString() != "\"Unable to log in with provided credentials.\""
                                } catch (e: TypeCastException) {
                                    LocalStorage.set(
                                        context,
                                        LocalStorage.PreferenceKeys.TOKEN.value,
                                        body["token"].toString()
                                    )
                                    true
                                }
                            }
                        } else {
                            Toast.makeText(context, "Request failed", Toast.LENGTH_LONG).show()
                        }
                        waitUntilGetResponse.countDown()
                    }
                }
            )

            waitUntilGetResponse.await()
            return foundToken
        }

        fun setUser(context: Context) {
            var username: String? =
                LocalStorage.get(context, LocalStorage.PreferenceKeys.USERNAME.value)
            var password: String? =
                LocalStorage.get(context, LocalStorage.PreferenceKeys.PASSWORD.value)

            var shouldCreateUser = true
            if (username != null && password != null) {
                shouldCreateUser = !setToken(context, username, password)
            }

            val creatingUserIfNecessary = CountDownLatch(1)

            // If username or password is empty then create new one and put the credential into SharedPreferences
            if (shouldCreateUser) {
                username = getAppId(context)
                password = getAppId(context)

                runBlocking {
                    launch(Dispatchers.Default) {
                        MainService.createUser(
                            context,
                            name = "${android.os.Build.MODEL} [${username}]",
                            username = username,
                            password = password,
                            callback = object : Callback {
                                override fun onFailure(call: Call, e: IOException) {
                                    e.printStackTrace()
                                    creatingUserIfNecessary.countDown()
                                }

                                override fun onResponse(call: Call, response: Response) {
                                    if (response.isSuccessful) {
                                        if (response.code() == 201) {
                                            LocalStorage.set(
                                                context,
                                                LocalStorage.PreferenceKeys.USERNAME.value,
                                                username
                                            )
                                            LocalStorage.set(
                                                context,
                                                LocalStorage.PreferenceKeys.PASSWORD.value,
                                                password
                                            )
                                        }
                                    } else {
                                        Toast.makeText(context, "Request failed", Toast.LENGTH_LONG)
                                            .show()
                                    }
                                    creatingUserIfNecessary.countDown()
                                }
                            }
                        )
                    }
                }
            } else {
                creatingUserIfNecessary.countDown()
            }

            creatingUserIfNecessary.await()
            val failToGetToken: Boolean =
                username != null && password != null && !setToken(context, username, password)

            if (failToGetToken) {
                Toast.makeText(context, "Fail to set token", Toast.LENGTH_LONG).show()
            }
        }
    }
}