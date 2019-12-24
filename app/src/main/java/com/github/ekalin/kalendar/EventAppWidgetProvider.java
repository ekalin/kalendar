package com.github.ekalin.kalendar;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.github.ekalin.kalendar.prefs.AllSettings;

public class EventAppWidgetProvider extends AppWidgetProvider {
    private static final String TAG = EventAppWidgetProvider.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (AppWidgetManager.ACTION_APPWIDGET_UPDATE.equals(action)) {
            Bundle extras = intent.getExtras();
            int[] widgetIds = extras == null
                    ? null
                    : extras.getIntArray(AppWidgetManager.EXTRA_APPWIDGET_IDS);
            if (widgetIds == null || widgetIds.length == 0) {
                widgetIds = getWidgetIds(context);
            }
            if (widgetIds != null && widgetIds.length > 0) {
                onUpdate(context, AppWidgetManager.getInstance(context), widgetIds);
            }
        } else {
            super.onReceive(context, intent);
        }
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        super.onDeleted(context, appWidgetIds);
        for (int widgetId : appWidgetIds) {
            AllSettings.delete(context, widgetId);
        }
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int widgetId : appWidgetIds) {
            EventRemoteViewsFactory.updateWidget(context, widgetId, null);
            notifyWidgetDataChanged(context, widgetId);
        }
    }

    public static int[] getWidgetIds(Context context) {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        return appWidgetManager == null
                ? new int[]{}
                : appWidgetManager.getAppWidgetIds(new ComponentName(context, EventAppWidgetProvider.class));
    }

    private static void notifyWidgetDataChanged(Context context, int widgetId) {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        if (appWidgetManager != null) {
            appWidgetManager.notifyAppWidgetViewDataChanged(new int[]{widgetId}, R.id.event_list);
        } else {
            Log.d(TAG, widgetId + " notifyWidgetDataChanged, appWidgetManager is null, context:" + context);
        }
    }
}
