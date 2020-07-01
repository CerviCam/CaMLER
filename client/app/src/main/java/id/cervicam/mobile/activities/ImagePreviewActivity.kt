package id.cervicam.mobile.activities

import android.app.Activity
import android.graphics.Bitmap
import android.os.AsyncTask
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.squareup.picasso.Picasso
import id.cervicam.mobile.R
import id.cervicam.mobile.fragments.Button
import id.cervicam.mobile.helper.Utility
import kotlinx.android.synthetic.main.activity_image_preview.*
import java.io.File


class ImagePreviewActivity: AppCompatActivity() {
    companion object {
        public const val ARG_IMAGE_URI = "IMAGE_URI"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Utility.hideStatusBar(window)
        Utility.setStatusBarColor(window,this@ImagePreviewActivity, R.color.colorBlack)
        supportActionBar?.hide()
        setContentView(R.layout.activity_image_preview)

        val prevButton: Button = Button.newInstance(
            "Ulangi",
            onClick = {
                setResult(Activity.RESULT_CANCELED)
                finish()
            }
        )

        val nextButton: Button = Button.newInstance(
            "Lanjut",
            onClick = {
                setResult(Activity.RESULT_OK)
                finish()
            }
        )

        supportFragmentManager
            ?.beginTransaction()
            ?.replace(R.id.prevButtonView, prevButton)
            ?.replace(R.id.nextButtonView, nextButton)
            ?.commit()

        val imageUri: String = intent.getStringExtra(ARG_IMAGE_URI)!!
        println("----------------------------------------------------------")
        print(imageUri)
        print(" ")
        println(File(imageUri).exists())

//        object : AsyncTask<String?, Void?, Void?>() {
//            protected fun doInBackground(vararg params: String): Void? {
//                val path = params[0]
//                val resizedBitmap: Bitmap = ImageUtils.getResizedBitmap(200, 200, PATH_TO_IMAGE)
//                this.runOnUiThread(Runnable { imageView.setImageBitmap(resizedBitmap) })
//                return null
//            }
//        }.execute(imageLoadPath)

        progressBar.visibility = View.VISIBLE
        val picasso = Picasso.Builder(this@ImagePreviewActivity)
            .listener { picasso, uri, exception ->
                println("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++")
                exception.printStackTrace()
                println("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++")
                //Here your log
            }
            .build()
        picasso.load("file://$imageUri").config(Bitmap.Config.RGB_565).into(imageView, object: com.squareup.picasso.Callback {
            override fun onSuccess() {
                println("success")
                progressBar.visibility = View.GONE
            }

            override fun onError() {
                println("error")
                progressBar.visibility = View.GONE
            }
        })
    }
}