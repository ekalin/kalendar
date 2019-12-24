package com.github.ekalin.kalendar;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService.RemoteViewsFactory;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import com.github.ekalin.kalendar.calendar.CalendarEventVisualizer;
import com.github.ekalin.kalendar.prefs.AllSettings;
import com.github.ekalin.kalendar.prefs.InstanceSettings;
import com.github.ekalin.kalendar.task.TaskVisualizer;
import com.github.ekalin.kalendar.util.CalendarIntentUtil;
import com.github.ekalin.kalendar.util.DateUtil;
import com.github.ekalin.kalendar.util.PermissionsUtil;
import com.github.ekalin.kalendar.widget.DayHeader;
import com.github.ekalin.kalendar.widget.DayHeaderVisualizer;
import com.github.ekalin.kalendar.widget.EmptyListMessageVisualizer;
import com.github.ekalin.kalendar.widget.WidgetEntry;
import com.github.ekalin.kalendar.widget.WidgetEntryVisualizer;

import static com.github.ekalin.kalendar.util.CalendarIntentUtil.createOpenCalendarEventPendingIntent;
import static com.github.ekalin.kalendar.util.CalendarIntentUtil.createOpenCalendarPendingIntent;
import static com.github.ekalin.kalendar.util.RemoteViewsUtil.setBackgroundColor;
import static com.github.ekalin.kalendar.util.RemoteViewsUtil.setDrawableColor;
import static com.github.ekalin.kalendar.util.RemoteViewsUtil.setTextSize;

public class EventRemoteViewsFactory implements RemoteViewsFactory {
    private static final String TAG = EventRemoteViewsFactory.class.getSimpleName();

    private static final int MIN_MILLIS_BETWEEN_RELOADS = 500;
    private static final String PACKAGE = "com.github.ekalin.kalendar";
    public static final String ACTION_REFRESH = PACKAGE + ".action.REFRESH";
    private static final int MAX_NUMBER_OF_WIDGETS = 100;
    private static final int REQUEST_CODE_EMPTY = 1;
    private static final int REQUEST_CODE_ADD_EVENT = 2;
    public static final int REQUEST_CODE_MIDNIGHT_ALARM = REQUEST_CODE_ADD_EVENT + MAX_NUMBER_OF_WIDGETS;

    private final Context context;
    private final int widgetId;
    private final InstanceSettings settings;
    private volatile List<WidgetEntry> widgetEntries = new ArrayList<>();
    private volatile List<WidgetEntryVisualizer<?>> eventProviders;
    private volatile long prevReloadFinishedAt = 0;

    public EventRemoteViewsFactory(Context context, int widgetId) {
        this.context = context;
        this.widgetId = widgetId;
        this.settings = AllSettings.instanceFromId(context, widgetId);

        eventProviders = new ArrayList<>();
        eventProviders.add(new DayHeaderVisualizer(context, widgetId));
        eventProviders.add(new CalendarEventVisualizer(context, widgetId));
        eventProviders.add(new TaskVisualizer(context, widgetId));
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
        DateTime nextUpdate = DateUtil.startOfNextDay(DateUtil.now(getSettings().getTimeZone()));
        for (WidgetEntry entry : widgetEntries) {
            DateTime eventUpdateTime = entry.getNextUpdateTime();
            if (eventUpdateTime != null && eventUpdateTime.isBefore(nextUpdate)) {
                nextUpdate = eventUpdateTime;
            }
        }

        EnvironmentChangedReceiver.scheduleNextUpdate(getSettings(), nextUpdate);
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
        logEvent("getViewTypeCount:" + result);
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
        rv.setOnClickPendingIntent(viewId, createOpenCalendarPendingIntent(settings));
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
        rv.setOnClickPendingIntent(R.id.add_event, getPermittedAddEventPendingIntent(settings));
    }

    private static void configureRefresh(InstanceSettings settings, RemoteViews rv) {
        Intent intent = new Intent(settings.getContext(), EnvironmentChangedReceiver.class);
        intent.setAction(ACTION_REFRESH);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, settings.getWidgetId());
        PendingIntent pendingIntent = PermissionsUtil.getPermittedPendingBroadcastIntent(settings, intent);
        rv.setOnClickPendingIntent(R.id.refresh, pendingIntent);
    }

    private static void configureOverflowMenu(InstanceSettings settings, RemoteViews rv) {
        Intent intent = MainActivity.intentToConfigure(settings.getContext(), settings.getWidgetId());
        PendingIntent pendingIntent = PermissionsUtil.getPermittedPendingActivityIntent(settings, intent);
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
        Intent intent = new Intent(context, EventWidgetService.class);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
        intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));
        setBackgroundColor(rv, R.id.event_list, settings.getBackgroundColor());
        rv.setRemoteAdapter(R.id.event_list, intent);
        boolean permissionsGranted = PermissionsUtil.arePermissionsGranted(context);
        if (permissionsGranted) {
            rv.setPendingIntentTemplate(R.id.event_list, createOpenCalendarEventPendingIntent(settings));
        }
    }

    public static PendingIntent getPermittedAddEventPendingIntent(InstanceSettings settings) {
        Context context = settings.getContext();
        Intent intent = PermissionsUtil.getPermittedActivityIntent(context,
                CalendarIntentUtil.createNewEventIntent(settings.getTimeZone()));
        return isIntentAvailable(context, intent) ?
                PendingIntent.getActivity(context, REQUEST_CODE_ADD_EVENT, intent, PendingIntent.FLAG_UPDATE_CURRENT) :
                getEmptyPendingIntent(context);
    }

    private static boolean isIntentAvailable(Context context, Intent intent) {
        PackageManager packageManager = context.getPackageManager();
        List<ResolveInfo> list = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        return list.size() > 0;
    }

    private static PendingIntent getEmptyPendingIntent(Context context) {
        return PendingIntent.getActivity(
                context.getApplicationContext(),
                REQUEST_CODE_EMPTY,
                new Intent(),
                PendingIntent.FLAG_UPDATE_CURRENT);
    }
}
