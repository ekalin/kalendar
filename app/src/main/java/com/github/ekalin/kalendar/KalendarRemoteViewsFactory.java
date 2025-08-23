package com.github.ekalin.kalendar;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;
import androidx.annotation.NonNull;
import androidx.core.widget.RemoteViewsCompat;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import com.github.ekalin.kalendar.birthday.BirthdayVisualizer;
import com.github.ekalin.kalendar.calendar.CalendarEventVisualizer;
import com.github.ekalin.kalendar.prefs.AllSettings;
import com.github.ekalin.kalendar.prefs.InstanceSettings;
import com.github.ekalin.kalendar.task.TaskVisualizer;
import com.github.ekalin.kalendar.util.DateUtil;
import com.github.ekalin.kalendar.util.PermissionsUtil;
import com.github.ekalin.kalendar.widget.DayHeader;
import com.github.ekalin.kalendar.widget.DayHeaderVisualizer;
import com.github.ekalin.kalendar.widget.EmptyListMessageVisualizer;
import com.github.ekalin.kalendar.widget.WidgetEntry;
import com.github.ekalin.kalendar.widget.WidgetEntryVisualizer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import static com.github.ekalin.kalendar.util.RemoteViewsUtil.setBackgroundColor;
import static com.github.ekalin.kalendar.util.RemoteViewsUtil.setDrawableColor;
import static com.github.ekalin.kalendar.util.RemoteViewsUtil.setTextSize;

public class KalendarRemoteViewsFactory {
    private static final String TAG = KalendarRemoteViewsFactory.class.getSimpleName();

    private static final int MINIMUM_UPDATE_MINUTES = 60;

    private final InstanceSettings settings;
    private volatile List<WidgetEntryVisualizer<?>> eventProviders;

    public KalendarRemoteViewsFactory(Context context, int widgetId) {
        this.settings = AllSettings.instanceFromId(context, widgetId);

        eventProviders = new ArrayList<>();
        eventProviders.add(new DayHeaderVisualizer(context, widgetId));
        eventProviders.add(new CalendarEventVisualizer(context, widgetId));
        eventProviders.add(new TaskVisualizer(context, widgetId));
        eventProviders.add(new BirthdayVisualizer(context, widgetId));
    }

    void updateWidget(Context context, int widgetId) {
        Log.d(TAG, "Starting update for " + widgetId);
        try {
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            if (appWidgetManager == null) {
                Log.d(TAG, widgetId + " updateWidget, appWidgetManager is null, context:" + context);
                return;
            }

            InstanceSettings settings = AllSettings.instanceFromId(context, widgetId);
            RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.widget_main);
            configureWidgetHeader(settings, rv);
            configureWidgetEntriesList(context, settings, rv);

            Log.d(TAG, "Calling appWidgetManager.updateAppWidget");
            appWidgetManager.updateAppWidget(widgetId, rv);
            Log.d(TAG, "Finished update for " + widgetId);
        } catch (Exception e) {
            Log.w(TAG, widgetId + " Exception in updateWidget, context:" + context, e);
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

    private void configureWidgetEntriesList(Context context, InstanceSettings settings, RemoteViews rv) {
        setBackgroundColor(rv, R.id.event_list, settings.getBackgroundColor());

        List<WidgetEntry> entries = getWidgetEntries(settings);
        if (entries.isEmpty()) {
            configureEmptyWidgetMessage(settings, rv);
        }

        RemoteViewsCompat.setRemoteAdapter(context, rv, settings.getWidgetId(), R.id.event_list, getEntryViews(entries));
        rv.setPendingIntentTemplate(R.id.event_list,
                KalendarClickReceiver.createMutablePendingIntentForAction(KalendarClickReceiver.KalendarAction.VIEW_ENTRY, settings));

        scheduleNextUpdate(entries);
    }

    private RemoteViewsCompat.RemoteCollectionItems getEntryViews(List<WidgetEntry> entries) {
        Log.d(TAG, "Creating entries list");
        RemoteViewsCompat.RemoteCollectionItems.Builder builder = new RemoteViewsCompat.RemoteCollectionItems.Builder()
                .setHasStableIds(false)
                .setViewTypeCount(getViewTypeCount());

        for (int i = 0; i < entries.size(); i++) {
            WidgetEntry entry = entries.get(i);
            RemoteViews view = getEntryView(entry, i);
            builder.addItem(i, view);
        }

        Log.d(TAG, "Finished creating entries list with " + entries.size() + " items");
        return builder.build();
    }

    private List<WidgetEntry> getWidgetEntries(InstanceSettings settings) {
        List<WidgetEntry> eventEntries = new ArrayList<>();
        for (WidgetEntryVisualizer<?> eventProvider : eventProviders) {
            eventEntries.addAll(eventProvider.getEventEntries());
        }
        Collections.sort(eventEntries);

        return settings.getShowDayHeaders() ? addDayHeaders(eventEntries) : eventEntries;
    }

    private List<WidgetEntry> addDayHeaders(List<WidgetEntry> listIn) {
        List<WidgetEntry> listOut = new ArrayList<>();
        if (!listIn.isEmpty()) {
            DateTimeZone zone = getSettings().getTimeZone();
            boolean showDaysWithoutEvents = getSettings().getShowDaysWithoutEvents();
            DayHeader curDayBucket = new DayHeader(new DateTime(0, zone), zone);
            for (WidgetEntry entry : listIn) {
                DateTime nextStartOfDay = entry.getStartDay();
                if (!nextStartOfDay.isEqual(curDayBucket.getStartDay())) {
                    if (showDaysWithoutEvents) {
                        addEmptyDayHeadersBetweenTwoDays(listOut, curDayBucket.getStartDay(), nextStartOfDay, zone);
                    }
                    curDayBucket = new DayHeader(nextStartOfDay, zone);
                    listOut.add(curDayBucket);
                }
                listOut.add(entry);
            }
        }
        return listOut;
    }

    private void addEmptyDayHeadersBetweenTwoDays(List<WidgetEntry> entries, DateTime fromDayExclusive,
                                                  DateTime toDayExclusive, DateTimeZone zone) {
        DateTime emptyDay = fromDayExclusive.plusDays(1);
        DateTime today = DateUtil.now(getSettings().getTimeZone()).withTimeAtStartOfDay();
        if (emptyDay.isBefore(today)) {
            emptyDay = today;
        }
        while (emptyDay.isBefore(toDayExclusive)) {
            entries.add(new DayHeader(emptyDay, zone));
            emptyDay = emptyDay.plusDays(1);
        }
    }

    public RemoteViews getEntryView(WidgetEntry entry, int position) {
        for (WidgetEntryVisualizer<?> eventProvider : eventProviders) {
            if (entry.getClass().isAssignableFrom(eventProvider.getSupportedEventEntryType())) {
                return eventProvider.getRemoteViews(entry, position);
            }
        }

        return null;
    }

    public int getViewTypeCount() {
        int result = 0;
        for (WidgetEntryVisualizer<?> eventProvider : eventProviders) {
            result += eventProvider.getViewTypeCount();
        }
        return result;
    }

    // TODO: This logic would fit better in KalendarUpdater
    // Maybe here only the next event date is returned, and the rest of the logic is in KalendarUpdater
    private void scheduleNextUpdate(List<WidgetEntry> entries) {
        DateTime now = DateUtil.now(getSettings().getTimeZone());

        DateTime nextUpdate = DateUtil.startOfNextDay(now);

        for (WidgetEntry entry : entries) {
            DateTime eventUpdateTime = entry.getNextUpdateTime();
            if (eventUpdateTime != null && eventUpdateTime.isBefore(nextUpdate)) {
                nextUpdate = eventUpdateTime;
            }
        }

        DateTime minimumUpdateInterval = now.plusMinutes(MINIMUM_UPDATE_MINUTES);
        if (minimumUpdateInterval.isBefore(nextUpdate)) {
            nextUpdate = minimumUpdateInterval;
        }

        KalendarUpdater.scheduleNextUpdate(getSettings(), nextUpdate);
    }

    @NonNull
    private InstanceSettings getSettings() {
        return settings;
    }
}
