package org.andstatus.todoagenda.widget;

import android.content.Context;

import org.andstatus.todoagenda.calendar.CalendarEvent;
import org.andstatus.todoagenda.util.DateUtil;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import static com.google.common.truth.Truth.assertThat;

public class CalendarEntry_TimesTest {
    private final Context context = Mockito.mock(Context.class);
    private final int widgetId = 1;
    private final DateTimeZone zone = DateTimeZone.getDefault();
    private DateTime now;

    @Before
    public void setupDateTime() {
        now = new DateTime(2019, 8, 10, 12, 0);
        DateUtil.setNow(now);
    }

    @Test
    public void isCurrent_forCurrentEvent_returnsTrue() {
        CalendarEvent event = new CalendarEvent(context, widgetId, zone, false);
        event.setStartDate(now.minusHours(1));
        event.setEndDate(now.plusMinutes(10));
        CalendarEntry calendarEntry = CalendarEntry.fromEvent(event, event.getStartDate());

        assertThat(calendarEntry.isCurrent()).isTrue();
    }

    @Test
    public void isCurrent_forPastEvent_returnsFalse() {
        CalendarEvent event = new CalendarEvent(context, widgetId, zone, false);
        event.setStartDate(now.minusHours(2));
        event.setEndDate(now.minusHours(1));
        CalendarEntry calendarEntry = CalendarEntry.fromEvent(event, event.getStartDate());

        assertThat(calendarEntry.isCurrent()).isFalse();
    }

    @Test
    public void isCurrent_forFutureEvent_returnsFalse() {
        CalendarEvent event = new CalendarEvent(context, widgetId, zone, false);
        event.setStartDate(now.plusSeconds(10));
        event.setEndDate(now.plusMinutes(15));
        CalendarEntry calendarEntry = CalendarEntry.fromEvent(event, event.getStartDate());

        assertThat(calendarEntry.isCurrent()).isFalse();
    }

    @Test
    public void isCurrent_forAllDayEventToday_returnsTrue() {
        CalendarEvent event = new CalendarEvent(context, widgetId, zone, true);
        event.setStartDate(now.withTimeAtStartOfDay());
        event.setEndDate(DateUtil.startOfNextDay(now));
        CalendarEntry calendarEntry = CalendarEntry.fromEvent(event, event.getStartDate());

        assertThat(calendarEntry.isCurrent()).isTrue();
    }

    @Test
    public void getNextUpdateTime_forFutureEvent_returnsStartTime() {
        final DateTime startDate = now.plusMinutes(30);

        CalendarEvent event = new CalendarEvent(context, widgetId, zone, false);
        event.setStartDate(startDate);
        event.setEndDate(now.plusMinutes(45));
        CalendarEntry calendarEntry = CalendarEntry.fromEvent(event, event.getStartDate());

        assertThat(calendarEntry.getNextUpdateTime()).isEqualTo(startDate);
    }

    @Test
    public void getNextUpdateTime_forCurrentEvent_returnsEndTime() {
        final DateTime endDate = now.plusHours(2);

        CalendarEvent event = new CalendarEvent(context, widgetId, zone, false);
        event.setStartDate(now.minusHours(2));
        event.setEndDate(endDate);
        CalendarEntry calendarEntry = CalendarEntry.fromEvent(event, event.getStartDate());

        assertThat(calendarEntry.getNextUpdateTime()).isEqualTo(endDate);
    }

    @Test
    public void getNextUpdateTime_forPastEvent_returnsNull() {
        CalendarEvent event = new CalendarEvent(context, widgetId, zone, false);
        event.setStartDate(now.minusHours(2));
        event.setEndDate(now.minusMinutes(1));
        CalendarEntry calendarEntry = CalendarEntry.fromEvent(event, event.getStartDate());

        assertThat(calendarEntry.getNextUpdateTime()).isNull();
    }

    @After
    public void unsetDate() {
        DateUtil.setNow(null);
    }
}
