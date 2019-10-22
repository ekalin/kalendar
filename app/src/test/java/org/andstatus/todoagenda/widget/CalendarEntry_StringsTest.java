package org.andstatus.todoagenda.widget;

import android.content.Context;
import android.text.format.DateUtils;
import android.text.format.DateUtilsMock;

import org.andstatus.todoagenda.calendar.CalendarEvent;
import org.andstatus.todoagenda.prefs.InstanceSettings;
import org.andstatus.todoagenda.util.DateUtil;
import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.mockito.quality.Strictness;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.when;

public class CalendarEntry_StringsTest {
    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule().strictness(Strictness.STRICT_STUBS);

    @Mock
    private CalendarEvent event;
    @Mock
    private InstanceSettings settings;
    @Mock
    private Context context;

    @Before
    public void setup() {
        DateTime now = new DateTime(2019, 8, 10, 12, 0);
        DateUtil.setNow(now);

        final DateTime startTime = now.plusHours(1);
        final DateTime endTime = now.plusHours(2);

        Mockito.lenient().when(event.getStartDate()).thenReturn(startTime);
        Mockito.lenient().when(event.getEndDate()).thenReturn(endTime);
        Mockito.lenient().when(event.getSettings()).thenReturn(settings);

        Mockito.lenient().when(settings.getContext()).thenReturn(context);
        Mockito.lenient().when(settings.getDateFormat()).thenReturn("auto");

        Mockito.lenient().when(DateUtilsMock.INSTANCE.formatDateTime(context,
                startTime.getMillis(), DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_24HOUR))
                .thenReturn("[Start]");
        Mockito.lenient().when(DateUtilsMock.INSTANCE.formatDateTime(context,
                endTime.getMillis(), DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_24HOUR))
                .thenReturn("[End]");
    }

    @Test
    public void getEventTimeString_spanningOneFullDay_returnsEmpty() {
        final DateTime start = DateTime.now();
        final DateTime end = start.plusDays(1);

        when(event.getStartDate()).thenReturn(start);
        when(event.getEndDate()).thenReturn(end);
        CalendarEntry entry = CalendarEntry.fromEvent(event, event.getStartDate());

        assertThat(entry.getEventTimeString()).isEmpty();
    }

    @Test
    public void getEventTimeString_forAllDayEvent_returnsEmpty() {
        when(settings.getFillAllDayEvents()).thenReturn(true);

        when(event.isAllDay()).thenReturn(true);

        CalendarEntry entry = CalendarEntry.fromEvent(event, event.getStartDate());

        assertThat(entry.getEventTimeString()).isEmpty();
    }

    @Test
    public void getEventTimeString_forRegularEvent_withoutEndTime_returnsStartTime() {
        when(settings.getShowEndTime()).thenReturn(false);

        CalendarEntry entry = CalendarEntry.fromEvent(event, event.getStartDate());

        assertThat(entry.getEventTimeString()).isEqualTo("[Start]");
    }

    @Test
    public void getEventTimeString_forRegularEvent_withEndTime_returnsTimes() {
        when(settings.getShowEndTime()).thenReturn(true);

        CalendarEntry entry = CalendarEntry.fromEvent(event, event.getStartDate());

        assertThat(entry.getEventTimeString()).isEqualTo("[Start] - [End]");
    }

    @Test
    public void getEventTimeString_forStartOfMultidayEvent_returnsStartTimeAndArrow() {
        when(settings.getShowEndTime()).thenReturn(true);

        when(event.isPartOfMultiDayEvent()).thenReturn(true);
        CalendarEntry entry = CalendarEntry.fromEvent(event, event.getStartDate());
        entry.setEndDate(entry.getEndDate().withTimeAtStartOfDay());

        assertThat(entry.getEventTimeString()).isEqualTo("[Start] →");
    }

    @Test
    public void getEventTimeString_forEndOfMultidayEvent_returnsArrowAndEndTime() {
        when(settings.getShowEndTime()).thenReturn(true);

        when(event.isPartOfMultiDayEvent()).thenReturn(true);
        CalendarEntry entry = CalendarEntry.fromEvent(event, event.getStartDate());
        entry.setStartDate(entry.getStartDate().withTimeAtStartOfDay().plusDays(1));

        assertThat(entry.getEventTimeString()).isEqualTo("→ [End]");
    }

    @Test
    public void getEventTimeString_forMultiDayAllDayEvent_returnsArrowAndEndDate() {
        when(DateUtilsMock.INSTANCE.formatDateTime(context, event.getEndDate().minusDays(1).getMillis(),
                DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_WEEKDAY))
                .thenReturn("[EndDay]");

        when(settings.getFillAllDayEvents()).thenReturn(false);

        when(event.isAllDay()).thenReturn(true);
        CalendarEntry entry = CalendarEntry.fromEvent(event, event.getStartDate());

        assertThat(entry.getEventTimeString()).isEqualTo("→ [EndDay]");
    }

    @Test
    public void getEventTimeString_forLastDayOfMultiDayEvent_returnsEmpty() {
        when(settings.getFillAllDayEvents()).thenReturn(false);

        when(event.isAllDay()).thenReturn(true);
        when(event.isPartOfMultiDayEvent()).thenReturn(true);
        CalendarEntry entry = CalendarEntry.fromEvent(event, event.getStartDate());
        entry.setEndDate(event.getEndDate().plusDays(1).withTimeAtStartOfDay());

        assertThat(entry.getEventTimeString()).isEmpty();
    }

    @Test
    public void getLocationString_withNullLocation_returnsEmpty() {
        when(event.getLocation()).thenReturn(null);

        CalendarEntry entry = CalendarEntry.fromEvent(event, event.getStartDate());

        assertThat(entry.getLocationString()).isEmpty();
    }

    @Test
    public void getLocationString_withEmptyLocation_returnsEmpty() {
        when(event.getLocation()).thenReturn("");
        CalendarEntry entry = CalendarEntry.fromEvent(event, event.getStartDate());

        assertThat(entry.getLocationString()).isEmpty();
    }

    @Test
    public void getLocationString_withLocationDisabled_returnsEmpty() {
        when(settings.getShowLocation()).thenReturn(false);

        when(event.getLocation()).thenReturn("15 Something St");
        CalendarEntry entry = CalendarEntry.fromEvent(event, event.getStartDate());

        assertThat(entry.getLocationString()).isEmpty();
    }

    @Test
    public void getLocationString_withLocation_returnsLocation() {
        final String location = "15 Something St";

        when(settings.getShowLocation()).thenReturn(true);

        when(event.getLocation()).thenReturn(location);
        CalendarEntry entry = CalendarEntry.fromEvent(event, event.getStartDate());

        assertThat(entry.getLocationString()).isEqualTo(location);
    }

    @After
    public void unsetDate() {
        DateUtil.setNow(null);
    }
}
