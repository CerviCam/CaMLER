package id.cervicam.mobile.activities

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.squareup.picasso.Picasso
import id.cervicam.mobile.R
import id.cervicam.mobile.fragments.Button
import id.cervicam.mobile.helper.Utility
import kotlinx.android.synthetic.main.activity_image_preview.*
import java.io.File

class ImagePreviewActivity : AppCompatActivity() {
    companion object {
        const val KEY_IMAGE_PATH = "IMAGE_URI"
    }

    private var originalImage: File? = null
    private var previewedImage: File? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Utility.hideStatusBar(window)
        Utility.setStatusBarColor(window, this@ImagePreviewActivity, R.color.colorBlack)
        supportActionBar?.hide()
        setContentView(R.layout.activity_image_preview)

        val imagePath: String = intent.getStringExtra(KEY_IMAGE_PATH)!!

        originalImage = File(imagePath)

        // Compress image if the size is more than 300 Kb
        if (originalImage!!.length() >= 300 * 1000) {
            previewedImage = File("${cacheDir}/image-preview/${Utility.getBasename(originalImage!!.path)}")
            originalImage?.copyTo(previewedImage!!)
            Utility.compressImage(previewedImage!!.path, 25)
        } else {
            // Use original image to preview if the size is small enough
            previewedImage = originalImage
        }

        val prevButton = Button.newInstance(
            getString(R.string.activity_imagepreview_previous),
            type = Button.ButtonType.CLEAN,
            color = ContextCompat.getColor(this, R.color.colorWhite),
            onClick = {
                onBackPressed()
            }
        )

        val nextButton = Button.newInstance(
            getString(R.string.activity_imagepreview_next),
            onClick = {
                sendImageAndOpenResultActivity()
            }
        )

        supportFragmentManager
            .beginTransaction()
            .replace(R.id.prevButtonView, prevButton)
            .replace(R.id.nextButtonView, nextButton)
            .commit()

        Picasso.with(this)
            .load(previewedImage)
            .config(Bitmap.Config.RGB_565)
            .into(imageView, object : com.squareup.picasso.Callback {
                override fun onSuccess() {
                    progressBarContainer.visibility = View.GONE
                    imageContainer.visibility = View.VISIBLE
                }

                override fun onError() {
                    Toast.makeText(
                        this@ImagePreviewActivity,
                        "Unable to show the image",
                        Toast.LENGTH_LONG
                    ).show()
                    setResult(Activity.RESULT_CANCELED)
                    finish()
                }
            })
    }

    override fun onBackPressed() {
        if (originalImage!!.path != previewedImage!!.path) {
            previewedImage!!.delete()
        }
        setResult(Activity.RESULT_CANCELED)
        super.onBackPressed()
    }

    private fun sendImageAndOpenResultActivity() {
        val openResultActivityIntent = Intent(this, ResultActivity::class.java)
        openResultActivityIntent.putExtra(ResultActivity.KEY_REQUEST_ID, "1")                   // Dummy request id
        startActivity(openResultActivityIntent)

        setResult(Activity.RESULT_OK)
        finish()
//        TODO("Send image to server over HTTP Request and open result activity")
    }
}