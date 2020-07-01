package id.cervicam.mobile.helper

import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.content.res.Resources
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.view.Window
import android.view.WindowManager
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import id.cervicam.mobile.R
import id.cervicam.mobile.activities.CameraActivity
import java.io.File


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

        /* Get uri related content real local file path. */
        fun getUriRealPath(
            ctx: Context,
            uri: Uri
        ): String? {
            var ret: String? = ""
            ret = if (isAboveKitKat()) {
                // Android OS above sdk version 19.
                getUriRealPathAboveKitkat(ctx, uri)
            } else {
                // Android OS below sdk version 19
                getImageRealPath(ctx.contentResolver, uri, null)
            }
            return ret
        }

        private fun getUriRealPathAboveKitkat(
            ctx: Context?,
            uri: Uri?
        ): String? {
            var ret: String? = ""
            if (ctx != null && uri != null) {
                if (isContentUri(uri)) {
                    ret = if (isGooglePhotoDoc(uri.authority)) {
                        uri.lastPathSegment
                    } else {
                        getImageRealPath(ctx.contentResolver, uri, null)
                    }
                } else if (isFileUri(uri)) {
                    ret = uri.path
                } else if (isDocumentUri(ctx, uri)) {

                    // Get uri related document id.
                    val documentId = DocumentsContract.getDocumentId(uri)

                    // Get uri authority.
                    val uriAuthority = uri.authority
                    if (isMediaDoc(uriAuthority)) {
                        val idArr =
                            documentId.split(":".toRegex()).toTypedArray()
                        if (idArr.size == 2) {
                            // First item is document type.
                            val docType = idArr[0]

                            // Second item is document real id.
                            val realDocId = idArr[1]

                            // Get content uri by document type.
                            var mediaContentUri =
                                MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                            if ("image" == docType) {
                                mediaContentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                            } else if ("video" == docType) {
                                mediaContentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                            } else if ("audio" == docType) {
                                mediaContentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                            }

                            // Get where clause with real document id.
                            val whereClause =
                                MediaStore.Images.Media._ID + " = " + realDocId
                            ret =
                                getImageRealPath(ctx.contentResolver, mediaContentUri, whereClause)
                        }
                    } else if (isDownloadDoc(uriAuthority)) {
                        // Build download uri.
                        val downloadUri =
                            Uri.parse("content://downloads/public_downloads")

                        // Append download document id at uri end.
                        val downloadUriAppendId = ContentUris.withAppendedId(
                            downloadUri,
                            java.lang.Long.valueOf(documentId)
                        )
                        ret = getImageRealPath(ctx.contentResolver, downloadUriAppendId, null)
                    } else if (isExternalStoreDoc(uriAuthority)) {
                        val idArr =
                            documentId.split(":".toRegex()).toTypedArray()
                        if (idArr.size == 2) {
                            val type = idArr[0]
                            val realDocId = idArr[1]
                            if ("primary".equals(type, ignoreCase = true)) {
                                print("test")
                                ret = Environment.getExternalStorageDirectory()
                                    .toString() + "/" + realDocId
                            }
                        }
                    }
                }
            }
            return ret
        }

        /* Check whether current android os version is bigger than kitkat or not. */
        private fun isAboveKitKat(): Boolean {
            var ret = false
            ret = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT
            return ret
        }

        /* Check whether this uri represent a document or not. */
        private fun isDocumentUri(
            ctx: Context?,
            uri: Uri?
        ): Boolean {
            var ret = false
            if (ctx != null && uri != null) {
                ret = DocumentsContract.isDocumentUri(ctx, uri)
            }
            return ret
        }

        /* Check whether this uri is a content uri or not.
*  content uri like content://media/external/images/media/1302716
*  */
        private fun isContentUri(uri: Uri?): Boolean {
            var ret = false
            if (uri != null) {
                val uriSchema = uri.scheme
                if ("content".equals(uriSchema, ignoreCase = true)) {
                    ret = true
                }
            }
            return ret
        }

        /* Check whether this uri is a file uri or not.
*  file uri like file:///storage/41B7-12F1/DCIM/Camera/IMG_20180211_095139.jpg
* */
        private fun isFileUri(uri: Uri?): Boolean {
            var ret = false
            if (uri != null) {
                val uriSchema = uri.scheme
                if ("file".equals(uriSchema, ignoreCase = true)) {
                    ret = true
                }
            }
            return ret
        }


        /* Check whether this document is provided by ExternalStorageProvider. */
        private fun isExternalStoreDoc(uriAuthority: String?): Boolean {
            var ret = false
            if ("com.android.externalstorage.documents" == uriAuthority) {
                ret = true
            }
            return ret
        }

        /* Check whether this document is provided by DownloadsProvider. */
        private fun isDownloadDoc(uriAuthority: String?): Boolean {
            var ret = false
            if ("com.android.providers.downloads.documents" == uriAuthority) {
                ret = true
            }
            return ret
        }

        /* Check whether this document is provided by MediaProvider. */
        private fun isMediaDoc(uriAuthority: String?): Boolean {
            var ret = false
            if ("com.android.providers.media.documents" == uriAuthority) {
                ret = true
            }
            return ret
        }

        /* Check whether this document is provided by google photos. */
        private fun isGooglePhotoDoc(uriAuthority: String?): Boolean {
            var ret = false
            if ("com.google.android.apps.photos.content" == uriAuthority) {
                ret = true
            }
            return ret
        }

        /* Return uri represented document file real local path.*/
        private fun getImageRealPath(
            contentResolver: ContentResolver,
            uri: Uri,
            whereClause: String?
        ): String? {
            var ret = ""

            // Query the uri with condition.
            val cursor =
                contentResolver.query(uri, null, whereClause, null, null)
            if (cursor != null) {
                val moveToFirst = cursor.moveToFirst()
                if (moveToFirst) {

                    // Get columns name by uri type.
                    var columnName = MediaStore.Images.Media.DATA
                    if (uri === MediaStore.Images.Media.EXTERNAL_CONTENT_URI) {
                        columnName = MediaStore.Images.Media.DATA
                    } else if (uri === MediaStore.Audio.Media.EXTERNAL_CONTENT_URI) {
                        columnName = MediaStore.Audio.Media.DATA
                    } else if (uri === MediaStore.Video.Media.EXTERNAL_CONTENT_URI) {
                        columnName = MediaStore.Video.Media.DATA
                    }

                    // Get column index.
                    val imageColumnIndex = cursor.getColumnIndex(columnName)

                    // Get column value which is the uri related file local path.
                    ret = cursor.getString(imageColumnIndex)
                }
            }
            return ret
        }
    }
}