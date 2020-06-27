package id.cervicam.mobile.fragments

import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import id.cervicam.mobile.R
import id.cervicam.mobile.helper.Utility
import kotlinx.android.synthetic.main.fragment_camera_preview.*
import java.io.File

class CameraPreview: Fragment() {
    companion object {
        private const val ARG_IMAGE_URI = "IMAGE_URI"
        fun newInstance(prevAction: () -> Unit, nextAction: () -> Unit, imageUri: String): CameraPreview {
            val cameraPreview: CameraPreview = CameraPreview()
            cameraPreview.prevAction = prevAction
            cameraPreview.nextAction = nextAction

            val arguments: Bundle = Bundle()
            arguments.putString(ARG_IMAGE_URI, imageUri)
            cameraPreview.arguments = arguments
            return cameraPreview
        }
    }

    private var prevAction: (() -> Unit)? = null
    private var nextAction: (() -> Unit)? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_camera_preview, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val prevButton: Button = Button.newInstance(
            "Ulangi",
            onClick = {
                prevAction!!()
            }
        )

        val nextButton: Button = Button.newInstance(
            "Lanjut",
            onClick = {
                nextAction!!()
            }
        )

        fragmentManager
            ?.beginTransaction()
            ?.replace(R.id.prevButtonView, prevButton)
            ?.replace(R.id.nextButtonView, nextButton)
            ?.commit()

        val imageUri: String = arguments?.getString(ARG_IMAGE_URI)?: return
        print(imageUri)
        print(" ")
        println(File(imageUri).exists())
        val bmImg = BitmapFactory.decodeFile(imageUri)
        imageView.setImageBitmap(bmImg)
    }
}