package org.andstatus.todoagenda.task;

import org.joda.time.DateTime;

public class TaskEvent {
    private String title;
    private DateTime startDate;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public DateTime getStartDate() {
        return startDate;
    }

    public void setStartDate(DateTime startDate) {
        this.startDate = startDate;
    }
}
