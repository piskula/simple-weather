package sk.momosi.simplewidget

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.content.pm.PackageManager
import android.graphics.drawable.Animatable
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.view.View
import kotlinx.android.synthetic.main.activity_main.*
import android.content.Intent
import android.net.Uri
import java.lang.ref.WeakReference

private const val MY_PERMISSION_ACCESS_COARSE_LOCATION = 11

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initView()
    }

    private fun initView() {
        (imageView.drawable as Animatable).start()

        show_last_location.setOnClickListener {
            val prefs = getSharedPreferences("com.example.android.appwidgetsample", 0)
            val lastLat = prefs.getFloat("last_lat", 0f)
            val lastLon = prefs.getFloat("last_lon", 0f)

            val gmmIntentUri = Uri.parse("geo:$lastLat,$lastLon")
            val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                .setPackage("com.google.android.apps.maps")

            startActivity(mapIntent)
        }
    }

    private fun setGpsEnabled(isEnabled: Boolean) {
        gps_data_disabled.visibility = if (isEnabled) View.GONE else View.VISIBLE
        gps_data_enabled.visibility = if (isEnabled) View.VISIBLE else View.GONE

        if (isEnabled) {
            // when app is open or location permissions are granted, perform refresh
            RefreshConditions(WeakReference(this)).execute()
        }
    }

    override fun onStart() {
        super.onStart()

        val permissionStatus = ContextCompat.checkSelfPermission(this, ACCESS_COARSE_LOCATION)

        if (permissionStatus != PackageManager.PERMISSION_GRANTED) {
            // request access permission Dialog
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.ACCESS_COARSE_LOCATION),
                MY_PERMISSION_ACCESS_COARSE_LOCATION
            )
        } else {
            setGpsEnabled(true)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == MY_PERMISSION_ACCESS_COARSE_LOCATION) {
            val isGpsEnabled = grantResults[0] == 0

            setGpsEnabled(isGpsEnabled)
        }
    }
}
