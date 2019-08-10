package org.andstatus.todoagenda;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService.RemoteViewsFactory;

import org.andstatus.todoagenda.calendar.CalendarEventVisualizer;
import org.andstatus.todoagenda.prefs.InstanceSettings;
import org.andstatus.todoagenda.task.TaskVisualizer;
import org.andstatus.todoagenda.widget.DayHeader;
import org.andstatus.todoagenda.widget.DayHeaderVisualizer;
import org.andstatus.todoagenda.widget.WidgetEntry;
import org.andstatus.todoagenda.widget.WidgetEntryVisualizer;
import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.andstatus.todoagenda.CalendarIntentUtil.createOpenCalendarEventPendingIntent;
import static org.andstatus.todoagenda.Theme.themeNameToResId;

public class EventRemoteViewsFactory implements RemoteViewsFactory {
    private final Context context;
    private final int widgetId;
    private volatile List<WidgetEntry> mWidgetEntries = new ArrayList<>();
    private final List<WidgetEntryVisualizer<?>> eventProviders;

    public EventRemoteViewsFactory(Context context, int widgetId) {
        this.context = context;
        this.widgetId = widgetId;
        eventProviders = new ArrayList<>();
        eventProviders.add(new DayHeaderVisualizer(context, widgetId));
        eventProviders.add(new CalendarEventVisualizer(context, widgetId));
        eventProviders.add(new TaskVisualizer(context, widgetId));
    }

    public void onCreate() {
        RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.widget);
        rv.setPendingIntentTemplate(R.id.event_list, createOpenCalendarEventPendingIntent(getSettings()));
    }

    public void onDestroy() {
        // Empty
    }

    public int getCount() {
        return mWidgetEntries.size();
    }

    public RemoteViews getViewAt(int position) {
        List<WidgetEntry> widgetEntries = mWidgetEntries;
        if (position < widgetEntries.size()) {
            WidgetEntry entry = widgetEntries.get(position);
            for (WidgetEntryVisualizer<?> eventProvider : eventProviders) {
                if (entry.getClass().isAssignableFrom(eventProvider.getSupportedEventEntryType())) {
                    return eventProvider.getRemoteView(entry);
                }
            }
        }
        return null;
    }

    @NonNull
    private InstanceSettings getSettings() {
        return InstanceSettings.fromId(context, widgetId);
    }

    public void onDataSetChanged() {
        context.setTheme(themeNameToResId(getSettings().getEntryTheme()));
        if (getSettings().getShowDayHeaders())
            mWidgetEntries = addDayHeaders(getEventEntries());
        else
            mWidgetEntries = getEventEntries();
    }

    private List<WidgetEntry> getEventEntries() {
        List<WidgetEntry> entries = new ArrayList<>();
        for (WidgetEntryVisualizer<?> eventProvider : eventProviders) {
            entries.addAll(eventProvider.getEventEntries());
        }
        Collections.sort(entries);
        return entries;
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
        return mWidgetEntries;
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
        int result = 3; // we have 3 because of the "left", "right" and "center" day headers
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
