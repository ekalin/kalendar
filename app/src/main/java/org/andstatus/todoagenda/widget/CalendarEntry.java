package org.andstatus.todoagenda.widget;

import android.text.TextUtils;
import android.text.format.DateUtils;

import org.andstatus.todoagenda.R;
import org.andstatus.todoagenda.calendar.CalendarEvent;
import org.andstatus.todoagenda.prefs.InstanceSettings;
import org.andstatus.todoagenda.util.DateUtil;
import org.joda.time.DateTime;

public class CalendarEntry extends WidgetEntry {
    private static final String TWELVE = "12";
    private static final String AUTO = "auto";
    private static final String SPACE_ARROW = " →";
    private static final String ARROW_SPACE = "→ ";
    private static final String EMPTY_STRING = "";
    static final String SPACE_DASH_SPACE = " - ";

    private DateTime endDate;
    private boolean allDay;
    private CalendarEvent event;

    private CalendarEntry() {
        super(30);
    }

    public static CalendarEntry fromEvent(CalendarEvent event, DateTime entryDate) {
        CalendarEntry entry = new CalendarEntry();
        entry.setStartDate(entryDate);
        DateTime defaultEndDate = DateUtil.startOfNextDay(entryDate);
        if (event.getEndDate().isBefore(defaultEndDate)) {
            entry.setEndDate(event.getEndDate());
        } else {
            entry.setEndDate(defaultEndDate);
        }

        entry.allDay = event.isAllDay();
        entry.event = event;
        return entry;
    }

    public String getTitle() {
        String title = event.getTitle();
        if (TextUtils.isEmpty(title)) {
            title = getSettings().getContext().getResources().getString(R.string.no_title);
        }
        return title;
    }

    public DateTime getEndDate() {
        return endDate;
    }

    public void setEndDate(DateTime endDate) {
        this.endDate = endDate;
    }

    @Override
    public DateTime getNextUpdateTime() {
        DateTime now = DateUtil.now(event.getZone());
        if (getStartDate().isAfter(now)) {
            return getStartDate();
        } else if (getEndDate().isAfter(now)) {
            return getEndDate();
        } else {
            return null;
        }
    }

    public int getColor() {
        return event.getColor();
    }

    public boolean isAllDay() {
        return allDay;
    }

    public String getLocation() {
        return event.getLocation();
    }

    public boolean isAlarmActive() {
        return event.isAlarmActive();
    }

    public boolean isRecurring() {
        return event.isRecurring();
    }

    public boolean isPartOfMultiDayEvent() {
        return getEvent().isPartOfMultiDayEvent();
    }

    public boolean isStartOfMultiDayEvent() {
        return isPartOfMultiDayEvent() && !getEvent().getStartDate().isBefore(getStartDate());
    }

    public boolean isEndOfMultiDayEvent() {
        return isPartOfMultiDayEvent() && !getEvent().getEndDate().isAfter(getEndDate());
    }

    private boolean spansOneFullDay() {
        return getStartDate().plusDays(1).isEqual(getEvent().getEndDate());
    }

    public CalendarEvent getEvent() {
        return event;
    }

    public String getEventTimeString() {
        return hideEventTime() ? "" : createTimeSpanString();
    }

    private boolean hideEventTime() {
        return (spansOneFullDay() && !(isStartOfMultiDayEvent() || isEndOfMultiDayEvent()))
                || (isAllDay() && getSettings().getFillAllDayEvents());
    }

    private String createTimeSpanString() {
        if (isAllDay() && !getSettings().getFillAllDayEvents()) {
            DateTime dateTime = getEvent().getEndDate().minusDays(1);
            if (isEndOfMultiDayEvent()) {
                return "";
            } else {
                return ARROW_SPACE + DateUtil.createDateString(getSettings(), dateTime);
            }
        } else {
            return createTimeStringForCalendarEntry();
        }
    }

    private String createTimeStringForCalendarEntry() {
        String startStr;
        String endStr;
        String separator = SPACE_DASH_SPACE;
        if (isPartOfMultiDayEvent() && DateUtil.isMidnight(getStartDate())
                && !isStartOfMultiDayEvent()) {
            startStr = ARROW_SPACE;
            separator = EMPTY_STRING;
        } else {
            startStr = createTimeString(getStartDate());
        }
        if (getSettings().getShowEndTime()) {
            if (isPartOfMultiDayEvent() && DateUtil.isMidnight(getEndDate())
                    && !isEndOfMultiDayEvent()) {
                endStr = SPACE_ARROW;
                separator = EMPTY_STRING;
            } else {
                endStr = createTimeString(getEndDate());
            }
        } else {
            separator = EMPTY_STRING;
            endStr = EMPTY_STRING;
        }

        if (startStr.equals(endStr)) {
            return startStr;
        }

        return startStr + separator + endStr;
    }

    private String createTimeString(DateTime time) {
        return DateUtil.formatDateTime(getSettings(), time, DateUtils.FORMAT_SHOW_TIME);
    }

    public String getLocationString() {
        return hideLocation() ? "" : getLocation();
    }

    private boolean hideLocation() {
        return getLocation() == null || getLocation().isEmpty() || !getSettings().getShowLocation();
    }

    public boolean isCurrent() {
        DateTime now = DateUtil.now(event.getZone());
        return getStartDate().isBefore(now) && getEndDate().isAfter(now);
    }

    public InstanceSettings getSettings() {
        return event.getSettings();
    }

    @Override
    public String toString() {
        return "CalendarEntry ["
                + "startDate=" + getStartDate()
                + ", endDate=" + getEndDate()
                + ", allDay=" + allDay
                + ", time=" + getEventTimeString()
                + ", location=" + getLocationString()
                + ", event=" + event
                + "]";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        CalendarEntry that = (CalendarEntry) o;
        if (!event.equals(that.event) || !getStartDate().equals(that.getStartDate())) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result += 31 * event.hashCode();
        return result;
    }
}
