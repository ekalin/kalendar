package org.andstatus.todoagenda.widget;

import org.andstatus.todoagenda.task.TaskEvent;

public class TaskEntry extends WidgetEntry {
    private TaskEvent event;

    private TaskEntry() {
        super(20);
    }

    public static TaskEntry fromEvent(TaskEvent event) {
        TaskEntry entry = new TaskEntry();
        entry.event = event;
        entry.setStartDate(event.getTaskDate());
        return entry;
    }

    public String getTitle() {
        return event.getTitle();
    }

    public TaskEvent getEvent() {
        return event;
    }
}
