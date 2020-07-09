package id.cervicam.mobile.activities

// Own libraries
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import id.cervicam.mobile.R
import id.cervicam.mobile.fragments.Button
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    companion object {
        const val OPEN_CAMERA_REQUEST_CODE = 101
    }

    private val results: Array<Pair<String, Int>> =  arrayOf(
        Pair("Result 1", 1),
        Pair("Result 2", 2)
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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

    private fun openResultActivity(requestId: String) {
        val showResultActivity = Intent(this, ResultActivity::class.java)
        showResultActivity.putExtra(ResultActivity.KEY_REQUEST_ID, requestId)
        startActivity(showResultActivity)
    }
}