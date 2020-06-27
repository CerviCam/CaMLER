package id.cervicam.mobile.fragments

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import id.cervicam.mobile.R
import id.cervicam.mobile.helper.Utility
import kotlinx.android.synthetic.main.fragment_camera_capture.*
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class CameraCapture: Fragment() {
    companion object {
        private val PERMISSIONS: Array<String> = arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
        )

        private const val REQUEST_PERMISSION_CODE: Int = 101
        private const val IMAGE_GALLERY_REQUEST_CODE: Int = 2001

        fun newInstance(lookPreview: (String) -> Unit): CameraCapture {
            val cameraCapture: CameraCapture = CameraCapture()
            cameraCapture.lookPreview = lookPreview

            return cameraCapture
        }
    }

    private var lookPreview: ((String) -> Unit)? = null
    private var flashIsOn: Boolean = false
    private var cameraProvider: ProcessCameraProvider? = null
    private var selectedCamera: CameraSelector? = null
    private var camera: Camera? = null
    private var imagePreview: Preview? = null
    private var imageCapture: ImageCapture? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_camera_capture, container, false)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setListeners()

        if (hasCameraPermission()) {
            openCamera()
        } else {
            activity?.let { ActivityCompat.requestPermissions(it, PERMISSIONS, REQUEST_PERMISSION_CODE) }
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
            print(flashIsOn)
            if (camera!!.cameraInfo.hasFlashUnit()) {
                flashIsOn = !flashIsOn
                camera!!.cameraControl.enableTorch(flashIsOn)
            } else {
                Toast.makeText(context, "Unable to use flash", Toast.LENGTH_LONG)
            }
        }
    }

    /**
     * Check whether all needed permission are granted by client or not
     */
    private fun hasCameraPermission(): Boolean {
        return PERMISSIONS.fold(
            true,
            {allPermissions, permission -> allPermissions && context?.let {
                ActivityCompat.checkSelfPermission(
                    it, permission)
            } == PackageManager.PERMISSION_GRANTED}
        )
    }

    private fun openCamera() {
        val cameraProviderFuture = context?.let { ProcessCameraProvider.getInstance(it) }

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
        }, ContextCompat.getMainExecutor(context))
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
            Toast.makeText(context, "Something is wrong", Toast.LENGTH_LONG)
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

        val imageFolder: File = File("${context?.let { Utility.getOutputDirectory(it, resources) }}/images")
        if (!imageFolder.exists()) {
            imageFolder.mkdirs()
        }

        val image: File = File(imageFolder, fileName)
        val outputOptions = ImageCapture.OutputFileOptions.Builder(image).build()

        imageCapture!!.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(context),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    Toast.makeText(context, "Saved", Toast.LENGTH_LONG).show()
                    val path: String = "${context?.let { it1 -> Utility.getOutputDirectory(it1, resources).path }}/images/${fileName}"
                    lookPreview?.let { it(path) }
                }

                override fun onError(exception: ImageCaptureException) {
                    Toast.makeText(context, "Failed to take a picture, try again", Toast.LENGTH_LONG).show()
                    exception.printStackTrace()
                }
            }
        )
    }

    private fun getPath(uri: Uri): String? {
        val projection = arrayOf(MediaStore.Images.Media._ID)
        val cursor: Cursor = uri.let { context?.contentResolver?.query(it, projection, null, null, null) }
            ?: return null
        val columnIndex: Int = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
        cursor.moveToFirst()
        val s: String = cursor.getString(columnIndex)
        cursor.close()
        return s
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != Activity.RESULT_OK) return

        if (requestCode == IMAGE_GALLERY_REQUEST_CODE) {
            val imageUri: Uri = data!!.data!! as Uri
            println(File(getPath(imageUri)).exists())
            lookPreview?.let { imageUri.path?.let { it1 -> it(it1) } }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_PERMISSION_CODE && grantResults[0] == PackageManager.PERMISSION_DENIED) {
            Toast.makeText(context, "Sorry, camera permission is needed", Toast.LENGTH_LONG).show()
        } else {
            openCamera()
        }
    }
}