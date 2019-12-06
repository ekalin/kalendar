package org.andstatus.todoagenda.task;

import org.andstatus.todoagenda.testutil.DateAssert;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.Test;

import static com.google.common.truth.Truth.assertThat;

public class TaskEventTest {
    @Test
    public void setDates_withStartAndDue_usesBothDates() {
        DateTime start = DateTime.now().minusHours(1);
        DateTime due = DateTime.now().plusHours(2);

        TaskEvent taskEvent = new TaskEvent();
        taskEvent.setZone(DateTimeZone.getDefault());
        taskEvent.setDates(start.getMillis(), due.getMillis());

        assertThat(taskEvent.getStartDate()).isEqualTo(start);
        assertThat(taskEvent.getDueDate()).isEqualTo(due);
    }

    @Test
    public void setDates_withStartButNotDue_usesStartAsDueDate() {
        DateTime start = DateTime.now().minusHours(10);

        TaskEvent taskEvent = new TaskEvent();
        taskEvent.setZone(DateTimeZone.getDefault());
        taskEvent.setDates(start.getMillis(), null);

        assertThat(taskEvent.getStartDate()).isEqualTo(start);
        assertThat(taskEvent.getDueDate()).isEqualTo(start);
    }

    @Test
    public void setDates_withDueButNotStart_usesDueAsStart() {
        DateTime due = DateTime.now().plusDays(3);

        TaskEvent taskEvent = new TaskEvent();
        taskEvent.setZone(DateTimeZone.getDefault());
        taskEvent.setDates(null, due.getMillis());

        assertThat(taskEvent.getStartDate()).isEqualTo(due);
        assertThat(taskEvent.getDueDate()).isEqualTo(due);
    }

    @Test
    public void setDates_withoutAnyDates() {
        // Start date should be now
        // End date is set to a long time in the future so they are sorted last
        DateTime now = DateTime.now();

        TaskEvent taskEvent = new TaskEvent();
        taskEvent.setZone(DateTimeZone.getDefault());
        taskEvent.setDates(null, null);

        DateAssert.assertDatesWithTolerance(taskEvent.getStartDate(), now);
        assertThat(taskEvent.getDueDate()).isAtLeast(now.plusYears(50));
    }
}
