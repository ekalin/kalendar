package com.github.ekalin.kalendar.calendar;

import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CalendarContract;
import android.provider.CalendarContract.Attendees;
import android.provider.CalendarContract.Calendars;
import android.provider.CalendarContract.Instances;
import android.util.SparseArray;
import androidx.annotation.NonNull;
import androidx.core.util.Supplier;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.github.ekalin.kalendar.prefs.EventSource;
import com.github.ekalin.kalendar.prefs.InstanceSettings;
import com.github.ekalin.kalendar.provider.EventProvider;
import com.github.ekalin.kalendar.provider.QueryResult;
import com.github.ekalin.kalendar.provider.QueryResultsStorage;
import com.github.ekalin.kalendar.util.CalendarIntentUtil;
import com.github.ekalin.kalendar.util.DateUtil;
import com.github.ekalin.kalendar.util.PermissionsUtil;

public class CalendarEventProvider extends EventProvider {
    private static final String EXCLUDE_DECLINED = Instances.SELF_ATTENDEE_STATUS + NOT_EQUALS
            + Attendees.ATTENDEE_STATUS_DECLINED;
    private static final String EXCLUDE_CANCELED = Instances.STATUS + NOT_EQUALS + Instances.STATUS_CANCELED;

    public CalendarEventProvider(Context context, int widgetId, InstanceSettings settings) {
        super(context, widgetId, settings);
    }

    public List<CalendarEvent> getEvents() {
        initialiseParameters();
        if (!PermissionsUtil.arePermissionsGranted(context)) {
            return new ArrayList<>();
        }
        List<CalendarEvent> eventList = getTimeFilteredEventList();
        if (getSettings().getShowOnlyClosestInstanceOfRecurringEvent()) {
            filterShowOnlyClosestInstanceOfRecurringEvent(eventList);
        }
        return eventList;
    }

    private void filterShowOnlyClosestInstanceOfRecurringEvent(@NonNull List<CalendarEvent> eventList) {
        SparseArray<CalendarEvent> eventIds = new SparseArray<>();
        List<CalendarEvent> toDelete = new ArrayList<>();
        for (CalendarEvent event : eventList) {
            CalendarEvent otherEvent = eventIds.get(event.getEventId());
            if (otherEvent == null) {
                eventIds.put(event.getEventId(), event);
            } else if (Math.abs(event.getStartDate().getMillis() -
                    DateUtil.now(zone).getMillis()) <
                    Math.abs(otherEvent.getStartDate().getMillis() -
                            DateUtil.now(zone).getMillis())) {
                toDelete.add(otherEvent);
                eventIds.put(event.getEventId(), event);
            } else {
                toDelete.add(event);
            }
        }
        eventList.removeAll(toDelete);
    }

    public DateTime getEndOfTimeRange() {
        return mEndOfTimeRange;
    }

    public DateTime getStartOfTimeRange() {
        return mStartOfTimeRange;
    }

    private List<CalendarEvent> getTimeFilteredEventList() {
        Uri.Builder builder = Instances.CONTENT_URI.buildUpon();
        ContentUris.appendId(builder, getStartOfTimeRangeForQuery(mStartOfTimeRange));
        ContentUris.appendId(builder, mEndOfTimeRange.getMillis());
        List<CalendarEvent> eventList = queryList(builder.build(), getCalendarSelection());
        // Above filters are not exactly correct for AllDay events: for them that filter
        // time should be moved by a time zone... (i.e. by several hours)
        // This is why we need to do additional filtering after querying a Content Provider:
        for (Iterator<CalendarEvent> it = eventList.iterator(); it.hasNext(); ) {
            CalendarEvent event = it.next();
            if (!event.getEndDate().isAfter(mStartOfTimeRange)
                    || !mEndOfTimeRange.isAfter(event.getStartDate())) {
                // We remove using Iterator to avoid ConcurrentModificationException
                it.remove();
            }
        }
        return eventList;
    }

    private long getStartOfTimeRangeForQuery(DateTime startOfTimeRange) {
        int offset = zone.getOffset(startOfTimeRange);
        if (offset >= 0) {
            return startOfTimeRange.getMillis();
        } else {
            return startOfTimeRange.getMillis() + offset;
        }
    }

    private String getCalendarSelection() {
        Set<String> activeCalendars = getSettings().getActiveCalendars();
        StringBuilder stringBuilder = new StringBuilder(EXCLUDE_DECLINED).append(AND).append(EXCLUDE_CANCELED);

        if (!activeCalendars.isEmpty()) {
            stringBuilder.append(AND_BRACKET);
            Iterator<String> iterator = activeCalendars.iterator();
            while (iterator.hasNext()) {
                String calendarId = iterator.next();
                stringBuilder.append(Instances.CALENDAR_ID);
                stringBuilder.append(EQUALS);
                stringBuilder.append(calendarId);
                if (iterator.hasNext()) {
                    stringBuilder.append(OR);
                }
            }
            stringBuilder.append(CLOSING_BRACKET);
        }

        return stringBuilder.toString();
    }

    private List<CalendarEvent> queryList(Uri uri, String selection) {
        QueryResult result = new QueryResult(getSettings(), QueryResult.QueryResultType.CALENDAR, uri, getProjection(), selection);

        List<CalendarEvent> eventList = queryProviderAndStoreResults(uri, getProjection(), selection, result, this::createCalendarEvent);
        QueryResultsStorage.storeResult(result);

        return eventList.stream().filter(event -> !mKeywordsFilter.matched(event.getTitle())).collect(Collectors.toList());
    }

    public static String[] getProjection() {
        List<String> columnNames = new ArrayList<>();
        columnNames.add(Instances.EVENT_ID);
        columnNames.add(Instances.TITLE);
        columnNames.add(Instances.BEGIN);
        columnNames.add(Instances.END);
        columnNames.add(Instances.ALL_DAY);
        columnNames.add(Instances.EVENT_LOCATION);
        columnNames.add(Instances.HAS_ALARM);
        columnNames.add(Instances.RRULE);
        columnNames.add(Instances.DISPLAY_COLOR);
        return columnNames.toArray(new String[0]);
    }

    private CalendarEvent createCalendarEvent(Cursor cursor) {
        boolean allDay = cursor.getInt(cursor.getColumnIndex(Instances.ALL_DAY)) > 0;
        CalendarEvent event = new CalendarEvent(settings, zone, allDay);
        event.setEventId(cursor.getInt(cursor.getColumnIndex(Instances.EVENT_ID)));
        event.setTitle(cursor.getString(cursor.getColumnIndex(Instances.TITLE)));
        event.setStartMillis(cursor.getLong(cursor.getColumnIndex(Instances.BEGIN)));
        event.setEndMillis(cursor.getLong(cursor.getColumnIndex(Instances.END)));
        event.setLocation(cursor.getString(cursor.getColumnIndex(Instances.EVENT_LOCATION)));
        event.setAlarmActive(cursor.getInt(cursor.getColumnIndex(Instances.HAS_ALARM)) > 0);
        event.setRecurring(cursor.getString(cursor.getColumnIndex(Instances.RRULE)) != null);
        event.setColor(getAsOpaque(getEventColor(cursor)));
        return event;
    }

    private int getEventColor(Cursor cursor) {
        return cursor.getInt(cursor.getColumnIndex(Instances.DISPLAY_COLOR));
    }

    public List<EventSource> getCalendars() {
        String[] projection = new String[]{
                Calendars._ID,
                Calendars.CALENDAR_DISPLAY_NAME,
                Calendars.ACCOUNT_NAME,
                Calendars.CALENDAR_COLOR,
        };

        return queryProvider(Calendars.CONTENT_URI, projection, null, cursor -> {
            int idIdx = cursor.getColumnIndex(CalendarContract.Calendars._ID);
            int nameIdx = cursor.getColumnIndex(CalendarContract.Calendars.CALENDAR_DISPLAY_NAME);
            int accountIdx = cursor.getColumnIndex(CalendarContract.Calendars.ACCOUNT_NAME);
            int colorIdx = cursor.getColumnIndex(CalendarContract.Calendars.CALENDAR_COLOR);
            return new EventSource(String.valueOf(cursor.getInt(idIdx)), cursor.getString(nameIdx),
                    cursor.getString(accountIdx), cursor.getInt(colorIdx));
        });
    }

    public Intent createOpenCalendarEventIntent(CalendarEvent event) {
        Intent intent = CalendarIntentUtil.createViewIntent();
        intent.setData(ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, event.getEventId()));
        intent.putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, event.getStartMillis());
        intent.putExtra(CalendarContract.EXTRA_EVENT_END_TIME, event.getEndMillis());
        return intent;
    }

    public static List<ContentObserver> registerObservers(Context context, Supplier<ContentObserver> observerCreator) {
        if (PermissionsUtil.arePermissionsGranted(context)) {
            ContentObserver observer = observerCreator.get();
            context.getContentResolver().registerContentObserver(CalendarContract.CONTENT_URI, false,
                    observer);
            return Collections.singletonList(observer);
        } else {
            return Collections.emptyList();
        }
    }
}
