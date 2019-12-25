package com.github.ekalin.kalendar.widget;

import org.joda.time.DateTime;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.google.common.truth.Truth.assertThat;

public class WidgetEntryTest {
    @Test
    public void getStartDay_returnsMidnightOfDay() {
        WidgetEntry entry = createWidgetEntry(new DateTime(2019, 8, 7, 18, 8));
        assertThat(entry.getStartDay()).isEqualTo(new DateTime(2019, 8, 7, 0, 0));
    }

    @Test
    public void getDaysFromToday_returnsNumberOfDays() {
        DateTime eventDate = DateTime.now().withTimeAtStartOfDay().plusDays(4).plusHours(7);
        WidgetEntry entry = createWidgetEntry(eventDate);

        assertThat(entry.getDaysFromToday()).isEqualTo(4);
    }

    @Test
    public void sortsByStartDateThenPriority() {
        WidgetEntry event_day7_12h_prio20 = createWidgetEntry(new DateTime(2019, 1, 7, 12, 0), 20);
        WidgetEntry event_day7_8h_prio20 = createWidgetEntry(new DateTime(2019, 1, 7, 8, 0), 20);
        WidgetEntry event_day8_15h_prio20 = createWidgetEntry(new DateTime(2019, 1, 8, 15, 0), 20);
        WidgetEntry event_day8_15h_prio10 = createWidgetEntry(new DateTime(2019, 1, 8, 15, 0), 10);
        List<WidgetEntry> events = Arrays.asList(event_day7_12h_prio20, event_day7_8h_prio20, event_day8_15h_prio20,
                event_day8_15h_prio10);

        Collections.sort(events);

        assertThat(events).containsExactly(event_day7_8h_prio20, event_day7_12h_prio20, event_day8_15h_prio10,
                event_day8_15h_prio20).inOrder();
    }

    private WidgetEntry createWidgetEntry(DateTime startDate) {
        return createWidgetEntry(startDate, 0);
    }

    private WidgetEntry createWidgetEntry(DateTime startDate, int priority) {
        WidgetEntry entry = new WidgetEntry(priority);
        entry.setStartDate(startDate);
        return entry;
    }
}
