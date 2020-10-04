package com.github.ekalin.kalendar.birthday;

import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;

public class BirthdayEvent {
    private long id;
    private String title;
    private LocalDate date;
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

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
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
        result += 31 * date.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "BirthdayEvent{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", startDate=" + date +
                ", color=" + color +
                '}';
    }
}
