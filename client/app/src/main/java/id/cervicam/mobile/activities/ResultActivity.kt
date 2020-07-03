package id.cervicam.mobile.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.squareup.picasso.Picasso
import id.cervicam.mobile.R
import id.cervicam.mobile.fragments.Button
import id.cervicam.mobile.helper.Utility
import id.cervicam.mobile.services.MainService
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.readText
import io.ktor.http.HttpStatusCode
import kotlinx.android.synthetic.main.activity_result.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class ResultActivity : AppCompatActivity() {
    companion object {
        const val KEY_REQUEST_ID = "REQUEST_ID"

        const val OPEN_CAMERA_REQUEST_CODE = 201
    }

    override fun onCreate(savedInstanceState: Bundle?): Unit {
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
                startActivityForResult(
                    openCameraActivityIntent,
                    ResultActivity.OPEN_CAMERA_REQUEST_CODE
                )
            }
        )

        supportFragmentManager
            .beginTransaction()
            .replace(R.id.takePictureButton, openCameraButton)
            .commit()
    }

    private fun getAndSetResult(requestId: String) = runBlocking {
        launch(Dispatchers.Default) {
            val response: HttpResponse = MainService.fetchClassificationResult(requestId)
            if (response.status == HttpStatusCode.OK) {
                val responseBody = Utility.parseJSON(response.readText())
//                setResult(responseBody)
            } else {
                TODO("Implement handler for status code != 200")
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == MainActivity.OPEN_CAMERA_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                val imagePath: String =
                    data!!.getStringExtra(CameraActivity.KEY_IMAGE_PATH) as String
                sendImageAndSetResult(imagePath)
            }
        }
    }

    private fun sendImageAndSetResult(imagePath: String) {
//        TODO("Send image over HTTP Request and show the result to this activity")
    }

    private fun setResult(responseBody: HashMap<String, Any>) = runBlocking {
        val imageUri: String = responseBody["thumbnailUrl"] as String
        Picasso.with(this@ResultActivity).load(imageUri).into(imageView)
    }
}