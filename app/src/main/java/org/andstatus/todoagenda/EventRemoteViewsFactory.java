package org.andstatus.todoagenda;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService.RemoteViewsFactory;
import androidx.annotation.NonNull;

import org.andstatus.todoagenda.calendar.CalendarEventVisualizer;
import org.andstatus.todoagenda.prefs.AllSettings;
import org.andstatus.todoagenda.prefs.InstanceSettings;
import org.andstatus.todoagenda.task.TaskVisualizer;
import org.andstatus.todoagenda.util.DateUtil;
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

import static org.andstatus.todoagenda.Theme.themeNameToResId;

public class EventRemoteViewsFactory implements RemoteViewsFactory {
    private static final int MIN_MILLIS_BETWEEN_RELOADS = 500;

    private final Context context;
    private final int widgetId;
    private volatile List<WidgetEntry> widgetEntries = new ArrayList<>();
    private volatile List<WidgetEntryVisualizer<?>> eventProviders;
    private volatile long prevReloadFinishedAt = 0;

    public EventRemoteViewsFactory(Context context, int widgetId) {
        this.context = context;
        this.widgetId = widgetId;
        eventProviders = new ArrayList<>();
        eventProviders.add(new DayHeaderVisualizer(getSettings().getEntryThemeContext(), widgetId));
        eventProviders.add(new CalendarEventVisualizer(getSettings().getEntryThemeContext(), widgetId));
        eventProviders.add(new TaskVisualizer(getSettings().getEntryThemeContext(), widgetId));
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
                    return eventProvider.getRemoteViews(entry);
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
        reload();
    }

    private void reload() {
        long prevReloadMillis = Math.abs(System.currentTimeMillis() - prevReloadFinishedAt);
        if (prevReloadMillis < MIN_MILLIS_BETWEEN_RELOADS) {
            logEvent("reload, skip as done " + prevReloadMillis + " ms ago");
            return;
        }

        context.setTheme(themeNameToResId(getSettings().getEntryTheme()));

        InstanceSettings settings = getSettings();
        this.widgetEntries = getWidgetEntries(settings);
        logEvent("reload, visualizers:" + eventProviders.size() + ", entries:" + this.widgetEntries.size());

        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        if (appWidgetManager != null) {
            RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.widget_initial);

            EventAppWidgetProvider.configureWidgetHeader(settings, rv);
            EventAppWidgetProvider.configureWidgetEntriesList(settings, context, widgetId, rv);

            appWidgetManager.updateAppWidget(widgetId, rv);
        } else {
            Log.d(EventRemoteViewsFactory.class.getSimpleName(), widgetId + " reload, appWidgetManager is null" +
                    ", context:" + context);
        }

        prevReloadFinishedAt = System.currentTimeMillis();
    }

    private List<WidgetEntry> getWidgetEntries(InstanceSettings settings) {
        List<WidgetEntry> eventEntries = new ArrayList<>();
        for (WidgetEntryVisualizer<?> eventProvider : eventProviders) {
            eventEntries.addAll(eventProvider.getEventEntries());
        }
        Collections.sort(eventEntries);
        List<WidgetEntry> widgetEntries = settings.getShowDayHeaders() ? addDayHeaders(eventEntries) : eventEntries;
        widgetEntries.add(LastEntry.from(settings, widgetEntries));
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

    private void addEmptyDayHeadersBetweenTwoDays(List<WidgetEntry> entries, DateTime fromDayExclusive, DateTime toDayExclusive) {
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
        return result;
    }

    public long getItemId(int position) {
        return position;
    }

    public boolean hasStableIds() {
        return true;
    }
}
