package org.andstatus.todoagenda.task;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;

public class TaskProvider {
    public List<TaskEvent> getEvents() {
        ArrayList<TaskEvent> events = new ArrayList<>();

        TaskEvent event = new TaskEvent();
        event.setStartDate(DateTime.now());
        event.setTitle("TODO for today");
        events.add(event);

        event = new TaskEvent();
        event.setStartDate(DateTime.now().plusDays(5));
        event.setTitle("TODO in 5 days time");
        events.add(event);

        return events;
    }
}
