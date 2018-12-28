package sk.momosi.simplewidget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.support.v4.content.ContextCompat
import android.widget.RemoteViews
import java.lang.ref.WeakReference
import java.text.SimpleDateFormat
import java.util.*


/**
 * Implementation of App Widget functionality.
 */
class NewAppWidget : AppWidgetProvider() {

    companion object {

        /*
         * This method is responsible for updating widget
         * update is done periodically, interval is located in xml file
         */
        internal fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
            val views = RemoteViews(context.packageName, R.layout.new_app_widget)

            val permissions = ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_COARSE_LOCATION)
            if (permissions == PackageManager.PERMISSION_GRANTED) {
                RefreshConditions(WeakReference(context)).execute()
            } else {
//                views.setTextViewText(R.id.description, "Permis")
            }

//            Blurry.with(context).radius(25).sampling(2).onto(views as ViewGroup)


            // this intent is responsible for manually updating widget
//            views.setOnClickPendingIntent(R.id.button_update, generateUpdateButtonIntent(context, appWidgetId))
//            views.setTextViewText(R.id.appwidget_refresh,
//                "Refreshed: " + SimpleDateFormat("dd.MM. HH:mm").format(Date(System.currentTimeMillis())))

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }


        private fun generateUpdateButtonIntent(context: Context, appWidgetId: Int): PendingIntent {
            val intentUpdate = Intent(context, NewAppWidget::class.java)
            intentUpdate.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
            intentUpdate.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, intArrayOf(appWidgetId));

            return PendingIntent.getBroadcast(
                context, appWidgetId, intentUpdate,
                PendingIntent.FLAG_UPDATE_CURRENT
            )
        }
    }

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        // There may be multiple widgets active, so update all of them
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }
}
