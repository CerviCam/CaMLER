package id.cervicam.mobile.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import id.cervicam.mobile.R
import id.cervicam.mobile.fragments.Button
import id.cervicam.mobile.helper.Utility
import id.cervicam.mobile.services.MainService
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import okhttp3.Response
import org.json.JSONArray

/**
 * A starter activity when the app is starting
 *
 */
class MainActivity : AppCompatActivity() {
    companion object {
        const val OPEN_CAMERA_REQUEST_CODE = 101
    }

    // List of classification request that has been sent from client to server
    // The value is a dummy for now, should be changed if a server has been implemented
    private val classifications: ArrayList<String> = ArrayList()

    /**
     * Create a view of main activity and set all fragments
     *
     * @param savedInstanceState    Bundle of activity
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        setupAPIService(this)
        runBlocking {
            launch(Dispatchers.Default) {
                val response: Response = MainService.getAllClassifications(this@MainActivity)

                runOnUiThread {
                    if (response.code() == 200) {
                        val body = Utility.parseJSON(response.body()?.string())
                        val result = body["results"] as ArrayList<*>

                        for (element in result) {
                            classifications.add((element as Map<*, *>)["id"].toString())
                        }

                        resultListView.adapter = ArrayAdapter(this@MainActivity, android.R.layout.simple_list_item_1, classifications)
                    }
                }
            }
        }

        resultListView.onItemClickListener = object: AdapterView.OnItemClickListener {
            override fun onItemClick(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                openResultActivity(classifications[position])
            }
        }
        val openCameraButton = Button.newInstance(
            getString(R.string.activity_main_opencamera),
            clickable = true,
            type = Button.ButtonType.FILLED,
            onClick = {
                val openCameraActivityIntent = Intent(this, CameraActivity::class.java)
                startActivity(openCameraActivityIntent)
            }
        )

        supportFragmentManager
            .beginTransaction()
            .replace(R.id.openCameraButton, openCameraButton)
            .commit()
    }

    private fun setupAPIService(context: Context) = runBlocking {
        launch(Dispatchers.Default) {
            Utility.setUser(context)
        }
    }

    /**
     * Open classification result from a given request id
     *
     * @param requestId   Request id of classification
     */
    private fun openResultActivity(requestId: String) {
        val showResultActivity = Intent(this, ResultActivity::class.java)
        showResultActivity.putExtra(ResultActivity.KEY_REQUEST_ID, requestId)
        startActivity(showResultActivity)
    }
}