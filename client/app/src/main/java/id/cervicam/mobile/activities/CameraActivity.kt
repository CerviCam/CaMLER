package id.cervicam.mobile.activities

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.ImageDecoder
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.ViewOutlineProvider
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import id.cervicam.mobile.R
import id.cervicam.mobile.fragments.Button
import kotlinx.android.synthetic.main.activity_camera.*
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

    var flashIsOn: Boolean = false

    private var cameraProvider: ProcessCameraProvider? = null
    private var selectedCamera: CameraSelector? = null
    private var camera: Camera? = null
    private var imagePreview: Preview? = null
    private var imageCapture: ImageCapture? = null

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)

        val galleryButton: Button = Button.newInstance(
            "Gallery",
            onClick = {
                openGallery()
            }
        )

        val takePictureButton: Button = Button.newInstance(
            "Take",
            onClick = {
                takePicture()
            }
        )

        val flashButton: Button = Button.newInstance(
            "Flash",
            onClick = {
                flashIsOn = !flashIsOn
                if (camera!!.cameraInfo.hasFlashUnit()) {
                    camera!!.cameraControl.enableTorch(flashIsOn)
                }
            }
        )

        cameraLayout.outlineProvider = ViewOutlineProvider.BACKGROUND
        cameraLayout.clipToOutline = true
        cameraView.outlineProvider = ViewOutlineProvider.BACKGROUND
        cameraView.clipToOutline = true

        supportFragmentManager
            .beginTransaction()
            .replace(R.id.takePictureButtonFrame, takePictureButton)
            .replace(R.id.galleryButtonFrame, galleryButton)
            .replace(R.id.flashButtonFrame, flashButton)
            .commit()

        if (hasCameraPermission()) {
            openCamera()
        } else {
            ActivityCompat.requestPermissions(this@CameraActivity, PERMISSIONS, REQUEST_PERMISSION_CODE)
        }
//        cameraView.surfaceTextureListener = textureListener
    }
    /**
     * Check whether all needed permission are granted by client or not
     */
    private fun hasCameraPermission(): Boolean {
        return PERMISSIONS.fold(
            true,
            {allPermissions, permission -> allPermissions && ActivityCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED}
        )
    }

    private fun openCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener(Runnable {
            // Used to bind life-cycle of camera
            cameraProvider = cameraProviderFuture.get()

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

    private fun getOutputDirectory(): File {
        val mediaDir = externalMediaDirs.firstOrNull()?.let {
            File(it, resources.getString(R.string.app_name)).apply { mkdirs() } }

        return if (mediaDir != null && mediaDir.exists()) mediaDir else filesDir
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun takePicture() {
        // Don't take a picture if imageCapture have not been initialized
        if (imageCapture == null) return

        val currentDateTime: LocalDateTime = LocalDateTime.now()
        val dateTimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        val fileName: String = "${dateTimeFormatter.format(currentDateTime)}.jpg"

        val path: File = File("${getOutputDirectory()}/images")
        if (!path.exists()) {
            path.mkdirs()
        }

        val image: File = File(path, fileName)
        val outputOptions = ImageCapture.OutputFileOptions.Builder(image).build()

        imageCapture!!.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    Toast.makeText(baseContext, "Saved", Toast.LENGTH_LONG).show()
                }

                override fun onError(exception: ImageCaptureException) {
                    Toast.makeText(baseContext, "Failed to take a picture, try again", Toast.LENGTH_LONG).show()
                    exception.printStackTrace()
                }
            }
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != Activity.RESULT_OK) return

        if (requestCode == IMAGE_GALLERY_REQUEST_CODE) {
            println(data!!.data!!)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_PERMISSION_CODE && grantResults[0] == PackageManager.PERMISSION_DENIED) {
            Toast.makeText(this, "Sorry, camera permission is needed", Toast.LENGTH_LONG).show()
        } else {
            openCamera()
        }
    }
}