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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Utility.hideStatusBar(window)
        Utility.setStatusBarColor(window, this@ImagePreviewActivity, R.color.colorBlack)
        supportActionBar?.hide()
        setContentView(R.layout.activity_image_preview)

        val imagePath: String = intent.getStringExtra(KEY_IMAGE_PATH)!!

        var image = File(imagePath)

        // Compress image if the size is more than 300kb
        if (image.length() >= 300 * 1000) {
            val rawImage = image
            image = File("${cacheDir}/image-preview/${Utility.getBasename(rawImage.path)}")
            rawImage.copyTo(image)
            Utility.compressImage(image.path, 25)
        }

        // A returned data
        val returned = Intent()
        returned.putExtra(KEY_IMAGE_PATH, image.path)

        val prevButton = Button.newInstance(
            getString(R.string.activity_imagepreview_previous),
            type = Button.ButtonType.CLEAN,
            color = ContextCompat.getColor(this, R.color.colorWhite),
            onClick = {
                image.delete()
                setResult(Activity.RESULT_CANCELED, returned)
                finish()
            }
        )

        val nextButton = Button.newInstance(
            getString(R.string.activity_imagepreview_next),
            onClick = {
                image.delete()
                setResult(Activity.RESULT_OK, returned)
                finish()
            }
        )

        supportFragmentManager
            .beginTransaction()
            .replace(R.id.prevButtonView, prevButton)
            .replace(R.id.nextButtonView, nextButton)
            .commit()

        Picasso.with(this)
            .load(image)
            .config(Bitmap.Config.RGB_565)
            .into(imageView, object : com.squareup.picasso.Callback {
                override fun onSuccess() {
                    progressBarContainer.visibility = View.GONE
                    imageContainer.visibility = View.VISIBLE
                }

                override fun onError() {
                    setResult(Activity.RESULT_CANCELED, returned)
                    Toast.makeText(
                        this@ImagePreviewActivity,
                        "Unable to show the image",
                        Toast.LENGTH_LONG
                    ).show()
                    finish()
                }
            })
    }
}