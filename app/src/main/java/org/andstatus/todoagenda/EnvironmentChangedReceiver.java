package org.andstatus.todoagenda;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
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

public class EnvironmentChangedReceiver extends BroadcastReceiver {
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
            filter.addAction(Intent.ACTION_USER_PRESENT);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                filter.addAction(Intent.ACTION_DREAMING_STOPPED);
            }
            context.registerReceiver(receiver, filter);

            EnvironmentChangedReceiver oldReceiver = registeredReceiver.getAndSet(receiver);
            if (oldReceiver != null) {
                oldReceiver.unRegister(context);
            }
            scheduleNextAlarms(context, instances);

            Log.i(EventAppWidgetProvider.class.getName(),
                    "Registered receivers from " + instanceSettings.getContext().getClass().getName());
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
                    EventAppWidgetProvider.REQUEST_CODE_MIDNIGHT_ALARM + counter,
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

    @SuppressLint("UnsafeProtectedBroadcastReceiver")
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(this.getClass().getName(), "Received intent: " + intent);
        AllSettings.ensureLoadedFromFiles(context, false);
        EventAppWidgetProvider.updateAllWidgets(context);
    }
}
