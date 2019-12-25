package com.github.ekalin.kalendar.widget;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import com.github.ekalin.kalendar.util.DateUtil;

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
