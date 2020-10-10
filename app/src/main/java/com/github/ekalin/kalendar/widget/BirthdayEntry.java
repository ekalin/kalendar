package com.github.ekalin.kalendar.widget;

import com.github.ekalin.kalendar.birthday.BirthdayEvent;
import com.github.ekalin.kalendar.util.DateUtil;

public class BirthdayEntry extends WidgetEntry {
    private BirthdayEvent event;

    private BirthdayEntry() {
        super(30);
    }

    public static BirthdayEntry fromEvent(BirthdayEvent event) {
        BirthdayEntry entry = new BirthdayEntry();
        entry.event = event;

        entry.setStartDate(event.getDate().toDateTimeAtStartOfDay(event.getZone()));

        return entry;
    }

    public String getTitle(String template) {
        return String.format(template, event.getTitle());
    }

    public BirthdayEvent getEvent() {
        return event;
    }

    @Override
    public boolean isCurrent() {
        return getStartDate().equals(DateUtil.now(event.getZone()).withTimeAtStartOfDay());
    }

    @Override
    public String toString() {
        return "BirthdayEntry ["
                + "startDate=" + getStartDate()
                + ", event=" + event
                + "]";
    }
}
