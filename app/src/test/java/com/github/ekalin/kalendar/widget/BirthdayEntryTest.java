package com.github.ekalin.kalendar.widget;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;
import org.junit.Test;

import com.github.ekalin.kalendar.birthday.BirthdayEvent;

import static com.google.common.truth.Truth.assertThat;


public class BirthdayEntryTest {
    @Test
    public void startDateIsTheEventDate() {
        BirthdayEvent event = new BirthdayEvent();
        event.setDate(new LocalDate(2020, 9, 18));
        event.setZone(DateTimeZone.getDefault());

        BirthdayEntry entry = BirthdayEntry.fromEvent(event);

        assertThat(entry.getStartDate()).isEqualTo(new DateTime(2020, 9, 18, 0, 0, 0, 0, DateTimeZone.getDefault()));
    }
}
