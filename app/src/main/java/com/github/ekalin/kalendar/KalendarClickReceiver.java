package com.github.ekalin.kalendar;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import com.github.ekalin.kalendar.prefs.InstanceSettings;
import com.github.ekalin.kalendar.util.CalendarIntentUtil;

public class KalendarClickReceiver extends BroadcastReceiver {
    private static final String TAG = KalendarClickReceiver.class.getSimpleName();

    public static final String VIEW_ENTRY_DATA = "com.github.ekalin.kalendar.VIEW_ENTRY_DATA";
    public static final String VIEW_ENTRY_EXTRAS = "com.github.ekalin.kalendar.VIEW_ENTRY_EXTRAS";

    private static final String ACTION_PREFIX = "com.github.ekalin.action.";

    @Override
    public void onReceive(Context context, Intent intent) {
        String actionStr = intent.getAction();
        int widgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, 0);
        Log.i(TAG, "Received click in widget " + widgetId + " for action " + actionStr);

        if (widgetId == 0) {
            Log.w(TAG, "  No widgetId specified");
            return;
        }

        KalendarAction action = KalendarAction.getByName(actionStr);
        if (action == null) {
            Log.w(TAG, "  Unknown action " + actionStr);
            return;
        }

        switch (action) {
            case VIEW_ENTRY:
                viewEntry(context, intent);
                break;
        }
    }

    private void viewEntry(Context context, Intent intent) {
        Intent viewIntent = CalendarIntentUtil.createViewIntent();
        String viewData = intent.getStringExtra(VIEW_ENTRY_DATA);
        if (viewData == null) {
            Log.w(TAG, "  no data received");
            return;
        }

        Log.i(TAG, "  Original intent data: " + viewData);
        viewIntent.setData(Uri.parse(viewData));
        viewIntent.replaceExtras(intent.getBundleExtra(VIEW_ENTRY_EXTRAS));
        context.startActivity(viewIntent);
    }

    public static PendingIntent createPendingIntentForAction(KalendarAction action, InstanceSettings settings) {
        int requestCode = settings.getWidgetId();
        Intent intent = new Intent(settings.getContext().getApplicationContext(), KalendarClickReceiver.class)
                .setAction(action.getName())
                .putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, settings.getWidgetId());
        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            flags |= PendingIntent.FLAG_MUTABLE;
        }
        return PendingIntent.getBroadcast(settings.getContext(), requestCode, intent, flags);
    }

    public enum KalendarAction {
        VIEW_ENTRY;

        public String getName() {
            return ACTION_PREFIX + name();
        }

        public static KalendarAction getByName(String action) {
            KalendarAction[] values = values();
            for (KalendarAction value : values) {
                if (value.getName().equals(action)) {
                    return value;
                }
            }

            return null;
        }
    }
}
