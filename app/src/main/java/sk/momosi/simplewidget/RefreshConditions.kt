package sk.momosi.simplewidget

import android.annotation.SuppressLint
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.location.Location
import android.os.AsyncTask
import android.text.format.DateUtils
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
import java.lang.Exception
import java.lang.ref.WeakReference

class RefreshConditions(private val context: WeakReference<Context>) : AsyncTask<Void, Void, ResponseDto?>() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    var error: Exception? = null

    override fun onPreExecute() {
        super.onPreExecute()
        Log.e("RefreshConditions", "onPreExecute")
        val ctx = context.get()
        if (ctx != null) {
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(ctx)
        }
    }

    /*
     * This is handled before AsyncTask is called, because request permission dialog
     * cannot be called from non-UI thread. When similar situation occurred, proper
     * "visit app" shortcut should be visible in widget
     */
    @SuppressLint("MissingPermission")
    override fun doInBackground(vararg params: Void?): ResponseDto? {
        val location: Location = Tasks.await(fusedLocationClient.lastLocation)
        return refreshConditionsForCoordinates(location.latitude, location.longitude)
    }

    override fun onPostExecute(result: ResponseDto?) {
        super.onPostExecute(result)
        val ctx = context.get()

        if (ctx != null) {
            if (error != null) {
                setResultError(ctx, error)
            } else {
                setResultOk(ctx, result)
            }
        }
    }

    override fun onCancelled() {
        val ctx = context.get()
        if (ctx != null) setResultError(ctx, null)
    }

    private fun refreshConditionsForCoordinates(lat: Double, lon: Double): ResponseDto? {
        val http = HttpClientBuilder.create().build()
        val getMethod =
            HttpGetHC4("https://api.openweathermap.org/data/2.5/weather?lat=$lat&lon=$lon&appid=edd2d24092e9a212f38ade02c357303e&units=metric")

        try {
            http.use {
                val response = it.execute(getMethod)
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

    private fun setResultError(context: Context, exception: Exception?) {
        Log.e("RefreshConditions", "setResultError called")

        val counter = increaseCounter(false)

        val lastUpdatedAt = context.getSharedPreferences("com.example.android.appwidgetsample", 0).getLong("last_ok", 0L)

        // update widget
        val appWidgetManager = AppWidgetManager.getInstance(context)
        val remoteViews = RemoteViews(context.packageName, R.layout.new_app_widget)
        remoteViews.setTextViewText(R.id.appwidget_last_before, DateUtils.getRelativeTimeSpanString(lastUpdatedAt))
        remoteViews.setTextViewText(R.id.appwidget_err_count, counter.toString())

        val thisWidget = ComponentName(context, NewAppWidget::class.java)
        appWidgetManager.updateAppWidget(thisWidget, remoteViews)
    }

    private fun setResultOk(context: Context, response: ResponseDto?) {
        val temp = response?.main?.temp
        val town = response?.name
        Log.e("RefreshConditions", "Response has came: $temp")

        val counter = increaseCounter(true)

        // update widget
        val appWidgetManager = AppWidgetManager.getInstance(context)
        val remoteViews = RemoteViews(context.packageName, R.layout.new_app_widget)
        remoteViews.setTextViewText(R.id.appwidget_temp, context.resources.getString(R.string.temperature, temp, town))
        remoteViews.setTextViewText(R.id.appwidget_description, response?.weather?.get(0)?.description ?: "-")
        remoteViews.setTextViewText(R.id.appwidget_last_before, "")
        remoteViews.setTextViewText(R.id.appwidget_ok_count, counter.toString())

        val thisWidget = ComponentName(context, NewAppWidget::class.java)
        appWidgetManager.updateAppWidget(thisWidget, remoteViews)
    }

    private fun increaseCounter(isOk: Boolean): Int {
        val prefs = context.get()?.getSharedPreferences("com.example.android.appwidgetsample", 0)
        if (prefs != null) {
            val key = if (isOk) "ok_count" else "error_count"
            val count = prefs.getInt(key, 0) + 1

            val prefEditor = prefs.edit()
            prefEditor.putInt(key, count)

            if (isOk) {
                prefEditor.putLong("last_ok", System.currentTimeMillis())
            }

            prefEditor.apply()

            return count
        }
        return -1
    }

}
