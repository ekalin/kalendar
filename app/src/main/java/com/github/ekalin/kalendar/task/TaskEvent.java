package com.github.ekalin.kalendar.task;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import com.github.ekalin.kalendar.util.DateUtil;

public class TaskEvent {
    private long id;
    private String title;
    private DateTime startDate;
    private DateTime dueDate;
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

    public DateTime getStartDate() {
        return startDate;
    }

    public DateTime getDueDate() {
        return dueDate;
    }

    public void setDates(Long startDateMillis, Long dueDateMillis) {
        if (startDateMillis != null) {
            this.startDate = new DateTime(startDateMillis, zone);

            if (dueDateMillis != null) {
                this.dueDate = new DateTime(dueDateMillis, zone);
            } else {
                this.dueDate = this.startDate;
            }
        } else if (dueDateMillis != null) {
            this.startDate = new DateTime(dueDateMillis, zone);
            this.dueDate = this.startDate;
        } else {
            this.startDate = DateUtil.now(zone);
            this.dueDate = this.startDate.plusYears(50);
        }
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
                startDate.equals(taskEvent.startDate) &&
                dueDate.equals(taskEvent.dueDate);
    }

    @Override
    public int hashCode() {
        int result = Long.valueOf(id).hashCode();
        result += 31 * title.hashCode();
        result += 31 * startDate.hashCode();
        result += 31 * dueDate.hashCode();
        result += 31 * color;
        return result;
    }

    @Override
    public String toString() {
        return "TaskEvent{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", startDate=" + startDate +
                ", dueDate=" + dueDate +
                ", color=" + color +
                '}';
    }
}
