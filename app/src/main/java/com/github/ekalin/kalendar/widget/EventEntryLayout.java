package com.github.ekalin.kalendar.widget;

import android.text.TextUtils;
import android.view.View;
import android.widget.RemoteViews;
import androidx.annotation.LayoutRes;
import androidx.annotation.StringRes;

import com.github.ekalin.kalendar.R;
import com.github.ekalin.kalendar.calendar.CalendarEventVisualizer;
import com.github.ekalin.kalendar.prefs.InstanceSettings;
import com.github.ekalin.kalendar.util.DateUtil;
import com.github.ekalin.kalendar.util.RemoteViewsUtil;

import static com.github.ekalin.kalendar.util.RemoteViewsUtil.setMultiline;
import static com.github.ekalin.kalendar.util.RemoteViewsUtil.setTextSize;
import static com.github.ekalin.kalendar.util.RemoteViewsUtil.setViewWidth;

/**
 * @author yvolk@yurivolkov.com
 */
public enum EventEntryLayout {
    DEFAULT(R.layout.event_entry, "DEFAULT", R.string.default_multiline_layout) {
        @Override
        protected void setEventDetails(CalendarEventVisualizer calendarEventVisualizer, CalendarEntry entry,
                                       RemoteViews rv) {
            String eventDetails = getEventDetails(entry);
            int viewId = R.id.event_entry_details;
            if (TextUtils.isEmpty(eventDetails)) {
                rv.setViewVisibility(viewId, View.GONE);
            } else {
                rv.setViewVisibility(viewId, View.VISIBLE);
                rv.setTextViewText(viewId, eventDetails);
                setTextSize(entry.getSettings(), rv, viewId, R.dimen.event_entry_details);
                calendarEventVisualizer.setEventTitleColor(rv, viewId, entry);
            }
        }

        private String getEventDetails(CalendarEntry entry) {
            String time = entry.getEventTimeString();
            String location = entry.getLocationString();
            String separator = TextUtils.isEmpty(time) || TextUtils.isEmpty(location) ? "" : SEPARATOR;
            return time + separator + location;
        }
    },

    ONE_LINE(R.layout.event_entry_one_line, "ONE_LINE", R.string.single_line_layout) {
        @Override
        protected String getTitleString(CalendarEntry event) {
            String title = event.getTitle();
            String locationString = event.getLocationString();
            if (TextUtils.isEmpty(locationString)) {
                return title;
            } else {
                return title + SEPARATOR + locationString;
            }
        }

        @Override
        protected void setDaysToEvent(CalendarEventVisualizer calendarEventVisualizer, CalendarEntry entry, RemoteViews rv) {
            if (entry.getSettings().getShowDayHeaders()) {
                rv.setViewVisibility(R.id.event_entry_days, View.GONE);
                rv.setViewVisibility(R.id.event_entry_days_right, View.GONE);
            } else {
                int days = entry.getDaysFromToday();
                boolean daysAsText = days >= -1 && days <= 1;
                int viewToShow = daysAsText ? R.id.event_entry_days : R.id.event_entry_days_right;
                int viewToHide = daysAsText ? R.id.event_entry_days_right : R.id.event_entry_days;
                rv.setViewVisibility(viewToHide, View.GONE);
                rv.setViewVisibility(viewToShow, View.VISIBLE);
                rv.setTextViewText(viewToShow, DateUtil.getDaysFromTodayString(entry.getSettings().getContext(), days));
                InstanceSettings settings = entry.getSettings();
                setViewWidth(settings, rv, viewToShow, daysAsText
                        ? R.dimen.days_to_event_width
                        : R.dimen.days_to_event_right_width);
                setTextSize(settings, rv, viewToShow, R.dimen.event_entry_details);
                calendarEventVisualizer.setDayHeaderColor(rv, viewToShow, entry);
            }
        }

        @Override
        protected void setEventTime(CalendarEventVisualizer calendarEventVisualizer, CalendarEntry entry, RemoteViews rv) {
            int viewId = R.id.event_entry_time;
            RemoteViewsUtil.setMultiline(rv, viewId, entry.getSettings().getShowEndTime());
            rv.setTextViewText(viewId, entry.getEventTimeString().replace(CalendarEntry
                    .SPACE_DASH_SPACE, "\n"));
            InstanceSettings settings = entry.getSettings();
            setViewWidth(settings, rv, viewId, R.dimen.event_time_width);
            setTextSize(settings, rv, viewId, R.dimen.event_entry_details);
            calendarEventVisualizer.setEventTitleColor(rv, viewId, entry);
        }
    };

    private static final String SEPARATOR = "  |  ";

    @LayoutRes
    public final int layoutId;
    public final String value;
    @StringRes
    public final int summaryResId;

    EventEntryLayout(@LayoutRes int layoutId, String value, int summaryResId) {
        this.layoutId = layoutId;
        this.value = value;
        this.summaryResId = summaryResId;
    }

    public static EventEntryLayout fromPreferenceValue(String value) {
        EventEntryLayout layout = DEFAULT;
        for (EventEntryLayout item : EventEntryLayout.values()) {
            if (item.value.equals(value)) {
                layout = item;
                break;
            }
        }
        return layout;
    }

    public void visualizeEvent(CalendarEventVisualizer calendarEventVisualizer, CalendarEntry entry, RemoteViews rv) {
        setTitle(calendarEventVisualizer, entry, rv);
        setDaysToEvent(calendarEventVisualizer, entry, rv);
        setEventTime(calendarEventVisualizer, entry, rv);
        setEventDetails(calendarEventVisualizer, entry, rv);
    }

    protected void setTitle(CalendarEventVisualizer calendarEventVisualizer, CalendarEntry event, RemoteViews rv) {
        int viewId = R.id.event_entry_title;
        rv.setTextViewText(viewId, getTitleString(event));
        setTextSize(event.getSettings(), rv, viewId, R.dimen.event_entry_title);
        calendarEventVisualizer.setEventTitleColor(rv, viewId, event);
        setMultiline(rv, viewId, event.getSettings().getTitleMultiline());
    }

    protected String getTitleString(CalendarEntry event) {
        return event.getTitle();
    }

    protected void setDaysToEvent(CalendarEventVisualizer calendarEventVisualizer, CalendarEntry entry, RemoteViews rv) {
        // Empty
    }

    protected void setEventTime(CalendarEventVisualizer calendarEventVisualizer, CalendarEntry entry, RemoteViews rv) {
        // Empty
    }

    protected void setEventDetails(CalendarEventVisualizer calendarEventVisualizer, CalendarEntry entry, RemoteViews rv) {
        // Empty
    }
}
