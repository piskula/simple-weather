package sk.momosi.simplewidget

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.content.pm.PackageManager
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat


private const val MY_PERMISSION_ACCESS_COARSE_LOCATION = 11

class MainActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val permissionStatus = ContextCompat.checkSelfPermission(this, ACCESS_COARSE_LOCATION)

        if (permissionStatus != PackageManager.PERMISSION_GRANTED) {
            // request access permission Dialog
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.ACCESS_COARSE_LOCATION),
                MY_PERMISSION_ACCESS_COARSE_LOCATION
            )
        }
    }
}
