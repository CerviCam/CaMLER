package id.cervicam.mobile.activities

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import id.cervicam.mobile.R
import kotlinx.android.synthetic.main.activity_camera.*
import id.cervicam.mobile.helper.Utility
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


class CameraActivity: AppCompatActivity() {
    companion object {
        private val PERMISSIONS: Array<String> = arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
        )

        private const val REQUEST_PERMISSION_CODE: Int = 101
        private const val IMAGE_GALLERY_REQUEST_CODE: Int = 2001
    }

    private var flashIsOn: Boolean = false
    private var cameraProvider: ProcessCameraProvider? = null
    private var selectedCamera: CameraSelector? = null
    private var camera: Camera? = null
    private var imagePreview: Preview? = null
    private var imageCapture: ImageCapture? = null

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Utility.hideStatusBar(window)
        Utility.setStatusBarColor(window,this@CameraActivity, R.color.colorBlack)
        supportActionBar?.hide()
        setContentView(R.layout.activity_camera)
        setListeners()

        if (hasCameraPermission()) {
            openCamera()
        } else {
            this.let { ActivityCompat.requestPermissions(it, PERMISSIONS, REQUEST_PERMISSION_CODE) }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setListeners() {
        galleryButton.setOnClickListener {
            openGallery()
        }

        takePictureButton.setOnClickListener {
            takePicture()
        }

        flashButton.setOnClickListener {
            if (camera!!.cameraInfo.hasFlashUnit()) {
                flashIsOn = !flashIsOn
                camera!!.cameraControl.enableTorch(flashIsOn)
            } else {
                Toast.makeText(this, "Unable to use flash", Toast.LENGTH_LONG)
            }
        }
    }

    /**
     * Check whether all needed permission are granted by client or not
     */
    private fun hasCameraPermission(): Boolean {
        return PERMISSIONS.fold(
            true,
            {allPermissions, permission -> allPermissions && this.let {
                ActivityCompat.checkSelfPermission(
                    it, permission)
            } == PackageManager.PERMISSION_GRANTED}
        )
    }

    private fun openCamera() {
        val cameraProviderFuture = this.let { ProcessCameraProvider.getInstance(it) }

        cameraProviderFuture!!.addListener(Runnable {
            // Used to bind life-cycle of camera
            cameraProvider = cameraProviderFuture!!.get()

            // Set camera preview
            imagePreview = Preview.Builder().build()

            // Set image capture
            imageCapture = ImageCapture.Builder().build()

            // Select back camera
            selectedCamera = CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_BACK).build()

            setCameraCycles()
        }, ContextCompat.getMainExecutor(this))
    }

    private fun openGallery() {
        Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI).apply {
            startActivityForResult(this, IMAGE_GALLERY_REQUEST_CODE)
        }
    }

    private fun setCameraCycles() {
        if (cameraProvider == null || selectedCamera == null || imagePreview == null || imageCapture == null) return

        try {
            cameraProvider!!.unbindAll()
            camera = cameraProvider!!.bindToLifecycle(this, selectedCamera!!, imagePreview, imageCapture)
            imagePreview?.setSurfaceProvider(cameraView.createSurfaceProvider(camera?.cameraInfo))
        } catch (e: Exception) {
            Toast.makeText(this, "Something is wrong", Toast.LENGTH_LONG)
            e.printStackTrace()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun takePicture() {
        // Don't take a picture if imageCapture have not been initialized
        if (imageCapture == null) return

        val currentDateTime: LocalDateTime = LocalDateTime.now()
        val dateTimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        val fileName: String = "${dateTimeFormatter.format(currentDateTime)}.jpg"

        val imageFolder: File = File("${Utility.getOutputDirectory(this@CameraActivity, resources).path}/images")
        if (!imageFolder.exists()) {
            imageFolder.mkdirs()
        }

        val image: File = File(imageFolder, fileName)
        val outputOptions = ImageCapture.OutputFileOptions.Builder(image).build()

        imageCapture!!.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    Toast.makeText(this@CameraActivity, "Saved", Toast.LENGTH_LONG).show()
                    val path: String = "${Utility.getOutputDirectory(this@CameraActivity, resources).path}/images/${fileName}"
                    lookPreview(path)
                }

                override fun onError(exception: ImageCaptureException) {
                    Toast.makeText(this@CameraActivity, "Failed to take a picture, try again", Toast.LENGTH_LONG).show()
                    exception.printStackTrace()
                }
            }
        )
    }

    private fun lookPreview(path: String) {
        val openImagePreviewIntent = Intent(this, ImagePreviewActivity::class.java)
        openImagePreviewIntent.putExtra(ImagePreviewActivity.ARG_IMAGE_URI, path)
        startActivity(openImagePreviewIntent)
    }

    private fun getPath(uri: Uri): String? {
        val result: String
        val cursor: Cursor? = this.contentResolver?.query(uri, null, null, null, null)
        if (cursor == null) { // Source is Dropbox or other similar local file path
            result = uri.path.toString()
        } else {
            cursor.moveToFirst()
            val idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA)
            result = cursor.getString(idx)
            cursor.close()
        }
        return result
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != Activity.RESULT_OK) return

        if (requestCode == IMAGE_GALLERY_REQUEST_CODE) {
            val imageUri: Uri = data!!.data!! as Uri
//            println(this.let { Utility.getUriRealPath(it, imageUri) })
//            println(getPath(imageUri))
//            println("=====================================================")
            Utility.getUriRealPath(this, imageUri)?.let { lookPreview(it) }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_PERMISSION_CODE && grantResults[0] == PackageManager.PERMISSION_DENIED) {
            Toast.makeText(this@CameraActivity, "Sorry, camera permission is needed", Toast.LENGTH_LONG).show()
        } else {
            openCamera()
        }
    }
}