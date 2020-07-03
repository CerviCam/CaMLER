package id.cervicam.mobile.activities

// Own libraries
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import id.cervicam.mobile.R
import id.cervicam.mobile.fragments.Button
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    companion object {
        const val OPEN_CAMERA_REQUEST_CODE = 101
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        val openCameraButton = Button.newInstance(
            getString(R.string.activity_main_opencamera),
            clickable = true,
            type = Button.ButtonType.FILLED,
            onClick = {
                val openCameraActivityIntent = Intent(this, CameraActivity::class.java)
                startActivityForResult(openCameraActivityIntent, OPEN_CAMERA_REQUEST_CODE)
            }
        )

        val showFirstResultButton = Button.newInstance(
            getString(R.string.activity_main_resultbutton),
            type = Button.ButtonType.OUTLINE,
            onClick = {
                openResultActivity("1")
            }
        )

        supportFragmentManager
            .beginTransaction()
            .replace(R.id.openCameraButton, openCameraButton)
            .replace(R.id.showResultButton, showFirstResultButton)
            .commit()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == OPEN_CAMERA_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                val imagePath: String = data!!.getStringExtra(CameraActivity.KEY_IMAGE_PATH) as String
                sendImageAndOpenResultActivity(imagePath)
            }
        }
    }

    private fun openResultActivity(requestId: String) {
        val showResultActivity = Intent(this, ResultActivity::class.java)
        showResultActivity.putExtra(ResultActivity.KEY_REQUEST_ID, requestId)
        startActivity(showResultActivity)
    }

    private fun sendImageAndOpenResultActivity(imagePath: String) {
        openResultActivity("1") // Dummy request id
//        TODO("Send image to server over HTTP Request and open result activity")
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }
}