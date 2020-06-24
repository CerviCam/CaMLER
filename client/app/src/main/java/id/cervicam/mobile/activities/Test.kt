package id.cervicam.mobile.activities
//
//import android.Manifest
//import android.content.Context
//import android.content.pm.PackageManager
//import android.graphics.ImageFormat
//import android.graphics.SurfaceTexture
//import android.hardware.camera2.*
//import android.hardware.camera2.CameraCaptureSession.CaptureCallback
//import android.media.Image
//import android.media.ImageReader
//import android.media.ImageReader.OnImageAvailableListener
//import android.os.Bundle
//import android.os.Environment
//import android.os.Handler
//import android.os.HandlerThread
//import android.support.v4.app.ActivityCompat
//import android.support.v7.app.AppCompatActivity
//import android.util.Size
//import android.util.SparseIntArray
//import android.view.Surface
//import android.view.TextureView
//import android.view.TextureView.SurfaceTextureListener
//import android.widget.Button
//import android.widget.Toast
//import java.io.*
//import java.util.*
//
//class MainActivity : AppCompatActivity() {
//    private var btnCapture: Button? = null
//    private var textureView: TextureView? = null
//
//    companion object {
//        //Check state orientation of output image
//        private val ORIENTATIONS = SparseIntArray()
//        private const val REQUEST_CAMERA_PERMISSION = 200
//
//        init {
//            MainActivity.Companion.ORIENTATIONS.append(
//                Surface.ROTATION_0,
//                90
//            )
//            MainActivity.Companion.ORIENTATIONS.append(
//                Surface.ROTATION_90,
//                0
//            )
//            MainActivity.Companion.ORIENTATIONS.append(
//                Surface.ROTATION_180,
//                270
//            )
//            MainActivity.Companion.ORIENTATIONS.append(
//                Surface.ROTATION_270,
//                180
//            )
//        }
//    }
//
//    private var cameraId: String? = null
//    private var cameraDevice: CameraDevice? = null
//    private var cameraCaptureSessions: CameraCaptureSession? = null
//    private var captureRequestBuilder: CaptureRequest.Builder? = null
//    private var imageDimension: Size? = null
//    private val imageReader: ImageReader? = null
//
//    //Save to FILE
//    private var file: File? = null
//    private val mFlashSupported = false
//    private var mBackgroundHandler: Handler? = null
//    private var mBackgroundThread: HandlerThread? = null
//    var stateCallback: CameraDevice.StateCallback = object : CameraDevice.StateCallback() {
//        override fun onOpened(@NonNull camera: CameraDevice) {
//            cameraDevice = camera
//            createCameraPreview()
//        }
//
//        override fun onDisconnected(@NonNull cameraDevice: CameraDevice) {
//            cameraDevice.close()
//        }
//
//        override fun onError(@NonNull cameraDevice: CameraDevice, i: Int) {
//            var cameraDevice: CameraDevice? = cameraDevice
//            cameraDevice!!.close()
//            cameraDevice = null
//        }
//    }
//
//    protected fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_main)
//        textureView = findViewById(R.id.textureView) as TextureView?
//        assert(textureView != null)
//        textureView!!.surfaceTextureListener = textureListener
//        btnCapture = findViewById(R.id.btnCapture) as Button?
//        btnCapture!!.setOnClickListener { takePicture() }
//    }
//
//    private fun takePicture() {
//        if (cameraDevice == null) return
//        val manager =
//            getSystemService(Context.CAMERA_SERVICE) as CameraManager
//        try {
//            val characteristics =
//                manager.getCameraCharacteristics(cameraDevice!!.id)
//            var jpegSizes: Array<Size>? = null
//            if (characteristics != null) jpegSizes =
//                characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
//                    .getOutputSizes(ImageFormat.JPEG)
//
//            //Capture image with custom size
//            var width = 640
//            var height = 480
//            if (jpegSizes != null && jpegSizes.size > 0) {
//                width = jpegSizes[0].width
//                height = jpegSizes[0].height
//            }
//            val reader =
//                ImageReader.newInstance(width, height, ImageFormat.JPEG, 1)
//            val outputSurface: MutableList<Surface> =
//                ArrayList(2)
//            outputSurface.add(reader.surface)
//            outputSurface.add(Surface(textureView!!.surfaceTexture))
//            val captureBuilder =
//                cameraDevice!!.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE)
//            captureBuilder.addTarget(reader.surface)
//            captureBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO)
//
//            //Check orientation base on device
//            val rotation: Int = getWindowManager().getDefaultDisplay().getRotation()
//            captureBuilder.set(
//                CaptureRequest.JPEG_ORIENTATION,
//                MainActivity.Companion.ORIENTATIONS.get(rotation)
//            )
//            file = File(
//                Environment.getExternalStorageDirectory()
//                    .toString() + "/" + UUID.randomUUID().toString() + ".jpg"
//            )
//            val readerListener: OnImageAvailableListener = object : OnImageAvailableListener {
//                override fun onImageAvailable(imageReader: ImageReader) {
//                    var image: Image? = null
//                    try {
//                        image = reader.acquireLatestImage()
//                        val buffer = image.planes[0].buffer
//                        val bytes = ByteArray(buffer.capacity())
//                        buffer[bytes]
//                        save(bytes)
//                    } catch (e: FileNotFoundException) {
//                        e.printStackTrace()
//                    } catch (e: IOException) {
//                        e.printStackTrace()
//                    } finally {
//                        run { image?.close() }
//                    }
//                }
//
//                @Throws(IOException::class)
//                private fun save(bytes: ByteArray) {
//                    var outputStream: OutputStream? = null
//                    try {
//                        outputStream = FileOutputStream(file)
//                        outputStream.write(bytes)
//                    } finally {
//                        outputStream?.close()
//                    }
//                }
//            }
//            reader.setOnImageAvailableListener(readerListener, mBackgroundHandler)
//            val captureListener: CaptureCallback = object : CaptureCallback() {
//                override fun onCaptureCompleted(
//                    @NonNull session: CameraCaptureSession,
//                    @NonNull request: CaptureRequest,
//                    @NonNull result: TotalCaptureResult
//                ) {
//                    super.onCaptureCompleted(session, request, result)
//                    Toast.makeText(this@MainActivity, "Saved $file", Toast.LENGTH_SHORT).show()
//                    createCameraPreview()
//                }
//            }
//            cameraDevice!!.createCaptureSession(
//                outputSurface,
//                object : CameraCaptureSession.StateCallback() {
//                    override fun onConfigured(@NonNull cameraCaptureSession: CameraCaptureSession) {
//                        try {
//                            cameraCaptureSession.capture(
//                                captureBuilder.build(),
//                                captureListener,
//                                mBackgroundHandler
//                            )
//                        } catch (e: CameraAccessException) {
//                            e.printStackTrace()
//                        }
//                    }
//
//                    override fun onConfigureFailed(@NonNull cameraCaptureSession: CameraCaptureSession) {}
//                },
//                mBackgroundHandler
//            )
//        } catch (e: CameraAccessException) {
//            e.printStackTrace()
//        }
//    }
//
//    private fun createCameraPreview() {
//        try {
//            val texture = textureView!!.surfaceTexture!!
//            texture.setDefaultBufferSize(imageDimension!!.width, imageDimension!!.height)
//            val surface = Surface(texture)
//            captureRequestBuilder =
//                cameraDevice!!.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
//            captureRequestBuilder!!.addTarget(surface)
//            cameraDevice!!.createCaptureSession(
//                listOf(surface),
//                object : CameraCaptureSession.StateCallback() {
//                    override fun onConfigured(@NonNull cameraCaptureSession: CameraCaptureSession) {
//                        if (cameraDevice == null) return
//                        cameraCaptureSessions = cameraCaptureSession
//                        updatePreview()
//                    }
//
//                    override fun onConfigureFailed(@NonNull cameraCaptureSession: CameraCaptureSession) {
//                        Toast.makeText(this@MainActivity, "Changed", Toast.LENGTH_SHORT).show()
//                    }
//                },
//                null
//            )
//        } catch (e: CameraAccessException) {
//            e.printStackTrace()
//        }
//    }
//
//    private fun updatePreview() {
//        if (cameraDevice == null) Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show()
//        captureRequestBuilder!!.set(
//            CaptureRequest.CONTROL_MODE,
//            CaptureRequest.CONTROL_MODE_AUTO
//        )
//        try {
//            cameraCaptureSessions!!.setRepeatingRequest(
//                captureRequestBuilder!!.build(),
//                null,
//                mBackgroundHandler
//            )
//        } catch (e: CameraAccessException) {
//            e.printStackTrace()
//        }
//    }
//
//    private fun openCamera() {
//        val manager =
//            getSystemService(Context.CAMERA_SERVICE) as CameraManager
//        try {
//            cameraId = manager.cameraIdList[0]
//            val characteristics = manager.getCameraCharacteristics(cameraId)
//            val map =
//                characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)!!
//            imageDimension = map.getOutputSizes(SurfaceTexture::class.java)[0]
//            //Check realtime permission if run higher API 23
//            if (ActivityCompat.checkSelfPermission(
//                    this,
//                    Manifest.permission.CAMERA
//                ) !== PackageManager.PERMISSION_GRANTED
//            ) {
//                ActivityCompat.requestPermissions(
//                    this,
//                    arrayOf(
//                        Manifest.permission.CAMERA,
//                        Manifest.permission.WRITE_EXTERNAL_STORAGE
//                    ),
//                    MainActivity.Companion.REQUEST_CAMERA_PERMISSION
//                )
//                return
//            }
//            manager.openCamera(cameraId, stateCallback, null)
//        } catch (e: CameraAccessException) {
//            e.printStackTrace()
//        }
//    }
//
//    var textureListener: SurfaceTextureListener = object : SurfaceTextureListener {
//        override fun onSurfaceTextureAvailable(
//            surfaceTexture: SurfaceTexture,
//            i: Int,
//            i1: Int
//        ) {
//            openCamera()
//        }
//
//        override fun onSurfaceTextureSizeChanged(
//            surfaceTexture: SurfaceTexture,
//            i: Int,
//            i1: Int
//        ) {
//        }
//
//        override fun onSurfaceTextureDestroyed(surfaceTexture: SurfaceTexture): Boolean {
//            return false
//        }
//
//        override fun onSurfaceTextureUpdated(surfaceTexture: SurfaceTexture) {}
//    }
//
//    fun onRequestPermissionsResult(
//        requestCode: Int,
//        @NonNull permissions: Array<String?>?,
//        @NonNull grantResults: IntArray
//    ) {
//        if (requestCode == MainActivity.Companion.REQUEST_CAMERA_PERMISSION) {
//            if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
//                Toast.makeText(this, "You can't use camera without permission", Toast.LENGTH_SHORT)
//                    .show()
//                finish()
//            }
//        }
//    }
//
//    protected fun onResume() {
//        super.onResume()
//        startBackgroundThread()
//        if (textureView!!.isAvailable) openCamera() else textureView!!.surfaceTextureListener =
//            textureListener
//    }
//
//    protected fun onPause() {
//        stopBackgroundThread()
//        super.onPause()
//    }
//
//    private fun stopBackgroundThread() {
//        mBackgroundThread!!.quitSafely()
//        try {
//            mBackgroundThread!!.join()
//            mBackgroundThread = null
//            mBackgroundHandler = null
//        } catch (e: InterruptedException) {
//            e.printStackTrace()
//        }
//    }
//
//    private fun startBackgroundThread() {
//        mBackgroundThread = HandlerThread("Camera Background")
//        mBackgroundThread!!.start()
//        mBackgroundHandler = Handler(mBackgroundThread!!.looper)
//    }
//}