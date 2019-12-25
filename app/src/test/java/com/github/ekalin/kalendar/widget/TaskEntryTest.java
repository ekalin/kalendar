package com.github.ekalin.kalendar.widget;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.Test;

import com.github.ekalin.kalendar.task.TaskEvent;

import static com.google.common.truth.Truth.assertThat;

public class TaskEntryTest {
    @Test
    public void startDateOfEntryIsTodayIfStartDateOfTaskIsInThePast() {
        TaskEvent task = new TaskEvent();
        task.setZone(DateTimeZone.getDefault());
        task.setDates(DateTime.now().minusDays(2).getMillis(), null);
        TaskEntry taskEntry = TaskEntry.fromEvent(task);

        assertThat(taskEntry.getStartDate()).isEqualTo(DateTime.now().withTimeAtStartOfDay());
    }

    @Test
    public void startDateOfEntryIsStartDateOfTaskIfTaskIsNotInThePast() {
        DateTime startDate = DateTime.now().plusHours(3);

        TaskEvent task = new TaskEvent();
        task.setZone(DateTimeZone.getDefault());
        task.setDates(startDate.getMillis(), null);
        TaskEntry taskEntry = TaskEntry.fromEvent(task);

        assertThat(taskEntry.getStartDate()).isEqualTo(startDate);
    }

    @Test
    public void isCurrent_forTodayTask_returnsTrue() {
        TaskEvent task = new TaskEvent();
        task.setZone(DateTimeZone.getDefault());
        task.setDates(DateTime.now().withTimeAtStartOfDay().plusHours(5).getMillis(), null);
        TaskEntry taskEntry = TaskEntry.fromEvent(task);

        assertThat(taskEntry.isCurrent()).isTrue();
    }

    @Test
    public void isCurrent_forFutureTask_returnsFalse() {
        TaskEvent task = new TaskEvent();
        task.setZone(DateTimeZone.getDefault());
        task.setDates(DateTime.now().plusDays(2).getMillis(), null);
        TaskEntry taskEntry = TaskEntry.fromEvent(task);

        assertThat(taskEntry.isCurrent()).isFalse();
    }
}
