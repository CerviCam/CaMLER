package id.cervicam.mobile.activities

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import id.cervicam.mobile.R
import id.cervicam.mobile.fragments.CameraCapture
import id.cervicam.mobile.fragments.CameraPreview
import id.cervicam.mobile.helper.Utility
import java.io.File


class CameraActivity: AppCompatActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        Utility.hideStatusBar(window)
        setContentView(R.layout.activity_camera)
        Utility.setStatusBarColor(window,this, R.color.colorBlack)

        setCameraCaptureFragment()
    }

    private fun setMainFragment(fragment: Fragment) {
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.mainView, fragment)
            .commit()
    }

    private fun setCameraCaptureFragment() {
        val cameraCapture: CameraCapture = CameraCapture.newInstance {
            val cameraPreview: CameraPreview = CameraPreview.newInstance({
                val mediaFile: File = Utility.getOutputDirectory(this, resources)
                val mediaUri: String = mediaFile.path
                if (it.contains(mediaUri)) {
                    val image: File = File(mediaFile, "images/" + Utility.getBasename(it))
                    image.delete()
                }

                setCameraCaptureFragment()
            }, {
                finish()
            }, it)
            setMainFragment(cameraPreview)
        }

        setMainFragment(cameraCapture)
    }
}