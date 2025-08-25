package com.github.ekalin.kalendar;

import android.content.Context;
import android.util.Log;
import android.widget.RemoteViews;
import androidx.core.widget.RemoteViewsCompat;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import com.github.ekalin.kalendar.birthday.BirthdayVisualizer;
import com.github.ekalin.kalendar.calendar.CalendarEventVisualizer;
import com.github.ekalin.kalendar.prefs.InstanceSettings;
import com.github.ekalin.kalendar.task.TaskVisualizer;
import com.github.ekalin.kalendar.util.DateUtil;
import com.github.ekalin.kalendar.widget.DayHeader;
import com.github.ekalin.kalendar.widget.DayHeaderVisualizer;
import com.github.ekalin.kalendar.widget.WidgetEntry;
import com.github.ekalin.kalendar.widget.WidgetEntryVisualizer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class WidgetEntryFactory {
    private static final String TAG = WidgetEntryFactory.class.getSimpleName();

    private final InstanceSettings settings;

    private volatile List<WidgetEntryVisualizer<? extends WidgetEntry>> eventVisualizers;

    public WidgetEntryFactory(Context context, int widgetId, InstanceSettings settings) {
        this.settings = settings;
        eventVisualizers = new ArrayList<>();
        eventVisualizers.add(new DayHeaderVisualizer(context, widgetId));
        eventVisualizers.add(new CalendarEventVisualizer(context, widgetId));
        eventVisualizers.add(new TaskVisualizer(context, widgetId));
        eventVisualizers.add(new BirthdayVisualizer(context, widgetId));
    }

    public List<WidgetEntry> getWidgetEntries() {
        List<WidgetEntry> eventEntries = new ArrayList<>();
        for (WidgetEntryVisualizer<?> eventProvider : eventVisualizers) {
            eventEntries.addAll(eventProvider.getEventEntries());
        }
        Collections.sort(eventEntries);

        return settings.getShowDayHeaders() ? addDayHeaders(eventEntries) : eventEntries;
    }

    private List<WidgetEntry> addDayHeaders(List<WidgetEntry> listIn) {
        List<WidgetEntry> listOut = new ArrayList<>();
        if (!listIn.isEmpty()) {
            DateTimeZone zone = settings.getTimeZone();
            boolean showDaysWithoutEvents = settings.getShowDaysWithoutEvents();
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
        DateTime today = DateUtil.now(settings.getTimeZone()).withTimeAtStartOfDay();
        if (emptyDay.isBefore(today)) {
            emptyDay = today;
        }
        while (emptyDay.isBefore(toDayExclusive)) {
            entries.add(new DayHeader(emptyDay, zone));
            emptyDay = emptyDay.plusDays(1);
        }
    }

    public RemoteViewsCompat.RemoteCollectionItems getEntryViews(List<WidgetEntry> entries) {
        Log.d(TAG, "Creating entries list");
        RemoteViewsCompat.RemoteCollectionItems.Builder builder = new RemoteViewsCompat.RemoteCollectionItems.Builder()
                .setHasStableIds(false)
                .setViewTypeCount(getViewTypeCount());

        for (int i = 0; i < entries.size(); i++) {
            WidgetEntry entry = entries.get(i);
            RemoteViews view = getEntryView(entry, i);
            if (view != null) {
                builder.addItem(i, view);
            }
        }

        Log.d(TAG, "Finished creating entries list with " + entries.size() + " items");
        return builder.build();
    }

    private RemoteViews getEntryView(WidgetEntry entry, int position) {
        for (WidgetEntryVisualizer<?> eventProvider : eventVisualizers) {
            if (entry.getClass().isAssignableFrom(eventProvider.getSupportedEventEntryType())) {
                return eventProvider.getRemoteViews(entry, position);
            }
        }

        return null;
    }

    private int getViewTypeCount() {
        int result = 0;
        for (WidgetEntryVisualizer<?> eventProvider : eventVisualizers) {
            result += eventProvider.getViewTypeCount();
        }
        return result;
    }
}
