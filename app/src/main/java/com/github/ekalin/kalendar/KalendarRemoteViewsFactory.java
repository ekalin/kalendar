package com.github.ekalin.kalendar;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;
import androidx.core.widget.RemoteViewsCompat;

import org.joda.time.DateTime;

import com.github.ekalin.kalendar.prefs.AllSettings;
import com.github.ekalin.kalendar.prefs.InstanceSettings;
import com.github.ekalin.kalendar.util.DateUtil;
import com.github.ekalin.kalendar.util.PermissionsUtil;
import com.github.ekalin.kalendar.widget.EmptyListMessageVisualizer;
import com.github.ekalin.kalendar.widget.WidgetEntry;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

import static com.github.ekalin.kalendar.util.RemoteViewsUtil.setBackgroundColor;
import static com.github.ekalin.kalendar.util.RemoteViewsUtil.setDrawableColor;
import static com.github.ekalin.kalendar.util.RemoteViewsUtil.setTextSize;

public class KalendarRemoteViewsFactory {
    private static final String TAG = KalendarRemoteViewsFactory.class.getSimpleName();

    private final Context context;
    private final int widgetId;
    private final InstanceSettings settings;
    private final WidgetEntryFactory widgetEntryFactory;

    public KalendarRemoteViewsFactory(Context context, int widgetId) {
        this.context = context;
        this.widgetId = widgetId;
        this.settings = AllSettings.instanceFromId(context, widgetId);
        this.widgetEntryFactory = new WidgetEntryFactory(context, widgetId, settings);
    }

    public DateTime updateWidget() {
        Log.d(TAG, "Starting update for " + widgetId);
        try {
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            if (appWidgetManager == null) {
                Log.d(TAG, widgetId + " updateWidget, appWidgetManager is null, context:" + context);
                return null;
            }

            InstanceSettings settings = AllSettings.instanceFromId(context, widgetId);
            RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.widget_main);
            configureWidgetHeader(settings, rv);
            List<WidgetEntry> entries = widgetEntryFactory.getWidgetEntries();
            configureWidgetEntriesList(context, settings, rv, entries);

            Log.d(TAG, "Calling appWidgetManager.updateAppWidget");
            appWidgetManager.updateAppWidget(widgetId, rv);
            Log.d(TAG, "Finished update for " + widgetId);

            return nextUpdateTime(entries);
        } catch (Exception e) {
            Log.w(TAG, widgetId + " Exception in updateWidget, context:" + context, e);
            return nextUpdateTime(Collections.emptyList());
        }
    }

    private void configureWidgetHeader(InstanceSettings settings, RemoteViews rv) {
        rv.removeAllViews(R.id.header_parent);
        if (!settings.getShowWidgetHeader()) {
            return;
        }

        RemoteViews headerView = new RemoteViews(settings.getContext().getPackageName(),
                R.layout.widget_header_one_line);
        rv.addView(R.id.header_parent, headerView);
        setBackgroundColor(rv, R.id.header_parent, settings.getWidgetHeaderBackgroundColor());
        setBackgroundColor(rv, R.id.widget_header_separator, settings.getWidgetHeaderColor());
        rv.setViewVisibility(R.id.widget_header_separator,
                settings.getShowWidgetHeaderSeparator() ? View.VISIBLE : View.INVISIBLE);

        configureCurrentDate(settings, rv);
        setActionIcons(settings, rv);
        configureAddEvent(settings, rv);
        configureRefresh(settings, rv);
        configureOverflowMenu(settings, rv);
    }

    private void configureCurrentDate(InstanceSettings settings, RemoteViews rv) {
        int viewId = R.id.calendar_current_date;
        PendingIntent pendingIntent = KalendarClickReceiver.createImmutablePendingIntentForAction(
                KalendarClickReceiver.KalendarAction.VIEW_CALENDAR_TODAY, settings);
        rv.setOnClickPendingIntent(viewId, pendingIntent);
        String formattedDate = DateUtil.createDateString(settings,
                DateUtil.now(settings.getTimeZone())).toUpperCase(Locale.getDefault());
        rv.setTextViewText(viewId, formattedDate);
        setTextSize(settings, rv, viewId, R.dimen.widget_header_title);
        rv.setTextColor(viewId, settings.getWidgetHeaderColor());
    }

    private void setActionIcons(InstanceSettings settings, RemoteViews rv) {
        setDrawableColor(rv, R.id.add_event, settings.getWidgetHeaderColor());
        setDrawableColor(rv, R.id.refresh, settings.getWidgetHeaderColor());
        setDrawableColor(rv, R.id.overflow_menu, settings.getWidgetHeaderColor());
    }

    private void configureAddEvent(InstanceSettings settings, RemoteViews rv) {
        PendingIntent pendingIntent = KalendarClickReceiver.createImmutablePendingIntentForAction(
                KalendarClickReceiver.KalendarAction.ADD_CALENDAR_EVENT, settings);
        rv.setOnClickPendingIntent(R.id.add_event, pendingIntent);
    }

    private void configureRefresh(InstanceSettings settings, RemoteViews rv) {
        PendingIntent pendingIntent = KalendarClickReceiver.createImmutablePendingIntentForAction(
                KalendarClickReceiver.KalendarAction.REFRESH, settings);
        rv.setOnClickPendingIntent(R.id.refresh, pendingIntent);
    }

    private void configureOverflowMenu(InstanceSettings settings, RemoteViews rv) {
        PendingIntent pendingIntent = KalendarClickReceiver.createImmutablePendingIntentForAction(
                KalendarClickReceiver.KalendarAction.CONFIGURE, settings);
        rv.setOnClickPendingIntent(R.id.overflow_menu, pendingIntent);
    }

    private void configureEmptyWidgetMessage(InstanceSettings settings, RemoteViews rv) {
        EmptyListMessageVisualizer visualizer = new EmptyListMessageVisualizer(settings);
        RemoteViews message;
        if (PermissionsUtil.arePermissionsGranted(settings.getContext())) {
            message = visualizer.getView(EmptyListMessageVisualizer.Type.EMPTY);
        } else {
            message = visualizer.getView(EmptyListMessageVisualizer.Type.NO_PERMISSIONS);
        }
        rv.addView(R.id.header_parent, message);
    }

    private void configureWidgetEntriesList(Context context, InstanceSettings settings, RemoteViews rv, List<WidgetEntry> entries) {
        setBackgroundColor(rv, R.id.event_list, settings.getBackgroundColor());

        if (entries.isEmpty()) {
            configureEmptyWidgetMessage(settings, rv);
        }

        RemoteViewsCompat.RemoteCollectionItems entryViews = widgetEntryFactory.getEntryViews(entries);
        RemoteViewsCompat.setRemoteAdapter(context, rv, settings.getWidgetId(), R.id.event_list, entryViews);
        rv.setPendingIntentTemplate(R.id.event_list,
                KalendarClickReceiver.createMutablePendingIntentForAction(KalendarClickReceiver.KalendarAction.VIEW_ENTRY, settings));
    }

    private DateTime nextUpdateTime(List<WidgetEntry> entries) {
        DateTime now = DateUtil.now(settings.getTimeZone());
        DateTime nextUpdate = DateUtil.startOfNextDay(now);

        for (WidgetEntry entry : entries) {
            DateTime eventUpdateTime = entry.getNextUpdateTime();
            if (eventUpdateTime != null && eventUpdateTime.isBefore(nextUpdate)) {
                nextUpdate = eventUpdateTime;
            }
        }

        return nextUpdate;
    }
}
