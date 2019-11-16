package org.andstatus.todoagenda.widget;

import org.andstatus.todoagenda.util.DateUtil;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

public class DayHeader extends WidgetEntry {
    private final DateTimeZone zone;

    public DayHeader(DateTime date, DateTimeZone zone) {
        super(10);
        setStartDate(date.withTimeAtStartOfDay());
        this.zone = zone;
    }

    @Override
    public boolean isCurrent() {
        return getStartDate().equals(DateUtil.now(zone).withTimeAtStartOfDay());
    }
}
