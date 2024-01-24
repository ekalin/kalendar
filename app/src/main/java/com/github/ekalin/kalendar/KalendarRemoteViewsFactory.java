package com.github.ekalin.kalendar;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService.RemoteViewsFactory;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

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

public class KalendarRemoteViewsFactory implements RemoteViewsFactory {
    private static final String TAG = KalendarRemoteViewsFactory.class.getSimpleName();

    private static final int MIN_MILLIS_BETWEEN_RELOADS = 500;
    private static final int MINIMUM_UPDATE_MINUTES = 60;

    private final Context context;
    private final int widgetId;
    private final InstanceSettings settings;
    private volatile List<WidgetEntry> widgetEntries = new ArrayList<>();
    private volatile List<WidgetEntryVisualizer<?>> eventProviders;
    private volatile long prevReloadFinishedAt = 0;

    public KalendarRemoteViewsFactory(Context context, int widgetId) {
        this.context = context;
        this.widgetId = widgetId;
        this.settings = AllSettings.instanceFromId(context, widgetId);

        eventProviders = new ArrayList<>();
        eventProviders.add(new DayHeaderVisualizer(context, widgetId));
        eventProviders.add(new CalendarEventVisualizer(context, widgetId));
        eventProviders.add(new TaskVisualizer(context, widgetId));
        eventProviders.add(new BirthdayVisualizer(context, widgetId));
    }

    private void logEvent(String message) {
        Log.d(this.getClass().getSimpleName(), widgetId + " " + message);
    }

    @Override
    public void onCreate() {
        reload();
    }

    @Override
    public void onDestroy() {
        // Empty
    }

    @Override
    public int getCount() {
        return widgetEntries.size();
    }

    @Override
    public RemoteViews getViewAt(int position) {
        List<WidgetEntry> widgetEntries = this.widgetEntries;
        if (position < widgetEntries.size()) {
            WidgetEntry entry = widgetEntries.get(position);
            for (WidgetEntryVisualizer<?> eventProvider : eventProviders) {
                if (entry.getClass().isAssignableFrom(eventProvider.getSupportedEventEntryType())) {
                    return eventProvider.getRemoteViews(entry, position);
                }
            }
        }
        return null;
    }

    @NonNull
    private InstanceSettings getSettings() {
        return settings;
    }

    @Override
    public void onDataSetChanged() {
        logEvent("onDataSetChanged");
        reload();
    }

    private void reload() {
        long prevReloadMillis = Math.abs(System.currentTimeMillis() - prevReloadFinishedAt);
        if (prevReloadMillis < MIN_MILLIS_BETWEEN_RELOADS) {
            logEvent("reload, skip as done " + prevReloadMillis + " ms ago");
        } else {
            this.widgetEntries = getWidgetEntries(getSettings());
            logEvent("reload, visualizers:" + eventProviders.size() + ", entries:" + this.widgetEntries.size());
            prevReloadFinishedAt = System.currentTimeMillis();
            scheduleNextUpdate();
        }
        updateWidget(context, widgetId, this);
    }

    private void scheduleNextUpdate() {
        DateTime now = DateUtil.now(getSettings().getTimeZone());

        DateTime nextUpdate = DateUtil.startOfNextDay(now);

        for (WidgetEntry entry : widgetEntries) {
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

    static void updateWidget(Context context, int widgetId, @Nullable RemoteViewsFactory factory) {
        try {
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            if (appWidgetManager == null) {
                Log.d(TAG, widgetId + " updateWidget, appWidgetManager is null, context:" + context);
                return;
            }

            InstanceSettings settings = AllSettings.instanceFromId(context, widgetId);
            RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.widget_initial);
            configureWidgetHeader(settings, rv);
            if (factory != null && factory.getCount() == 0) {
                configureEmptyWidgetMessage(settings, rv);
            }
            configureWidgetEntriesList(settings, context, widgetId, rv);

            appWidgetManager.updateAppWidget(widgetId, rv);
        } catch (Exception e) {
            Log.w(TAG, widgetId + " Exception in updateWidget, context:" + context, e);
        }
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

    List<WidgetEntry> getWidgetEntries() {
        return widgetEntries;
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

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public int getViewTypeCount() {
        int result = 0;
        for (WidgetEntryVisualizer<?> eventProvider : eventProviders) {
            result += eventProvider.getViewTypeCount();
        }
        return result;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    private static void configureWidgetHeader(InstanceSettings settings, RemoteViews rv) {
        rv.removeAllViews(R.id.header_parent);
        if (!settings.getShowWidgetHeader()) {
            return;
        }

        RemoteViews headerView = new RemoteViews(settings.getContext().getPackageName(),
                R.layout.widget_header_one_line);
        rv.addView(R.id.header_parent, headerView);

        configureCurrentDate(settings, rv);
        setActionIcons(settings, rv);
        configureAddEvent(settings, rv);
        configureRefresh(settings, rv);
        configureOverflowMenu(settings, rv);
    }

    private static void configureCurrentDate(InstanceSettings settings, RemoteViews rv) {
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

    private static void setActionIcons(InstanceSettings settings, RemoteViews rv) {
        setDrawableColor(rv, R.id.add_event, settings.getWidgetHeaderColor());
        setDrawableColor(rv, R.id.refresh, settings.getWidgetHeaderColor());
        setDrawableColor(rv, R.id.overflow_menu, settings.getWidgetHeaderColor());
    }

    private static void configureAddEvent(InstanceSettings settings, RemoteViews rv) {
        PendingIntent pendingIntent = KalendarClickReceiver.createImmutablePendingIntentForAction(
                KalendarClickReceiver.KalendarAction.ADD_CALENDAR_EVENT, settings);
        rv.setOnClickPendingIntent(R.id.add_event, pendingIntent);
    }

    private static void configureRefresh(InstanceSettings settings, RemoteViews rv) {
        PendingIntent pendingIntent = KalendarClickReceiver.createImmutablePendingIntentForAction(
                KalendarClickReceiver.KalendarAction.REFRESH, settings);
        rv.setOnClickPendingIntent(R.id.refresh, pendingIntent);
    }

    private static void configureOverflowMenu(InstanceSettings settings, RemoteViews rv) {
        PendingIntent pendingIntent = KalendarClickReceiver.createImmutablePendingIntentForAction(
                KalendarClickReceiver.KalendarAction.CONFIGURE, settings);
        rv.setOnClickPendingIntent(R.id.overflow_menu, pendingIntent);
    }

    private static void configureEmptyWidgetMessage(InstanceSettings settings, RemoteViews rv) {
        EmptyListMessageVisualizer visualizer = new EmptyListMessageVisualizer(settings);
        RemoteViews message;
        if (PermissionsUtil.arePermissionsGranted(settings.getContext())) {
            message = visualizer.getView(EmptyListMessageVisualizer.Type.EMPTY);
        } else {
            message = visualizer.getView(EmptyListMessageVisualizer.Type.NO_PERMISSIONS);
        }
        rv.addView(R.id.header_parent, message);
    }

    private static void configureWidgetEntriesList(InstanceSettings settings, Context context, int widgetId,
                                                   RemoteViews rv) {
        Intent intent = new Intent(context, KalendarRemoteViewsService.class);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
        intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));
        setBackgroundColor(rv, R.id.event_list, settings.getBackgroundColor());
        rv.setRemoteAdapter(R.id.event_list, intent);
        rv.setPendingIntentTemplate(R.id.event_list,
                KalendarClickReceiver.createMutablePendingIntentForAction(KalendarClickReceiver.KalendarAction.VIEW_ENTRY, settings));
    }
}
