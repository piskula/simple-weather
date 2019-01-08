package sk.momosi.simplewidget.widget

import android.appwidget.AppWidgetManager
import android.content.Context
import sk.momosi.simplewidget.R

/**
 * Implementation of App Widget functionality.
 */
class Actual3x1Widget : ActualWidgetBase() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        // There may be multiple widgets active, so update all of them
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(
                context,
                appWidgetManager,
                appWidgetId,
                R.layout.actual_3x1_widget,
                this::class
            )
        }
    }
}
