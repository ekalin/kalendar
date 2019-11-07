package org.andstatus.todoagenda.calendar;

import android.content.Context;

import org.joda.time.DateTimeZone;
import org.junit.Test;
import org.mockito.Mockito;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static com.google.common.truth.Truth.assertThat;

public class CalendarEventTest {
    /* See https://github.com/plusonelabs/calendar-widget/issues/186 */
    @Test
    public void setStartDate_shouldAvoidIllegalInstantException() {
        DateTimeZone zone = DateTimeZone.forID("America/Sao_Paulo");
        CalendarEvent event = new CalendarEvent(Mockito.mock(Context.class), 1, zone, true);

        long millis = toMillis("2018-11-04T00:00:00");
        event.setStartMillis(millis);

        assertThat(event.getStartDate().getMillis()).isEqualTo(toMillis("2018-11-04T01:00:00"));
    }

    private long toMillis(String iso8601time) {
        Date date;
        try {
            date = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse(iso8601time);
        } catch (ParseException e) {
            throw new IllegalArgumentException(iso8601time, e);
        }
        return date.getTime();
    }
}
