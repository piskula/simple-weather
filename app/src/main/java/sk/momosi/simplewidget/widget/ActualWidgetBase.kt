package sk.momosi.simplewidget.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.support.v4.content.ContextCompat
import android.widget.RemoteViews
import sk.momosi.simplewidget.R
import sk.momosi.simplewidget.RefreshConditions
import java.lang.ref.WeakReference
import kotlin.reflect.KClass

/**
 * Implementation of App Widget functionality.
 */
abstract class ActualWidgetBase : AppWidgetProvider() {

    companion object {

        /*
         * This method is responsible for updating widget
         * update is done periodically, interval is located in xml file
         */
        internal fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int,
            widgetLayoutId: Int,
            clazz: KClass<*>
        ) {
            val views = RemoteViews(context.packageName, widgetLayoutId)

            val permissions = ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_COARSE_LOCATION)
            if (permissions == PackageManager.PERMISSION_GRANTED) {
                RefreshConditions(WeakReference(context), widgetLayoutId, clazz).execute()
            } else {
                views.setImageViewResource(
                    R.id.weather_icon,
                    R.drawable.unavailable
                )
                views.setTextViewText(R.id.place_value, context.getString(R.string.visit_app))
            }

            // this intent is responsible for manually updating widget
            views.setOnClickPendingIntent(
                R.id.actual_widget_wrapper,
                generateUpdateButtonIntent(context, appWidgetId, clazz.java)
            )

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }


        private fun generateUpdateButtonIntent(context: Context, appWidgetId: Int, clazz: Class<*>): PendingIntent {
            val intentUpdate = Intent(context, clazz)
            intentUpdate.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
            intentUpdate.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, intArrayOf(appWidgetId));

            return PendingIntent.getBroadcast(
                context, appWidgetId, intentUpdate,
                PendingIntent.FLAG_UPDATE_CURRENT
            )
        }
    }
}
