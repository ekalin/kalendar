package com.github.ekalin.kalendar.calendar;

import android.content.Context;
import androidx.test.core.app.ApplicationProvider;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.mockito.quality.Strictness;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.util.ReflectionHelpers;

import java.util.Collections;
import java.util.List;

import com.github.ekalin.kalendar.prefs.AllSettings;
import com.github.ekalin.kalendar.prefs.InstanceSettings;
import com.github.ekalin.kalendar.util.DateUtil;
import com.github.ekalin.kalendar.widget.CalendarEntry;

import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth.assertWithMessage;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class CalendarEventVisualizerTest {
    private final int widgetId = 1;
    private final DateTimeZone zone = DateTimeZone.getDefault();

    private Context context;
    private InstanceSettings settings;
    private DateTime now;

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule().strictness(Strictness.STRICT_STUBS);

    @Mock
    private CalendarEventProvider provider;

    private CalendarEventVisualizer visualizer;

    @Before
    public void setup() {
        now = new DateTime(2019, 8, 10, 12, 0);
        DateUtil.setNow(now);

        context = ApplicationProvider.getApplicationContext();
        settings = AllSettings.instanceFromId(context, 1);

        visualizer = new CalendarEventVisualizer(context, 1);
        ReflectionHelpers.setField(visualizer, "calendarContentProvider", provider);

        when(provider.getStartOfTimeRange()).thenReturn(now.withTimeAtStartOfDay());
        when(provider.getEndOfTimeRange()).thenReturn(now.plusDays(5).withTimeAtStartOfDay());
    }

    /* https://github.com/plusonelabs/calendar-widget/issues/199 */
    @Test
    public void valuesForTodaysCurrentEvent_areReturnedCorrectly() {
        CalendarEvent event = new CalendarEvent(settings, zone, false);
        event.setStartMillis(now.minusHours(1).getMillis());
        event.setEndMillis(now.plusHours(2).getMillis());

        when(provider.getEvents()).thenReturn(Collections.singletonList(event));
        List<CalendarEntry> entries = visualizer.getEventEntries();
        assertThat(entries).hasSize(1);
        CalendarEntry calendarEntry = entries.get(0);

        assertWithMessage("Is active event").that(calendarEntry.getEvent().isActive()).isTrue();
        assertWithMessage("Is part of multi day event").that(calendarEntry.isPartOfMultiDayEvent()).isFalse();
        assertWithMessage("Is start of multi day event").that(calendarEntry.isStartOfMultiDayEvent()).isFalse();
        assertWithMessage("Is end of multi day event").that(calendarEntry.isEndOfMultiDayEvent()).isFalse();
        assertWithMessage("Start time shouldn't change for today's event").that(calendarEntry.getStartDate()).isEqualTo(event.getStartDate());
        assertWithMessage("End time shouldn't change for today's event").that(calendarEntry.getEndDate()).isEqualTo(event.getEndDate());
    }

    /* https://github.com/plusonelabs/calendar-widget/issues/199 */
    @Test
    public void valuesForCurrentEventStartedYesterday_areReturnedCorrectly() {
        CalendarEvent event = new CalendarEvent(settings, zone, false);
        event.setStartMillis(now.minusDays(1).minusHours(1).getMillis());
        event.setEndMillis(now.plusHours(2).getMillis());

        when(provider.getEvents()).thenReturn(Collections.singletonList(event));
        List<CalendarEntry> entries = visualizer.getEventEntries();
        assertThat(entries).hasSize(1);
        CalendarEntry calendarEntry = entries.get(0);

        assertWithMessage("Is active event").that(calendarEntry.getEvent().isActive()).isTrue();
        assertWithMessage("Is part of multi day event").that(calendarEntry.isPartOfMultiDayEvent()).isTrue();
        assertWithMessage("Is start of multi day event").that(calendarEntry.isStartOfMultiDayEvent()).isFalse();
        assertWithMessage("Is end of multi day event").that(calendarEntry.isEndOfMultiDayEvent()).isTrue();
        assertWithMessage("Start time for event started yesterday is midnight").that(calendarEntry.getStartDate()).isEqualTo(now.withTimeAtStartOfDay());
        assertWithMessage("End time shouldn't change for event started yesterday").that(calendarEntry.getEndDate()).isEqualTo(event.getEndDate());
    }

    /* https://github.com/plusonelabs/calendar-widget/issues/199 */
    @Test
    public void valuesForCurrentEventEndingTomorrow_areReturnedCorrectly() {
        CalendarEvent event = new CalendarEvent(settings, zone, false);
        event.setStartMillis(now.minusHours(1).getMillis());
        event.setEndMillis(now.plusDays(1).plusHours(2).getMillis());

        when(provider.getEvents()).thenReturn(Collections.singletonList(event));
        List<CalendarEntry> entries = visualizer.getEventEntries();
        assertThat(entries).hasSize(2);

        CalendarEntry todaysEvent = entries.get(0);
        assertWithMessage("[Today] Is active event").that(todaysEvent.getEvent().isActive()).isTrue();
        assertWithMessage("[Today] Is part of multi day event").that(todaysEvent.isPartOfMultiDayEvent()).isTrue();
        assertWithMessage("[Today] Is start of multi day event").that(todaysEvent.isStartOfMultiDayEvent()).isTrue();
        assertWithMessage("[Today] Is end of multi day event").that(todaysEvent.isEndOfMultiDayEvent()).isFalse();
        assertWithMessage("[Today] Start time shouldn't change for event ending tomorrow").that(todaysEvent.getStartDate()).isEqualTo(event.getStartDate());
        assertWithMessage("[Today] End time for event ending tomorrow is next midnight").that(todaysEvent.getEndDate()).isEqualTo(now.plusDays(1).withTimeAtStartOfDay());

        CalendarEntry tomorrowsEvent = entries.get(1);
        assertWithMessage("[Tomorrow] Is active event").that(tomorrowsEvent.getEvent().isActive()).isTrue();
        assertWithMessage("[Tomorrow] Is part of multi day event").that(tomorrowsEvent.isPartOfMultiDayEvent()).isTrue();
        assertWithMessage("[Tomorrow] Is start of multi day event").that(tomorrowsEvent.isStartOfMultiDayEvent()).isFalse();
        assertWithMessage("[Tomorrow] Is end of multi day event").that(tomorrowsEvent.isEndOfMultiDayEvent()).isTrue();
        assertWithMessage("[Tomorrow] Start time for event ending tomorrow is next midnight").that(tomorrowsEvent.getStartDate()).isEqualTo(now.plusDays(1).withTimeAtStartOfDay());
        assertWithMessage("[Tomorrow] End time for event ending tomorrow is next midnight").that(tomorrowsEvent.getEndDate()).isEqualTo(event.getEndDate());
    }

    @After
    public void unsetDate() {
        DateUtil.setNow(null);
    }
}
