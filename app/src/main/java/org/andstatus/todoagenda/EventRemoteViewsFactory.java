package org.andstatus.todoagenda;

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

import org.andstatus.todoagenda.calendar.CalendarEventVisualizer;
import org.andstatus.todoagenda.prefs.AllSettings;
import org.andstatus.todoagenda.prefs.InstanceSettings;
import org.andstatus.todoagenda.task.TaskVisualizer;
import org.andstatus.todoagenda.util.CalendarIntentUtil;
import org.andstatus.todoagenda.util.DateUtil;
import org.andstatus.todoagenda.util.PermissionsUtil;
import org.andstatus.todoagenda.widget.DayHeader;
import org.andstatus.todoagenda.widget.DayHeaderVisualizer;
import org.andstatus.todoagenda.widget.LastEntry;
import org.andstatus.todoagenda.widget.LastEntryVisualizer;
import org.andstatus.todoagenda.widget.WidgetEntry;
import org.andstatus.todoagenda.widget.WidgetEntryVisualizer;
import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import static org.andstatus.todoagenda.Theme.themeNameToResId;
import static org.andstatus.todoagenda.util.CalendarIntentUtil.createOpenCalendarEventPendingIntent;
import static org.andstatus.todoagenda.util.CalendarIntentUtil.createOpenCalendarPendingIntent;
import static org.andstatus.todoagenda.util.RemoteViewsUtil.setAlpha;
import static org.andstatus.todoagenda.util.RemoteViewsUtil.setBackgroundColor;
import static org.andstatus.todoagenda.util.RemoteViewsUtil.setImageFromAttr;
import static org.andstatus.todoagenda.util.RemoteViewsUtil.setTextColorFromAttr;
import static org.andstatus.todoagenda.util.RemoteViewsUtil.setTextSize;

public class EventRemoteViewsFactory implements RemoteViewsFactory {
    private static final String TAG = EventRemoteViewsFactory.class.getSimpleName();

    private static final int MIN_MILLIS_BETWEEN_RELOADS = 500;
    private static final String PACKAGE = "org.andstatus.todoagenda";
    public static final String ACTION_REFRESH = PACKAGE + ".action.REFRESH";
    private static final int MAX_NUMBER_OF_WIDGETS = 100;
    private static final int REQUEST_CODE_EMPTY = 1;
    private static final int REQUEST_CODE_ADD_EVENT = 2;
    public static final int REQUEST_CODE_MIDNIGHT_ALARM = REQUEST_CODE_ADD_EVENT + MAX_NUMBER_OF_WIDGETS;

    private final Context context;
    private final int widgetId;
    private volatile List<WidgetEntry> widgetEntries = new ArrayList<>();
    private volatile List<WidgetEntryVisualizer<?>> eventProviders;
    private volatile long prevReloadFinishedAt = 0;

    public EventRemoteViewsFactory(Context context, int widgetId) {
        this.context = context;
        this.widgetId = widgetId;
        eventProviders = new ArrayList<>();
        eventProviders.add(new DayHeaderVisualizer(context, widgetId));
        eventProviders.add(new CalendarEventVisualizer(context, widgetId));
        eventProviders.add(new TaskVisualizer(context, widgetId));
        eventProviders.add(new LastEntryVisualizer(context, widgetId));

        widgetEntries.add(new LastEntry(LastEntry.LastEntryType.NOT_LOADED, DateUtil.now(getSettings().getTimeZone())));
    }

    private void logEvent(String message) {
        Log.d(this.getClass().getSimpleName(), widgetId + " " + message);
    }

    public void onCreate() {
        reload();
    }

    public void onDestroy() {
        // Empty
    }

    public int getCount() {
        return widgetEntries.size();
    }

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
        return AllSettings.instanceFromId(context, widgetId);
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
        }
        updateWidget(context, widgetId, this);
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

        List<WidgetEntry> widgetEntries = settings.getShowDayHeaders() ? addDayHeaders(eventEntries) : eventEntries;
        LastEntry lastEntry = LastEntry.from(settings, widgetEntries);
        if (lastEntry != null) {
            widgetEntries.add(lastEntry);
        }

        return widgetEntries;
    }

    private List<WidgetEntry> addDayHeaders(List<WidgetEntry> listIn) {
        List<WidgetEntry> listOut = new ArrayList<>();
        if (!listIn.isEmpty()) {
            boolean showDaysWithoutEvents = getSettings().getShowDaysWithoutEvents();
            DayHeader curDayBucket = new DayHeader(new DateTime(0, getSettings().getTimeZone()));
            for (WidgetEntry entry : listIn) {
                DateTime nextStartOfDay = entry.getStartDay();
                if (!nextStartOfDay.isEqual(curDayBucket.getStartDay())) {
                    if (showDaysWithoutEvents) {
                        addEmptyDayHeadersBetweenTwoDays(listOut, curDayBucket.getStartDay(), nextStartOfDay);
                    }
                    curDayBucket = new DayHeader(nextStartOfDay);
                    listOut.add(curDayBucket);
                }
                listOut.add(entry);
            }
        }
        return listOut;
    }

    public void logWidgetEntries(String tag) {
        for (int ind = 0; ind < getWidgetEntries().size(); ind++) {
            WidgetEntry widgetEntry = getWidgetEntries().get(ind);
            Log.v(tag, String.format("%02d ", ind) + widgetEntry.toString());
        }
    }

    List<WidgetEntry> getWidgetEntries() {
        return widgetEntries;
    }

    private void addEmptyDayHeadersBetweenTwoDays(List<WidgetEntry> entries, DateTime fromDayExclusive,
                                                  DateTime toDayExclusive) {
        DateTime emptyDay = fromDayExclusive.plusDays(1);
        DateTime today = DateUtil.now(getSettings().getTimeZone()).withTimeAtStartOfDay();
        if (emptyDay.isBefore(today)) {
            emptyDay = today;
        }
        while (emptyDay.isBefore(toDayExclusive)) {
            entries.add(new DayHeader(emptyDay));
            emptyDay = emptyDay.plusDays(1);
        }
    }

    public RemoteViews getLoadingView() {
        return null;
    }

    public int getViewTypeCount() {
        int result = 0;
        for (WidgetEntryVisualizer<?> eventProvider : eventProviders) {
            result += eventProvider.getViewTypeCount();
        }
        logEvent("getViewTypeCount:" + result);
        return result;
    }

    public long getItemId(int position) {
        return position;
    }

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
        setTextColorFromAttr(settings.getHeaderThemeContext(), rv, viewId, R.attr.header);
    }

    private static void setActionIcons(InstanceSettings settings, RemoteViews rv) {
        setImageFromAttr(settings.getHeaderThemeContext(), rv, R.id.add_event, R.attr.header_action_add_event);
        setImageFromAttr(settings.getHeaderThemeContext(), rv, R.id.refresh, R.attr.header_action_refresh);
        setImageFromAttr(settings.getHeaderThemeContext(), rv, R.id.overflow_menu, R.attr.header_action_overflow);
        int themeId = themeNameToResId(settings.getHeaderTheme());
        int alpha = 255;
        if (themeId == R.style.Theme_Calendar_Dark || themeId == R.style.Theme_Calendar_Light) {
            alpha = 154;
        }
        setAlpha(rv, R.id.add_event, alpha);
        setAlpha(rv, R.id.refresh, alpha);
        setAlpha(rv, R.id.overflow_menu, alpha);
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

    static void configureWidgetEntriesList(InstanceSettings settings, Context context, int widgetId,
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
