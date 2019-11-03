package org.andstatus.todoagenda;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import org.andstatus.todoagenda.prefs.InstanceSettings;
import org.andstatus.todoagenda.task.dmfs.DmfsOpenTasksContract;
import org.joda.time.DateTime;

import java.util.concurrent.atomic.AtomicReference;

import static org.andstatus.todoagenda.EventAppWidgetProvider.getWidgetIds;
import static org.andstatus.todoagenda.EventRemoteViewsFactory.ACTION_REFRESH;

public class EnvironmentChangedReceiver extends BroadcastReceiver {
    private static final String TAG = EnvironmentChangedReceiver.class.getSimpleName();
    private static boolean receiverRegistered = false;
    private static final AtomicReference<EnvironmentChangedReceiver> registeredReceiver = new AtomicReference<>();

    public static void registerReceivers(Context context) {
        context = context.getApplicationContext();
        synchronized (registeredReceiver) {
            if (receiverRegistered) {
                return;
            }

            EnvironmentChangedReceiver receiver = new EnvironmentChangedReceiver();

            IntentFilter providerChanged = new IntentFilter();
            providerChanged.addAction(Intent.ACTION_PROVIDER_CHANGED);
            providerChanged.addDataScheme("content");
            providerChanged.addDataAuthority("com.android.calendar", null);
            providerChanged.addDataAuthority(DmfsOpenTasksContract.AUTHORITY, null);
            context.registerReceiver(receiver, providerChanged);

            IntentFilter filter = new IntentFilter();
            filter.addAction(Intent.ACTION_CONFIGURATION_CHANGED);
            filter.addAction(Intent.ACTION_DREAMING_STOPPED);
            context.registerReceiver(receiver, filter);

            EnvironmentChangedReceiver oldReceiver = registeredReceiver.getAndSet(receiver);
            if (oldReceiver != null) {
                oldReceiver.unRegister(context);
            }

            receiverRegistered = true;
            Log.i(TAG, "Registered receivers from " + context.getClass().getName());
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

        AlarmManager am =
                (AlarmManager) settings.getContext().getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        am.set(AlarmManager.RTC, nextUpdate.getMillis(), pendingIntent);
    }

    @SuppressLint("UnsafeProtectedBroadcastReceiver")
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(this.getClass().getSimpleName(), "Received intent: " + intent);

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
