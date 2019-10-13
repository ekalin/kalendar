package org.andstatus.todoagenda.task;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

public class TaskEvent {
    private long id;
    private String title;
    private DateTime taskDate;
    private DateTimeZone zone;
    private int color;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public DateTime getTaskDate() {
        return taskDate;
    }

    public void setTaskDate(DateTime taskDate) {
        this.taskDate = taskDate;
    }

    public DateTimeZone getZone() {
        return zone;
    }

    public void setZone(DateTimeZone zone) {
        this.zone = zone;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TaskEvent taskEvent = (TaskEvent) o;
        return id == taskEvent.id &&
                color == taskEvent.color &&
                title.equals(taskEvent.title) &&
                taskDate.equals(taskEvent.taskDate);
    }

    @Override
    public int hashCode() {
        int result = Long.valueOf(id).hashCode();
        result += 31 * title.hashCode();
        result += 31 * taskDate.hashCode();
        result += 31 * color;
        return result;
    }

    @Override
    public String toString() {
        return "TaskEvent{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", taskDate=" + taskDate +
                ", color=" + color +
                '}';
    }
}
