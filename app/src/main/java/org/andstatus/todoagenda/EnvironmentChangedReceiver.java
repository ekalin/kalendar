package org.andstatus.todoagenda;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.util.Log;

import org.andstatus.todoagenda.prefs.AllSettings;
import org.andstatus.todoagenda.prefs.InstanceSettings;
import org.andstatus.todoagenda.task.dmfs.DmfsOpenTasksContract;
import org.andstatus.todoagenda.util.DateUtil;
import org.joda.time.DateTime;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import static org.andstatus.todoagenda.EventAppWidgetProvider.getWidgetIds;
import static org.andstatus.todoagenda.EventRemoteViewsFactory.ACTION_REFRESH;

public class EnvironmentChangedReceiver extends BroadcastReceiver {
    private static final String TAG = EnvironmentChangedReceiver.class.getSimpleName();
    private static final AtomicReference<EnvironmentChangedReceiver> registeredReceiver = new AtomicReference<>();

    public static void registerReceivers(Map<Integer, InstanceSettings> instances) {
        if (instances.isEmpty()) {
            return;
        }

        InstanceSettings instanceSettings = instances.values().iterator().next();
        Context context = instanceSettings.getContext().getApplicationContext();
        synchronized (registeredReceiver) {
            EnvironmentChangedReceiver receiver = new EnvironmentChangedReceiver();

            IntentFilter providerChanged = new IntentFilter();
            providerChanged.addAction(Intent.ACTION_PROVIDER_CHANGED);
            providerChanged.addDataScheme("content");
            providerChanged.addDataAuthority("com.android.calendar", null);
            providerChanged.addDataAuthority(DmfsOpenTasksContract.AUTHORITY, null);
            context.registerReceiver(receiver, providerChanged);

            IntentFilter filter = new IntentFilter();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                filter.addAction(Intent.ACTION_DREAMING_STOPPED);
            }
            context.registerReceiver(receiver, filter);

            EnvironmentChangedReceiver oldReceiver = registeredReceiver.getAndSet(receiver);
            if (oldReceiver != null) {
                oldReceiver.unRegister(context);
            }
            scheduleNextAlarms(context, instances);

            Log.i(TAG, "Registered receivers from " + instanceSettings.getContext().getClass().getName());
        }
    }

    private static void scheduleNextAlarms(Context context, Map<Integer, InstanceSettings> instances) {
        Set<DateTime> alarmTimes = new HashSet<>();
        for (InstanceSettings settings : instances.values()) {
            alarmTimes.add(DateUtil.now(settings.getTimeZone()).withTimeAtStartOfDay().plusDays(1));
        }
        int counter = 0;
        for (DateTime alarmTime : alarmTimes) {
            Intent intent = new Intent(context, EnvironmentChangedReceiver.class);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context,
                    EventRemoteViewsFactory.REQUEST_CODE_MIDNIGHT_ALARM + counter,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT);
            AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            am.set(AlarmManager.RTC, alarmTime.getMillis(), pendingIntent);
            counter++;
        }
    }

    private void unRegister(Context context) {
        context.unregisterReceiver(this);
    }

    public static void scheduleNextUpdate(InstanceSettings settings, DateTime nextUpdate) {
        Log.i(TAG, "Setting next update for id " + settings.getWidgetId() + " for " + nextUpdate);

        Intent intent = new Intent(settings.getContext(), EnvironmentChangedReceiver.class);
        intent.setAction(ACTION_REFRESH);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, settings.getWidgetId());
        PendingIntent pendingIntent = PendingIntent.getBroadcast(settings.getContext(),
                EventRemoteViewsFactory.REQUEST_CODE_MIDNIGHT_ALARM + settings.getWidgetId(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        AlarmManager am = settings.getContext().getSystemService(AlarmManager.class);
        am.set(AlarmManager.RTC, nextUpdate.getMillis(), pendingIntent);
    }

    @SuppressLint("UnsafeProtectedBroadcastReceiver")
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(this.getClass().getSimpleName(), "Received intent: " + intent);
        AllSettings.ensureLoadedFromFiles(context, false);

        int widgetId = intent == null
                ? 0
                : intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, 0);
        if (widgetId == 0) {
            updateAllWidgets(context);
        } else {
            updateWidget(context, widgetId);
        }
    }

    public static void updateWidget(Context context, int widgetId) {
        Intent intent = new Intent(context, EventAppWidgetProvider.class);
        intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, new int[]{widgetId});
        Log.d(TAG, "updateWidget:" + widgetId + ", context:" + context);
        context.sendBroadcast(intent);
    }

    public static void updateAllWidgets(Context context) {
        Intent intent = new Intent(context, EventAppWidgetProvider.class);
        intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        int[] widgetIds = getWidgetIds(context);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, widgetIds);
        Log.d(TAG, "updateAllWidgets:" + widgetIds + ", context:" + context);
        context.sendBroadcast(intent);
    }
}
