package com.github.ekalin.kalendar;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.net.Uri;
import android.provider.CalendarContract;
import android.util.Log;

import org.joda.time.DateTime;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import com.github.ekalin.kalendar.prefs.InstanceSettings;
import com.github.ekalin.kalendar.task.TaskProvider;

import static com.github.ekalin.kalendar.KalendarAppWidgetProvider.getWidgetIds;
import static com.github.ekalin.kalendar.KalendarRemoteViewsFactory.ACTION_REFRESH;

public class EnvironmentChangedReceiver extends BroadcastReceiver {
    private static final String TAG = EnvironmentChangedReceiver.class.getSimpleName();
    private static boolean receiverRegistered = false;
    private static final AtomicReference<EnvironmentChangedReceiver> registeredReceiver = new AtomicReference<>();
    private static final AtomicReference<ContentObserver> registeredCalendarObserver = new AtomicReference<>();
    private static final AtomicReference<List<ContentObserver>> registeredTaskObservers = new AtomicReference<>();

    public static void registerReceivers(Context context, boolean reregister) {
        Context applContext = context.getApplicationContext();
        synchronized (registeredReceiver) {
            if (!reregister && receiverRegistered) {
                return;
            }

            EnvironmentChangedReceiver receiver = new EnvironmentChangedReceiver();
            IntentFilter filter = new IntentFilter();
            filter.addAction(Intent.ACTION_CONFIGURATION_CHANGED);
            filter.addAction(Intent.ACTION_DREAMING_STOPPED);
            applContext.registerReceiver(receiver, filter);

            EnvironmentChangedReceiver oldReceiver = registeredReceiver.getAndSet(receiver);
            if (oldReceiver != null) {
                oldReceiver.unRegister(applContext);
            }

            EventsContentObserver calendarObserver = new EventsContentObserver(applContext);
            applContext.getContentResolver().registerContentObserver(CalendarContract.CONTENT_URI, false,
                    calendarObserver);
            ContentObserver oldCalendarObserver = registeredCalendarObserver.getAndSet(calendarObserver);
            if (oldCalendarObserver != null) {
                applContext.getContentResolver().unregisterContentObserver(oldCalendarObserver);
            }

            List<ContentObserver> taskObservers = TaskProvider.registerObservers(applContext,
                    () -> new EventsContentObserver(applContext));
            List<ContentObserver> oldTaskObservers = registeredTaskObservers.getAndSet(taskObservers);
            if (oldTaskObservers != null) {
                for (ContentObserver oldTaskObserver : oldTaskObservers) {
                    applContext.getContentResolver().unregisterContentObserver(oldTaskObserver);
                }
            }

            receiverRegistered = true;
            Log.i(TAG, "Registered receivers from " + applContext.getClass().getName());
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
                KalendarRemoteViewsFactory.REQUEST_CODE_MIDNIGHT_ALARM + settings.getWidgetId(),
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
        Intent intent = new Intent(context, KalendarAppWidgetProvider.class);
        intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, new int[]{widgetId});
        Log.d(TAG, "updateWidget:" + widgetId + ", context:" + context);
        context.sendBroadcast(intent);
    }

    public static void updateAllWidgets(Context context) {
        Intent intent = new Intent(context, KalendarAppWidgetProvider.class);
        intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        int[] widgetIds = getWidgetIds(context);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, widgetIds);
        Log.d(TAG, "updateAllWidgets:" + widgetIds + ", context:" + context);
        context.sendBroadcast(intent);
    }

    private static final class EventsContentObserver extends ContentObserver {
        private Context context;

        public EventsContentObserver(Context context) {
            super(null);
            this.context = context;
        }

        @Override
        public void onChange(boolean selfChange) {
            this.onChange(selfChange, null);
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            Log.i(TAG, "Content changed notification for " + uri);
            updateAllWidgets(context);
        }
    }
}
