package org.andstatus.todoagenda.widget;

import org.andstatus.todoagenda.util.DateUtil;
import org.joda.time.DateTime;
import org.joda.time.Days;

public class WidgetEntry implements Comparable<WidgetEntry> {
    private DateTime startDate;
    private int priority;

    protected WidgetEntry(int priority) {
        this.priority = priority;
    }

    public DateTime getStartDate() {
        return startDate;
    }

    public void setStartDate(DateTime startDate) {
        this.startDate = startDate;
    }

    public DateTime getStartDay() {
        return getStartDate().withTimeAtStartOfDay();
    }

    public int getDaysFromToday() {
        return Days.daysBetween(DateUtil.now(startDate.getZone()).withTimeAtStartOfDay(),
                startDate.withTimeAtStartOfDay()).getDays();
    }

    public DateTime getNextUpdateTime() {
        return null;
    }

    @Override
    public int compareTo(WidgetEntry otherEvent) {
        if (getStartDate().isAfter(otherEvent.getStartDate())) {
            return 1;
        } else if (getStartDate().isBefore(otherEvent.getStartDate())) {
            return -1;
        }
        return Integer.signum(priority - otherEvent.priority);
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + " [startDate=" + startDate + "]";
    }
}
