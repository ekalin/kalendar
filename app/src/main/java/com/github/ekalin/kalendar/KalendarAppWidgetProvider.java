package com.github.ekalin.kalendar;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;

import org.joda.time.DateTime;

import com.github.ekalin.kalendar.prefs.AllSettings;
import com.github.ekalin.kalendar.prefs.InstanceSettings;
import com.github.ekalin.kalendar.util.DateUtil;

import java.util.HashMap;
import java.util.Map;

public class KalendarAppWidgetProvider extends AppWidgetProvider {
    private static final int MINIMUM_UPDATE_MINUTES = 60;

    private static final Map<Integer, KalendarRemoteViewsFactory> factories = new HashMap<>();

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int widgetId : appWidgetIds) {
            DateTime nextUpdate = getFactory(context, widgetId).updateWidget();
            scheduleNextUpdate(context, widgetId, nextUpdate);
        }
        KalendarUpdater.registerReceivers(context, false);
    }

    private KalendarRemoteViewsFactory getFactory(Context context, int widgetId) {
        return factories.computeIfAbsent(widgetId, (id) -> new KalendarRemoteViewsFactory(context, id));
    }

    private void scheduleNextUpdate(Context context, int widgetId, DateTime nextUpdate) {
        InstanceSettings settings = AllSettings.instanceFromId(context, widgetId);

        DateTime now = DateUtil.now(settings.getTimeZone());

        DateTime minimumUpdateInterval = now.plusMinutes(MINIMUM_UPDATE_MINUTES);
        if (minimumUpdateInterval.isBefore(nextUpdate)) {
            nextUpdate = minimumUpdateInterval;
        }

        KalendarUpdater.scheduleNextUpdate(settings, nextUpdate);
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
