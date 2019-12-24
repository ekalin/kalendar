package com.github.ekalin.kalendar.calendar;

import android.content.Context;
import android.view.View;
import android.widget.RemoteViews;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;

import com.github.ekalin.kalendar.AlarmIndicatorScaled;
import com.github.ekalin.kalendar.R;
import com.github.ekalin.kalendar.RecurringIndicatorScaled;
import com.github.ekalin.kalendar.util.DateUtil;
import com.github.ekalin.kalendar.widget.CalendarEntry;
import com.github.ekalin.kalendar.widget.EventEntryLayout;
import com.github.ekalin.kalendar.widget.WidgetEntry;
import com.github.ekalin.kalendar.widget.WidgetEntryVisualizer;

import static com.github.ekalin.kalendar.util.RemoteViewsUtil.setBackgroundColor;
import static com.github.ekalin.kalendar.util.RemoteViewsUtil.setDrawableColor;

public class CalendarEventVisualizer extends WidgetEntryVisualizer<CalendarEntry> {
    private final CalendarEventProvider calendarContentProvider;

    public CalendarEventVisualizer(Context context, int widgetId) {
        super(context, widgetId);
        calendarContentProvider = new CalendarEventProvider(context, widgetId, getSettings());
    }

    @Override
    public RemoteViews getRemoteViews(WidgetEntry eventEntry, int position) {
        CalendarEntry entry = (CalendarEntry) eventEntry;
        EventEntryLayout eventEntryLayout = getSettings().getEventEntryLayout();
        RemoteViews rv = new RemoteViews(getContext().getPackageName(), eventEntryLayout.layoutId);
        rv.setOnClickFillInIntent(R.id.event_entry,
                calendarContentProvider.createOpenCalendarEventIntent(entry.getEvent()));
        eventEntryLayout.visualizeEvent(this, entry, rv);
        setAlarmActive(entry, rv);
        setRecurring(entry, rv);
        setColor(entry, rv);
        return rv;
    }

    private void setAlarmActive(CalendarEntry entry, RemoteViews rv) {
        boolean showIndicator = entry.isAlarmActive() && getSettings().getIndicateAlerts();
        for (AlarmIndicatorScaled indicator : AlarmIndicatorScaled.values()) {
            setIndicator(rv,
                    showIndicator && indicator == getSettings().getTextSizeScale().alarmIndicator,
                    indicator.indicatorResId, entry);
        }
    }

    private void setRecurring(CalendarEntry entry, RemoteViews rv) {
        boolean showIndicator = entry.isRecurring() && getSettings().getIndicateRecurring();
        for (RecurringIndicatorScaled indicator : RecurringIndicatorScaled.values()) {
            setIndicator(rv,
                    showIndicator && indicator == getSettings().getTextSizeScale().recurringIndicator,
                    indicator.indicatorResId, entry);
        }
    }

    private void setIndicator(RemoteViews rv, boolean showIndication, int viewId, CalendarEntry entry) {
        if (showIndication) {
            rv.setViewVisibility(viewId, View.VISIBLE);
            setDrawableColor(rv, viewId, getIndicatorColor(entry));
        } else {
            rv.setViewVisibility(viewId, View.GONE);
        }
    }

    private int getIndicatorColor(CalendarEntry entry) {
        if (entry.isCurrent()) {
            return getSettings().getCurrentEventColor();
        } else {
            return getSettings().getEventColor();
        }
    }

    private void setColor(CalendarEntry entry, RemoteViews rv) {
        setBackgroundColor(rv, R.id.event_entry_color, entry.getColor());
        if (entry.getEndDate().isBefore(DateUtil.now(entry.getEndDate().getZone()))) {
            setBackgroundColor(rv, R.id.event_entry, getSettings().getPastEventsBackgroundColor());
        } else {
            setBackgroundColor(rv, R.id.event_entry, 0);
        }
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public List<CalendarEntry> getEventEntries() {
        return createEntryList(calendarContentProvider.getEvents());
    }

    private List<CalendarEntry> createEntryList(List<CalendarEvent> eventList) {
        boolean fillAllDayEvents = getSettings().getFillAllDayEvents();
        List<CalendarEntry> entryList = new ArrayList<>();
        for (CalendarEvent event : eventList) {
            CalendarEntry dayOneEntry = getDayOneEntry(event);
            entryList.add(dayOneEntry);
            createFollowingEntries(entryList, event, dayOneEntry, fillAllDayEvents);
        }
        return entryList;
    }

    private CalendarEntry getDayOneEntry(CalendarEvent event) {
        DateTime firstDate = event.getStartDate();
        DateTime dayOfStartOfTimeRange = calendarContentProvider.getStartOfTimeRange()
                .withTimeAtStartOfDay();
        if (firstDate.isBefore(calendarContentProvider.getStartOfTimeRange())
                && event.getEndDate().isAfter(calendarContentProvider.getStartOfTimeRange())) {
            if (event.isAllDay() || firstDate.isBefore(dayOfStartOfTimeRange)) {
                firstDate = dayOfStartOfTimeRange;
            }
        }
        DateTime today = DateUtil.now(event.getStartDate().getZone()).withTimeAtStartOfDay();
        if (event.isActive() && firstDate.isBefore(today)) {
            firstDate = today;
        }
        return CalendarEntry.fromEvent(event, firstDate);
    }

    private void createFollowingEntries(List<CalendarEntry> entryList, CalendarEvent event, CalendarEntry dayOneEntry,
                                        boolean fillAllDayEvents) {
        if (!fillAllDayEvents && event.isAllDay()) {
            return;
        }

        DateTime endDate = event.getEndDate();
        if (endDate.isAfter(calendarContentProvider.getEndOfTimeRange())) {
            endDate = calendarContentProvider.getEndOfTimeRange();
        }
        DateTime thisDay = dayOneEntry.getStartDay().plusDays(1).withTimeAtStartOfDay();
        while (thisDay.isBefore(endDate)) {
            CalendarEntry nextEntry = CalendarEntry.fromEvent(dayOneEntry.getEvent(), thisDay);
            entryList.add(nextEntry);
            thisDay = thisDay.plusDays(1);
        }
    }

    @Override
    public Class<? extends CalendarEntry> getSupportedEventEntryType() {
        return CalendarEntry.class;
    }
}
