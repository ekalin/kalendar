package org.andstatus.todoagenda.widget;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.Test;

import static com.google.common.truth.Truth.assertThat;

public class DayHeaderTest {
    private final DateTimeZone zone = DateTimeZone.getDefault();

    @Test
    public void isCurrent_forToday_returnsTrue() {
        DayHeader dayHeader = new DayHeader(DateTime.now(), zone);

        assertThat(dayHeader.isCurrent()).isTrue();
    }

    @Test
    public void isCurrent_forFutureDay_returnsFalse() {
        DayHeader dayHeader = new DayHeader(DateTime.now().plusDays(5), zone);

        assertThat(dayHeader.isCurrent()).isFalse();
    }
}
