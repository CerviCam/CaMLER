package id.cervicam.mobile.activities

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.ImageFormat
import android.graphics.SurfaceTexture
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import id.cervicam.mobile.R
import id.cervicam.mobile.fragments.Button
import android.hardware.camera2.*
import android.hardware.camera2.params.StreamConfigurationMap
import android.media.ImageReader
import android.os.Handler
import android.os.HandlerThread
import android.util.Size
import android.util.SparseIntArray
import android.view.Surface
import android.view.TextureView
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import kotlinx.android.synthetic.main.activity_camera.*
import java.io.File

class CameraActivity: AppCompatActivity() {
    companion object {
        private val ORIENTATIONS: SparseIntArray = SparseIntArray()

        init {
            ORIENTATIONS.append(Surface.ROTATION_0, 90)
            ORIENTATIONS.append(Surface.ROTATION_90, 0)
            ORIENTATIONS.append(Surface.ROTATION_180, 270)
            ORIENTATIONS.append(Surface.ROTATION_270, 180)
        }
    }

    private lateinit var cameraId: String
    private var cameraDevice: CameraDevice? = null
    private lateinit var cameraCaptureSession: CameraCaptureSession
    private lateinit var captureRequest: CaptureRequest
    private lateinit var captureRequestBuilder: CaptureRequest.Builder

    private lateinit var imageDimensions: Size
    private lateinit var imageReader: ImageReader
    private lateinit var file: File
    private var mBackgroundHandler: Handler? = null
    private var mBackgroundThread: HandlerThread? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)

        val takePictureButton: Button = Button.newInstance(
            "Take",
            onClick = {
                takePicture()
            }
        )

        supportFragmentManager.beginTransaction().replace(R.id.takePictureButtonFrame, takePictureButton).commit()

        cameraView.surfaceTextureListener = textureListener
    }

    private val textureListener: TextureView.SurfaceTextureListener = object: TextureView.SurfaceTextureListener {
        override fun onSurfaceTextureAvailable(surface: SurfaceTexture?, width: Int, height: Int) {
            openCamera()
        }

        override fun onSurfaceTextureDestroyed(surface: SurfaceTexture?): Boolean {
            return false
        }

        override fun onSurfaceTextureSizeChanged(
            surface: SurfaceTexture?,
            width: Int,
            height: Int
        ) {
            // pass
        }

        override fun onSurfaceTextureUpdated(surface: SurfaceTexture?) {
            // pass
        }
    }

    private val stateCallback: CameraDevice.StateCallback = object : CameraDevice.StateCallback() {
        override fun onOpened(camera: CameraDevice) {
            cameraDevice = camera
            createCameraPreview()
        }

        override fun onDisconnected(camera: CameraDevice) {
            cameraDevice?.close()
        }

        override fun onError(camera: CameraDevice, error: Int) {
            cameraDevice?.close()
            cameraDevice = null
        }
    }

    private fun createCameraPreview() {
        val texture: SurfaceTexture = cameraView.surfaceTexture
        texture.setDefaultBufferSize(imageDimensions.width, imageDimensions.height)

        val surface: Surface = Surface(texture)

        captureRequestBuilder = cameraDevice!!.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
        captureRequestBuilder.addTarget(surface)

        cameraDevice!!.createCaptureSession(
            listOf(surface),
            object: CameraCaptureSession.StateCallback() {
                override fun onConfigured(session: CameraCaptureSession) {
                    if (cameraDevice == null) return

                    cameraCaptureSession = session
                    updatePreview()
                }

                override fun onConfigureFailed(cameraCaptureSession: CameraCaptureSession) {
                    Toast.makeText(applicationContext, "Configuration is changed", Toast.LENGTH_LONG).show()
                }
            },
            null
        )
    }

    private fun updatePreview() {
        if (cameraDevice == null) {
            return
        }

        captureRequestBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO)
        cameraCaptureSession.setRepeatingRequest(captureRequestBuilder.build(), null, mBackgroundHandler)
    }

    private fun openCamera() {
        val manager: CameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
        cameraId = manager.cameraIdList[0]
        val characteristics: CameraCharacteristics = manager.getCameraCharacteristics(cameraId)
        val map: StreamConfigurationMap = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
            ?: return
        imageDimensions = map.getOutputSizes(SurfaceTexture::class.java)[0]

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
        ) {
            val neededPermissions: Array<String> = arrayOf<String>(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            ActivityCompat.requestPermissions(this@CameraActivity, neededPermissions, 101)
            return
        }

        manager.openCamera(cameraId, stateCallback, null)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 101 && grantResults[0] == PackageManager.PERMISSION_DENIED) {
            Toast.makeText(this, "Sorry, camera permission is needed", Toast.LENGTH_LONG).show()
        }
    }

    private fun takePicture() {
        if (cameraDevice == null) return

        val manager: CameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
        try {
            val characteristics: CameraCharacteristics = manager.getCameraCharacteristics(cameraDevice!!.id)
            var sizes: Array<Size>? = null
            if (characteristics != null) {
                sizes = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)!!.getOutputSizes(ImageFormat.JPEG)
            }

            var height: Int = 640
            var width: Int = 480
            if (sizes != null && sizes.isNotEmpty()) {
                width = sizes[0].width
                height = sizes[0].height
            }

            val reader: ImageReader = ImageReader.newInstance(width, height, ImageFormat.JPEG, 1)
            val outputSurface: List<Surface> = listOf(reader.surface, Surface(cameraView.surfaceTexture))

            val captureBuilder: CaptureRequest.Builder = cameraDevice!!.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE)
            captureBuilder.addTarget(reader.surface)
            captureBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO)
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
    }

    override fun onResume() {
        super.onResume()

        if (cameraView.isAvailable) {
            try {
                openCamera()
            } catch (e: CameraAccessException) {
                e.printStackTrace()
            }
        } else {
            cameraView.surfaceTextureListener = textureListener
        }
    }

    private fun startBackgroundThread() {
        mBackgroundThread = HandlerThread("Camera Background")
        mBackgroundThread!!.start()
        mBackgroundHandler = Handler(mBackgroundThread!!.looper)
    }

    override fun onPause() {
        super.onPause()
        try {
            stopBackgroundThread()
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
    }

    private fun stopBackgroundThread() {
        mBackgroundThread!!.quitSafely()

        mBackgroundThread!!.join()
        mBackgroundThread = null
        mBackgroundHandler = null
    }
}