package com.github.ekalin.kalendar.birthday;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

public class BirthdayEvent {
    private long id;
    private String title;
    private DateTime startDate;
    private int color;
    private DateTimeZone zone;

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

    public void setStartDate(DateTime startDate) {
        this.startDate = startDate.withTimeAtStartOfDay();
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public DateTimeZone getZone() {
        return zone;
    }

    public void setZone(DateTimeZone zone) {
        this.zone = zone;
    }

    @Override
    public int hashCode() {
        int result = Long.valueOf(id).hashCode();
        result += 31 * title.hashCode();
        result += 31 * startDate.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "BirthdayEvent{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", startDate=" + startDate +
                ", color=" + color +
                '}';
    }
}
