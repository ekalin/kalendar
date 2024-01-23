package com.github.ekalin.kalendar;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import org.joda.time.DateTime;

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
            case VIEW_CALENDAR_TODAY:
                viewCalendarToday(context);
                break;
            case ADD_CALENDAR_EVENT:
                addCalendarEvent(context);
                break;
            case REFRESH:
                refresh(context, widgetId);
                break;
            case CONFIGURE:
                configure(context, widgetId);
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

    private void viewCalendarToday(Context context) {
        viewEntry(context, CalendarIntentUtil.createOpenCalendarAtDayFillInIntent(new DateTime()));
    }

    private void addCalendarEvent(Context context) {
        Intent intent = CalendarIntentUtil.createNewEventIntent();
        context.startActivity(intent);
    }

    private void refresh(Context context, int widgetId) {
        EnvironmentChangedReceiver.updateWidget(context, widgetId);
    }

    private void configure(Context context, int widgetId) {
        Intent intent = MainActivity.intentToConfigure(context, widgetId);
        context.startActivity(intent);
    }

    public static PendingIntent createImmutablePendingIntentForAction(KalendarAction action, InstanceSettings settings) {
        return createPendingIntentForAction(action, settings, false);
    }

    public static PendingIntent createMutablePendingIntentForAction(KalendarAction action, InstanceSettings settings) {
        return createPendingIntentForAction(action, settings, true);
    }

    private static PendingIntent createPendingIntentForAction(KalendarAction action, InstanceSettings settings,
                                                              boolean mutable) {
        int requestCode = settings.getWidgetId();
        Intent intent = new Intent(settings.getContext().getApplicationContext(), KalendarClickReceiver.class)
                .setAction(action.getName())
                .putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, settings.getWidgetId());
        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (mutable) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                flags |= PendingIntent.FLAG_MUTABLE;
            }
        } else {
            flags |= PendingIntent.FLAG_IMMUTABLE;
        }
        return PendingIntent.getBroadcast(settings.getContext(), requestCode, intent, flags);
    }

    public enum KalendarAction {
        VIEW_ENTRY,
        VIEW_CALENDAR_TODAY,
        ADD_CALENDAR_EVENT,
        REFRESH,
        CONFIGURE;

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
