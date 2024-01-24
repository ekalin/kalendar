package com.github.ekalin.kalendar;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.util.Log;

import com.github.ekalin.kalendar.prefs.AllSettings;

public class KalendarAppWidgetProvider extends AppWidgetProvider {
    private static final String TAG = KalendarAppWidgetProvider.class.getSimpleName();

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int widgetId : appWidgetIds) {
            KalendarRemoteViewsFactory.updateWidget(context, widgetId, null);
            notifyWidgetDataChanged(context, widgetId);
        }
    }

    private static void notifyWidgetDataChanged(Context context, int widgetId) {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        if (appWidgetManager != null) {
            appWidgetManager.notifyAppWidgetViewDataChanged(new int[]{widgetId}, R.id.event_list);
        } else {
            Log.d(TAG, widgetId + " notifyWidgetDataChanged, appWidgetManager is null, context:" + context);
        }
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        super.onDeleted(context, appWidgetIds);
        for (int widgetId : appWidgetIds) {
            AllSettings.delete(context, widgetId);
        }
    }

    public static int[] getWidgetIds(Context context) {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        return appWidgetManager == null
                ? new int[]{}
                : appWidgetManager.getAppWidgetIds(new ComponentName(context, KalendarAppWidgetProvider.class));
    }
}
