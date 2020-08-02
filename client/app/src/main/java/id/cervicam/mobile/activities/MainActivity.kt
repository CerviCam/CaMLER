package id.cervicam.mobile.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import id.cervicam.mobile.R
import id.cervicam.mobile.fragments.Button
import id.cervicam.mobile.helper.Utility
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.runBlocking

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
    private val results: Array<Pair<String, Int>> =  arrayOf()

    /**
     * Create a view of main activity and set all fragments
     *
     * @param savedInstanceState    Bundle of activity
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Utility.setAccount(this)

        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        resultListView.adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, results.map {
            it.first
        })
        resultListView.onItemClickListener = object: AdapterView.OnItemClickListener {
            override fun onItemClick(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                openResultActivity(results[position].second.toString())
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