package org.andstatus.todoagenda.calendar;

import android.content.Context;
import android.provider.CalendarContract;

import org.andstatus.todoagenda.prefs.AllSettings;
import org.andstatus.todoagenda.testutil.ContentProviderForTests;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;

import java.util.ArrayList;
import java.util.List;

import androidx.test.core.app.ApplicationProvider;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.assertThat;

@RunWith(RobolectricTestRunner.class)
public class CalendarEventProviderTest {
    private Context context;
    private ContentProviderForTests contentProvider;
    private int daysRange;
    private CalendarEventProvider calendarProvider;

    @Before
    public void setup() {
        context = ApplicationProvider.getApplicationContext();
        contentProvider = Robolectric.setupContentProvider(ContentProviderForTests.class,
                CalendarContract.Instances.CONTENT_URI.getAuthority());
        daysRange = AllSettings.instanceFromId(context, 1).getEventRange();
        calendarProvider = new CalendarEventProvider(context, 1);
    }

    @Test
    public void getEvents_shouldFilterEventsOutsideSearchRange() {
        setupEvents();

        List<CalendarEvent> events = calendarProvider.getEvents();

        List<String> titles = getTitles(events);
        assertThat(titles, containsInAnyOrder("Overlaps start range", "Inside range", "Overlaps end range"));
    }

    private void setupEvents() {
        DateTime start = DateTime.now();
        DateTime end = start.plusDays(daysRange);
        DateTimeZone zone = start.getZone();

        QueryResult queryResult = new QueryResult(1, DateTime.now());
        addCalendarRow(queryResult, createEvent(start.minusHours(2), start.minusHours(1), "Before start", zone));
        addCalendarRow(queryResult, createEvent(start.minusHours(1), start.plusHours(2), "Overlaps start range", zone));
        addCalendarRow(queryResult, createEvent(start.plusHours(2), start.plusHours(3), "Inside range", zone));
        addCalendarRow(queryResult, createEvent(end.minusHours(3), end.plusHours(1), "Overlaps end range", zone));
        addCalendarRow(queryResult, createEvent(end.plusHours(2), end.plusHours(4), "After end", zone));

        contentProvider.setQueryResult(queryResult);
    }

    private CalendarEvent createEvent(DateTime start, DateTime endDate, String title, DateTimeZone zone) {
        CalendarEvent event = new CalendarEvent(context, 1, zone, false);
        event.setStartDate(start);
        event.setEndDate(endDate);
        event.setTitle(title);
        return event;
    }

    private void addCalendarRow(QueryResult queryResult, CalendarEvent event) {
        queryResult.addRow(new QueryRow()
                .setEventId(event.getEventId())
                .setTitle(event.getTitle())
                .setBegin(event.getStartMillis())
                .setEnd(event.getEndMillis())
                .setDisplayColor(event.getColor())
                .setAllDay(event.isAllDay() ? 1 : 0)
                .setEventLocation(event.getLocation())
                .setHasAlarm(event.isAlarmActive() ? 1 : 0)
                .setRRule(event.isRecurring() ? "FREQ=WEEKLY;WKST=MO;BYDAY=MO,WE,FR" : null));
    }

    private List<String> getTitles(List<CalendarEvent> events) {
        List<String> titles = new ArrayList<>();
        for (CalendarEvent event : events) {
            titles.add(event.getTitle());
        }
        return titles;
    }
}
