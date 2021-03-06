package com.github.ekalin.kalendar.util;

import android.content.Context;
import android.text.format.DateUtils;
import android.text.format.DateUtilsMock;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.mockito.quality.Strictness;

import java.util.Formatter;

import com.github.ekalin.kalendar.EndedSomeTimeAgo;
import com.github.ekalin.kalendar.prefs.InstanceSettings;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class DateUtilTest {
    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule().strictness(Strictness.STRICT_STUBS);

    @Mock
    private Context context;
    @Mock
    private InstanceSettings settings;

    @Before
    public void setupContext() {
        Mockito.lenient().when(settings.getContext()).thenReturn(context);
    }

    @After
    public void resetDate() {
        DateUtil.setNow(null);
    }

    @Test
    public void formatDateTime_withoutLockedTZ_callsFormatDateTime() {
        when(settings.isTimeZoneLocked()).thenReturn(false);

        when(DateUtilsMock.INSTANCE.formatDateTime(any(Context.class), anyLong(), anyInt())).thenReturn("[DT]");

        DateTime testDate = DateTime.now();
        int flags = DateUtils.FORMAT_SHOW_WEEKDAY | DateUtils.FORMAT_SHOW_DATE;
        String result = DateUtil.formatDateTime(settings, testDate, flags);

        assertThat(result).isEqualTo("[DT]");
        verify(DateUtilsMock.INSTANCE).formatDateTime(context, testDate.getMillis(), flags);
    }

    @Test
    public void formatDateTime_withtLockedTZ_callsFormatDateRangeWithTZ() {
        when(settings.isTimeZoneLocked()).thenReturn(true);
        when(settings.getLockedTimeZoneId()).thenReturn("TZ");

        Formatter returnedFormatter = new Formatter();
        when(DateUtilsMock.INSTANCE.formatDateRange(any(Context.class), any(Formatter.class), anyLong(),
                anyLong(), anyInt(), anyString())).thenReturn(returnedFormatter);

        DateTime testDate = DateTime.now();
        int flags = DateUtils.FORMAT_ABBREV_ALL | DateUtils.FORMAT_SHOW_DATE;
        String result = DateUtil.formatDateTime(settings, testDate, flags);

        assertThat(result).isEqualTo(returnedFormatter.toString());
        verify(DateUtilsMock.INSTANCE).formatDateRange(eq(context), any(Formatter.class),
                eq(testDate.getMillis()), eq(testDate.getMillis()), eq(flags), eq("TZ"));
    }

    @Test
    public void createDayHeaderTitle_withAbbrevDate_passesAbbrevFlag() {
        when(settings.getAbbreviateDates()).thenReturn(true);
        when(settings.isTimeZoneLocked()).thenReturn(false);

        when(DateUtilsMock.INSTANCE.formatDateTime(any(Context.class), anyLong(), anyInt())).thenReturn("[DT]");

        DateTime testDate = DateTime.now().plusHours(2);
        String dayHeaderTitle = DateUtil.createDayHeaderTitle(settings, testDate);

        assertThat(dayHeaderTitle).isEqualTo("[DT]");
        verify(DateUtilsMock.INSTANCE).formatDateTime(context, testDate.getMillis(),
                DateUtils.FORMAT_ABBREV_ALL | DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_WEEKDAY);
    }

    @Test
    public void createDayHeaderTitle_withoutAbbrevDate_withFutureDate_returnsTheDate() {
        when(settings.getAbbreviateDates()).thenReturn(false);

        when(DateUtilsMock.INSTANCE.formatDateTime(any(Context.class), anyLong(), anyInt())).thenReturn("[DT]");

        DateTime testDate = DateTime.now().plusDays(5);
        String dayHeaderTitle = DateUtil.createDayHeaderTitle(settings, testDate);

        assertThat(dayHeaderTitle).isEqualTo("[DT]");
        verify(DateUtilsMock.INSTANCE).formatDateTime(context, testDate.getMillis(),
                DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_WEEKDAY);
    }

    @Test
    public void createDayHeaderTitle_withoutAbbrevDate_withTomorrow_returnsTheDate() {
        when(settings.getAbbreviateDates()).thenReturn(false);

        when(context.getString(anyInt())).thenReturn("Tomorrow");
        when(DateUtilsMock.INSTANCE.formatDateTime(any(Context.class), anyLong(), anyInt())).thenReturn("[DT]");

        DateTime testDate = DateTime.now().plusDays(1).withTimeAtStartOfDay().plusHours(13);
        String dayHeaderTitle = DateUtil.createDayHeaderTitle(settings, testDate);

        assertThat(dayHeaderTitle).isEqualTo("Tomorrow, [DT]");
        verify(DateUtilsMock.INSTANCE).formatDateTime(context, testDate.getMillis(), DateUtils.FORMAT_SHOW_DATE);
    }

    @Test
    public void createDayHeaderTitle_withoutAbbrevDate_withToday_returnsOnlyToday() {
        when(settings.getAbbreviateDates()).thenReturn(false);

        when(context.getString(anyInt())).thenReturn("Today");

        DateTime testDate = DateTime.now().withTimeAtStartOfDay().plusHours(2);
        String dayHeaderTitle = DateUtil.createDayHeaderTitle(settings, testDate);

        assertThat(dayHeaderTitle).isEqualTo("Today");
    }

    @Test
    public void parseContactsDate_withJustDate() {
        String dateOnly = "1995-10-04";

        LocalDate result = DateUtil.parseContactDate(dateOnly);

        assertThat(result).isEqualTo(new LocalDate(1995, 10, 4));
    }

    @Test
    public void parseContactsDate_withTime_disregardsTime() {
        String dateOnly = "1995-10-04T01:30:00.000Z";

        LocalDate result = DateUtil.parseContactDate(dateOnly);

        assertThat(result).isEqualTo(new LocalDate(1995, 10, 4));
    }

    @Test
    public void parseContactsDate_withoutYear() {
        String dateOnly = "--10-04";

        LocalDate result = DateUtil.parseContactDate(dateOnly);

        assertThat(result).isEqualTo(new LocalDate(2000, 10, 4));
    }

    @Test
    public void birthDateToDisplayedBirthday_setsYearToCurrentYear() {
        DateUtil.setNow(new DateTime(2020, 10, 4, 12, 50));
        when(settings.getTimeZone()).thenReturn(DateTimeZone.UTC);

        LocalDate birthDate = new LocalDate(1995, 11, 15);

        LocalDate result = DateUtil.birthDateToDisplayedBirthday(birthDate, settings);

        assertThat(result).isEqualTo(birthDate.withYear(2020));
    }

    @Test
    public void birthDateToDisplayedBirthday_setsYearToNextYear_ifBirthdayHasPassed() {
        DateUtil.setNow(new DateTime(2020, 10, 4, 12, 50));
        when(settings.getTimeZone()).thenReturn(DateTimeZone.UTC);
        when(settings.getEventsEnded()).thenReturn(EndedSomeTimeAgo.NONE);

        LocalDate birthDate = new LocalDate(1995, 10, 3);

        LocalDate result = DateUtil.birthDateToDisplayedBirthday(birthDate, settings);

        assertThat(result).isEqualTo(birthDate.withYear(2021));
    }

    @Test
    public void birthDateToDisplayedBirthday_returnsPastBirthday_ifIsToBeDisplayedInPastEvents() {
        DateUtil.setNow(new DateTime(2020, 10, 4, 12, 50));
        when(settings.getTimeZone()).thenReturn(DateTimeZone.UTC);
        when(settings.getEventsEnded()).thenReturn(EndedSomeTimeAgo.YESTERDAY);

        LocalDate birthDate = new LocalDate(1995, 10, 3);

        LocalDate result = DateUtil.birthDateToDisplayedBirthday(birthDate, settings);

        assertThat(result).isEqualTo(birthDate.withYear(2020));
    }
}
