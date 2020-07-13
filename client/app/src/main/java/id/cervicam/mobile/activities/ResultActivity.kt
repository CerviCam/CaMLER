package id.cervicam.mobile.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import id.cervicam.mobile.R
import id.cervicam.mobile.fragments.Button
import id.cervicam.mobile.helper.Utility
import id.cervicam.mobile.services.MainService
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.readText
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

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
            val response: HttpResponse = MainService.fetchClassificationResult(requestId)
            if (response.status == HttpStatusCode.OK) {
                Utility.parseJSON(response.readText())
//                setResult(responseBody)
            } else {
                TODO("Implement handler for status code != 200")
            }
        }
    }

//    private fun setResult(responseBody: HashMap<String, Any>) = runBlocking {
//        val imageUri: String = responseBody["thumbnailUrl"] as String
//        Picasso.with(this@ResultActivity).load(imageUri).into(imageView)
//    }
}