package id.cervicam.mobile.activities

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.squareup.picasso.Picasso
import id.cervicam.mobile.R
import id.cervicam.mobile.fragments.Button
import id.cervicam.mobile.helper.Utility
import id.cervicam.mobile.services.MainService
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.readText
import io.ktor.http.HttpStatusCode
import kotlinx.android.synthetic.main.activity_image_preview.*
import kotlinx.android.synthetic.main.activity_image_preview.imageView
import kotlinx.android.synthetic.main.activity_result.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import okhttp3.Response

/**
 * Show classification result
 *
 */
class ResultActivity : AppCompatActivity() {
    companion object {
        const val KEY_REQUEST_ID = "REQUEST_ID"
    }

    /**
     * Create a view
     *
     * @param savedInstanceState    Bundle of activity
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_result)

        val requestId: String = intent.getStringExtra(KEY_REQUEST_ID) as String
        getAndSetResult(requestId)

        val openCameraButton = Button.newInstance(
            getString(R.string.activity_main_opencamera),
            clickable = true,
            type = Button.ButtonType.FILLED,
            onClick = {
                val openCameraActivityIntent = Intent(this, CameraActivity::class.java)
                startActivity(openCameraActivityIntent)
                finish()
            }
        )

        supportFragmentManager
            .beginTransaction()
            .replace(R.id.takePictureButton, openCameraButton)
            .commit()
    }

    /**
     * Get classification result from server and set it to the page
     *
     * @param requestId     Request id of classification
     */
    private fun getAndSetResult(requestId: String) = runBlocking {
        launch(Dispatchers.Default) {
            val response: Response = MainService.getClassification(this@ResultActivity, requestId)
            runOnUiThread {
                if (response.code() == 200) {
                    val body = Utility.parseJSON(response.body()?.string())
                    Picasso.with(this@ResultActivity)
                        .load(body["image_url"].toString())
                        .config(Bitmap.Config.RGB_565)
                        .into(imageView)

                    statusTextView.text = (body["status"] as HashMap<*, *>)["label"].toString()
                    resultTextView.text = (body["result"] as HashMap<*, *>)["label"].toString()
                } else {
                    Toast.makeText(this@ResultActivity, "Unable to get the classification, try again later", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

//    private fun setResult(responseBody: HashMap<String, Any>) = runBlocking {
//        val imageUri: String = responseBody["thumbnailUrl"] as String
//        Picasso.with(this@ResultActivity).load(imageUri).into(imageView)
//    }
}