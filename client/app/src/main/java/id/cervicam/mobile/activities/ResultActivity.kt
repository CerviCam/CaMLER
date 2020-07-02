package id.cervicam.mobile.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import id.cervicam.mobile.R

class ResultActivity: AppCompatActivity() {
    companion object {
        const val KEY_REQUEST_ID = "REQUEST_ID"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_result)

        val requestId: String = intent.getStringExtra(KEY_REQUEST_ID)
        requestId
    }
}