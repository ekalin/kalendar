package org.andstatus.todoagenda.widget;

import org.andstatus.todoagenda.task.TaskEvent;
import org.andstatus.todoagenda.util.DateUtil;
import org.joda.time.DateTime;

public class TaskEntry extends WidgetEntry {
    private TaskEvent event;

    private TaskEntry() {
        super(20);
    }

    public static TaskEntry fromEvent(TaskEvent event) {
        TaskEntry entry = new TaskEntry();
        entry.event = event;

        DateTime now = DateUtil.now(event.getZone());
        if (event.getStartDate().isBefore(now)) {
            entry.setStartDate(now.withTimeAtStartOfDay());
        } else {
            entry.setStartDate(event.getStartDate());
        }

        return entry;
    }

    public String getTitle() {
        return event.getTitle();
    }

    public TaskEvent getEvent() {
        return event;
    }

    @Override
    public boolean isCurrent() {
        return getStartDate().withTimeAtStartOfDay().equals(DateUtil.now(event.getZone()).withTimeAtStartOfDay());
    }

    @Override
    protected DateTime getDateForSortingWithinDay() {
        return event.getDueDate();
    }

    @Override
    public String toString() {
        return "TaskEntry ["
                + "startDate=" + getStartDate()
                + ", event=" + event
                + "]";
    }
}
