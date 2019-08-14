package org.andstatus.todoagenda.widget;

import org.joda.time.DateTime;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.google.common.truth.Truth.assertThat;

public class WidgetEntryTest {
    @Test
    public void getStartDay_returnsMidnightOfDay() {
        WidgetEntry entry = new WidgetEntryForTest(new DateTime(2019, 8, 7, 18, 8));
        assertThat(entry.getStartDay()).isEqualTo(new DateTime(2019, 8, 7, 0, 0));
    }

    @Test
    public void getDaysFromToday_returnsNumberOfDays() {
        DateTime eventDate = DateTime.now().withTimeAtStartOfDay().plusDays(4).plusHours(7);
        WidgetEntry entry = new WidgetEntryForTest(eventDate);

        assertThat(entry.getDaysFromToday()).isEqualTo(4);
    }

    @Test
    public void sortsByStartDateThenPriority() {
        WidgetEntry event_day7_12h_prio20 = new WidgetEntryForTest(new DateTime(2019, 1, 7, 12, 0), 20);
        WidgetEntry event_day7_8h_prio20 = new WidgetEntryForTest(new DateTime(2019, 1, 7, 8, 0), 20);
        WidgetEntry event_day8_15h_prio20 = new WidgetEntryForTest(new DateTime(2019, 1, 8, 15, 0), 20);
        WidgetEntry event_day8_15h_prio10 = new WidgetEntryForTest(new DateTime(2019, 1, 8, 15, 0), 10);
        List<WidgetEntry> events = Arrays.asList(event_day7_12h_prio20, event_day7_8h_prio20, event_day8_15h_prio20,
                event_day8_15h_prio10);

        Collections.sort(events);

        assertThat(events).containsExactly(event_day7_8h_prio20, event_day7_12h_prio20, event_day8_15h_prio10,
                event_day8_15h_prio20).inOrder();
    }

    private static class WidgetEntryForTest extends WidgetEntry {
        private final int priority;

        private WidgetEntryForTest(DateTime startDate) {
            this(startDate, 0);
        }

        private WidgetEntryForTest(DateTime startDate, int priority) {
            setStartDate(startDate);
            this.priority = priority;
        }

        @Override
        public int getPriority() {
            return priority;
        }
    }
}
