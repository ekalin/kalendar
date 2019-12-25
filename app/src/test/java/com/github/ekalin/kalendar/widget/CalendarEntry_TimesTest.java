package com.github.ekalin.kalendar.widget;

import android.content.Context;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.github.ekalin.kalendar.calendar.CalendarEvent;
import com.github.ekalin.kalendar.prefs.InstanceSettings;
import com.github.ekalin.kalendar.util.DateUtil;

import static com.google.common.truth.Truth.assertThat;

public class CalendarEntry_TimesTest {
    private final Context context = Mockito.mock(Context.class);
    private final int widgetId = 1;
    private final InstanceSettings settings = Mockito.mock(InstanceSettings.class);
    private final DateTimeZone zone = DateTimeZone.getDefault();
    private DateTime now;

    @Before
    public void setupDateTime() {
        now = new DateTime(2019, 8, 10, 12, 0);
        DateUtil.setNow(now);
    }

    @Test
    public void isCurrent_forCurrentEvent_returnsTrue() {
        CalendarEvent event = new CalendarEvent(settings, zone, false);
        event.setStartMillis(now.minusHours(1).getMillis());
        event.setEndMillis(now.plusMinutes(10).getMillis());
        CalendarEntry calendarEntry = CalendarEntry.fromEvent(event, event.getStartDate());

        assertThat(calendarEntry.isCurrent()).isTrue();
    }

    @Test
    public void isCurrent_forPastEvent_returnsFalse() {
        CalendarEvent event = new CalendarEvent(settings, zone, false);
        event.setStartMillis(now.minusHours(2).getMillis());
        event.setEndMillis(now.minusHours(1).getMillis());
        CalendarEntry calendarEntry = CalendarEntry.fromEvent(event, event.getStartDate());

        assertThat(calendarEntry.isCurrent()).isFalse();
    }

    @Test
    public void isCurrent_forFutureEvent_returnsFalse() {
        CalendarEvent event = new CalendarEvent(settings, zone, false);
        event.setStartMillis(now.plusSeconds(10).getMillis());
        event.setEndMillis(now.plusMinutes(15).getMillis());
        CalendarEntry calendarEntry = CalendarEntry.fromEvent(event, event.getStartDate());

        assertThat(calendarEntry.isCurrent()).isFalse();
    }

    @Test
    public void isCurrent_forAllDayEventToday_returnsTrue() {
        CalendarEvent event = new CalendarEvent(settings, zone, true);
        event.setStartMillis(now.withTimeAtStartOfDay().getMillis());
        event.setEndMillis(DateUtil.startOfNextDay(now).getMillis());
        CalendarEntry calendarEntry = CalendarEntry.fromEvent(event, event.getStartDate());

        assertThat(calendarEntry.isCurrent()).isTrue();
    }

    @Test
    public void getNextUpdateTime_forFutureEvent_returnsStartTime() {
        final DateTime startDate = now.plusMinutes(30);

        CalendarEvent event = new CalendarEvent(settings, zone, false);
        event.setStartMillis(startDate.getMillis());
        event.setEndMillis(now.plusMinutes(45).getMillis());
        CalendarEntry calendarEntry = CalendarEntry.fromEvent(event, event.getStartDate());

        assertThat(calendarEntry.getNextUpdateTime()).isEqualTo(startDate);
    }

    @Test
    public void getNextUpdateTime_forCurrentEvent_returnsEndTime() {
        final DateTime endDate = now.plusHours(2);

        CalendarEvent event = new CalendarEvent(settings, zone, false);
        event.setStartMillis(now.minusHours(2).getMillis());
        event.setEndMillis(endDate.getMillis());
        CalendarEntry calendarEntry = CalendarEntry.fromEvent(event, event.getStartDate());

        assertThat(calendarEntry.getNextUpdateTime()).isEqualTo(endDate);
    }

    @Test
    public void getNextUpdateTime_forPastEvent_returnsNull() {
        CalendarEvent event = new CalendarEvent(settings, zone, false);
        event.setStartMillis(now.minusHours(2).getMillis());
        event.setEndMillis(now.minusMinutes(1).getMillis());
        CalendarEntry calendarEntry = CalendarEntry.fromEvent(event, event.getStartDate());

        assertThat(calendarEntry.getNextUpdateTime()).isNull();
    }

    @After
    public void unsetDate() {
        DateUtil.setNow(null);
    }
}
