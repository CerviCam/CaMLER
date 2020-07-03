package id.cervicam.mobile.activities

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
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
import id.cervicam.mobile.helper.Utility
import kotlinx.android.synthetic.main.activity_camera.*
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


class CameraActivity : AppCompatActivity() {
    companion object {
        private val PERMISSIONS: Array<String> = arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
        )

        private const val REQUEST_PERMISSION_CODE = 101
        private const val IMAGE_GALLERY_REQUEST_CODE = 2001
        private const val IMAGE_PREVIEW_ACTIVITY_REQUEST_CODE = 90

        const val KEY_IMAGE_PATH = "IMAGE_PATH"
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
        Utility.setStatusBarColor(window, this@CameraActivity, R.color.colorBlack)
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
            { allPermissions, permission ->
                allPermissions && this.let {
                    ActivityCompat.checkSelfPermission(
                        it, permission
                    )
                } == PackageManager.PERMISSION_GRANTED
            }
        )
    }

    private fun openCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture!!.addListener(Runnable {
            // Used to bind life-cycle of camera
            cameraProvider = cameraProviderFuture!!.get()

            // Set camera preview
            imagePreview = Preview.Builder().build()

            // Set image capture
            imageCapture = ImageCapture.Builder().build()

            // Select back camera
            selectedCamera =
                CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_BACK).build()

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
            camera =
                cameraProvider!!.bindToLifecycle(this, selectedCamera!!, imagePreview, imageCapture)
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

        val currentDateTime = LocalDateTime.now()
        val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        val fileName: String = "${dateTimeFormatter.format(currentDateTime)}.jpg"

        val imageFolder = File("${cacheDir.path}/images")
        if (!imageFolder.exists()) {
            imageFolder.mkdirs()
        }
        val image = File(imageFolder, fileName)
        val outputOptions = ImageCapture.OutputFileOptions.Builder(image).build()

        imageCapture!!.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    val path = image.path
                    lookPreview(path)
                }

                override fun onError(exception: ImageCaptureException) {
                    Toast.makeText(
                        this@CameraActivity,
                        "Failed to take a picture, try again",
                        Toast.LENGTH_LONG
                    ).show()
                    exception.printStackTrace()
                }
            }
        )
    }

    private fun lookPreview(path: String) {
        val openImagePreviewIntent = Intent(this, ImagePreviewActivity::class.java)
        openImagePreviewIntent.putExtra(ImagePreviewActivity.KEY_IMAGE_PATH, path)
        startActivityForResult(openImagePreviewIntent, IMAGE_PREVIEW_ACTIVITY_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == IMAGE_GALLERY_REQUEST_CODE) {
            if (resultCode != Activity.RESULT_OK) return

            val imageUri: Uri = data!!.data!! as Uri
            Utility.getFile(this, imageUri)?.path?.let { lookPreview(it) }
        } else if (requestCode == IMAGE_PREVIEW_ACTIVITY_REQUEST_CODE) {
            val path: String = data!!.getStringExtra(ImagePreviewActivity.KEY_IMAGE_PATH)!!
            if (resultCode == Activity.RESULT_CANCELED) {
                if (path.contains("cache")) {
                    File(path).delete()
                }
            } else if (resultCode == Activity.RESULT_OK) {
                val returned = Intent()
                val longLifeImage = File("${Utility.getOutputDirectory(this@CameraActivity, resources).path}/images/${Utility.getBasename(path)}")
                File(path).copyTo(longLifeImage)
                Toast.makeText(this@CameraActivity, "Saved", Toast.LENGTH_LONG).show()

                returned.putExtra(KEY_IMAGE_PATH, longLifeImage.path)
                setResult(Activity.RESULT_OK, returned)
                finish()
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_PERMISSION_CODE && grantResults[0] == PackageManager.PERMISSION_DENIED) {
            Toast.makeText(
                this@CameraActivity,
                "Sorry, camera permission is needed",
                Toast.LENGTH_LONG
            ).show()
        } else {
            openCamera()
        }
    }
}