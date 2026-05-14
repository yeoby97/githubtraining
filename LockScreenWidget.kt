package com.example.lockscreen

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.os.BatteryManager
import android.content.IntentFilter
import android.widget.RemoteViews
import java.text.SimpleDateFormat
import java.util.*

/**
 * Implementation of App Widget functionality.
 */
class LockScreenWidget : AppWidgetProvider() {
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        // There may be multiple widgets active, so update all of them
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onEnabled(context: Context) {
        // Enter relevant functionality for when the first widget is created
    }

    override fun onDisabled(context: Context) {
        // Enter relevant functionality for when the last widget is disabled
    }
}

internal fun updateAppWidget(
    context: Context,
    appWidgetManager: AppWidgetManager,
    appWidgetId: Int
) {
    // Get the current time
    val timeFormat = SimpleDateFormat("hh:mm", Locale.getDefault())
    val dateFormat = SimpleDateFormat("EEE, MMM dd", Locale.getDefault())
    val calendar = Calendar.getInstance()
    val currentTime = timeFormat.format(calendar.time)
    val currentDate = dateFormat.format(calendar.time)
    
    // Construct the RemoteViews object
    val views = RemoteViews(context.packageName, R.layout.lock_screen_widget)
    views.setTextViewText(R.id.timeTextView, currentTime)
    views.setTextViewText(R.id.dateTextView, currentDate)
    
    // Get battery level
    val batteryIntent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
    val level = batteryIntent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
    val scale = batteryIntent?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
    val batteryPct = if (level != -1 && scale != -1) {
        (level / scale.toFloat() * 100).toInt()
    } else {
        -1
    }
    
    // Set battery level to TextView
    if (batteryPct != -1) {
        views.setTextViewText(R.id.batteryIndicator, "$batteryPct%")
    } else {
        views.setTextViewText(R.id.batteryIndicator, "??%")
    }
    
    // Create an Intent to launch the LockScreenActivity when the widget is clicked
    val intent = Intent(context, LockScreenActivity::class.java)
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
    val pendingIntent = PendingIntent.getActivity(
        context, 
        0, 
        intent, 
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )
    views.setOnClickPendingIntent(R.id.widgetContainer, pendingIntent)
    
    // Instruct the widget manager to update the widget
    appWidgetManager.updateAppWidget(appWidgetId, views)
}
