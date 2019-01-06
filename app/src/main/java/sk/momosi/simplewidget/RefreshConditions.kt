package sk.momosi.simplewidget

import android.annotation.SuppressLint
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.location.Location
import android.os.AsyncTask
import android.util.Log
import android.widget.RemoteViews
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.Tasks
import com.google.gson.Gson
import org.apache.http.client.methods.HttpGetHC4
import org.apache.http.impl.client.HttpClientBuilder
import org.apache.http.util.EntityUtilsHC4
import sk.momosi.simplewidget.entity.ResponseDto
import sk.momosi.simplewidget.entity.WeatherDto
import java.lang.Exception
import java.lang.ref.WeakReference
import kotlin.math.roundToInt


class RefreshConditions(private val context: WeakReference<Context>) : AsyncTask<Void, Void, ResponseDto?>() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    var error: Exception? = null

    override fun onPreExecute() {
        super.onPreExecute()
        Log.e("RefreshConditions", "onPreExecute")
        val ctx = context.get()
        if (ctx != null) {
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(ctx)

            renderWidget(ctx, R.drawable.no_data, ctx.getString(R.string.loading), null)
        }
    }

    /*
     * This is handled before AsyncTask is called, because request permission dialog
     * cannot be called from non-UI thread. When similar situation occurred, proper
     * "visit app" shortcut should be visible in widget
     */
    @SuppressLint("MissingPermission")
    override fun doInBackground(vararg params: Void?): ResponseDto? {
        // TODO handle null lastLocation
        val location: Location = Tasks.await(fusedLocationClient.lastLocation)

        val prefs = context.get()?.getSharedPreferences("com.example.android.appwidgetsample", 0)
        if (prefs != null) {
            val prefEditor = prefs.edit()
            prefEditor.putFloat("last_lat", location.latitude.toFloat())
            prefEditor.putFloat("last_lon", location.longitude.toFloat())

            prefEditor.apply()
        }

        return refreshConditionsForCoordinates(location.latitude, location.longitude)
    }

    override fun onPostExecute(result: ResponseDto?) {
        super.onPostExecute(result)
        val ctx = context.get()

        if (ctx != null) {
            if (error != null) {
                setResultError(ctx)
            } else {
                setResultOk(ctx, result)
            }
        }
    }

    override fun onCancelled() {
        val ctx = context.get()
        if (ctx != null) setResultError(ctx)
    }

    private fun refreshConditionsForCoordinates(lat: Double, lon: Double): ResponseDto? {
        val http = HttpClientBuilder.create().build()
        val url = "https://api.openweathermap.org/data/2.5/weather?lat=$lat&lon=$lon&appid=edd2d24092e9a212f38ade02c357303e&units=metric"
        Log.e("RefreshConditions", "URL: $url")

        try {
            http.use {
                val response = it.execute(HttpGetHC4(url))
                if (response.statusLine.statusCode >= 300) {
                    error = IllegalStateException("Error response ${response.statusLine.statusCode}: $response")
                    return null
                }
                return Gson().fromJson<ResponseDto>(EntityUtilsHC4.toString(response.entity), ResponseDto::class.java)
            }
        } catch (e: Exception) {
            error = e
            return null
        }
    }

    private fun setResultError(context: Context) {
        Log.e("RefreshConditions", "setResultError called")

        // update widget
        renderWidget(context, R.drawable.no_data, "Error", null)
    }

    private fun setResultOk(context: Context, response: ResponseDto?) {
        val temp = response?.main?.temp
        val town = response?.name
        Log.e("RefreshConditions", "Response has came: $temp, ${response?.weather?.get(0)}")

        // update widget
        renderWidget(context, resolveIconId(context, response?.weather?.get(0)), town, temp)
    }

    private fun resolveIconId(context: Context, weatherDto: WeatherDto?): Int {
        var icon = weatherDto?.icon ?: "50d"
        if (arrayListOf("03n", "04n", "09n", "11n", "13n", "50n").contains(icon)) {
            icon = icon.replace('n', 'd')
        }
        return context.resources.getIdentifier("w$icon", "drawable", context.packageName)
    }

    private fun renderWidget(context: Context, drawableId: Int = R.drawable.no_data, city: String? = "-", temp: Double?) {
        val appWidgetManager = AppWidgetManager.getInstance(context)
        val remoteViews = RemoteViews(context.packageName, R.layout.new_app_widget)
//        val controlTimestamp = SimpleDateFormat("HH:mm").format(Date(System.currentTimeMillis()))

        remoteViews.setImageViewResource(R.id.weather_icon, drawableId)
        remoteViews.setTextViewText(R.id.temperature_value, if (temp == null) "-" else context.resources.getString(R.string.temperature, temp.roundToInt()))
        remoteViews.setTextViewText(R.id.place_value, city)

        val thisWidget = ComponentName(context, NewAppWidget::class.java)
        appWidgetManager.updateAppWidget(thisWidget, remoteViews)
    }

}
