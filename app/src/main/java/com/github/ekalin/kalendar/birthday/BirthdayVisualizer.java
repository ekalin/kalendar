package com.github.ekalin.kalendar.birthday;

import android.content.Context;
import android.view.View;
import android.widget.RemoteViews;

import java.util.ArrayList;
import java.util.List;

import com.github.ekalin.kalendar.R;
import com.github.ekalin.kalendar.util.DateUtil;
import com.github.ekalin.kalendar.widget.BirthdayEntry;
import com.github.ekalin.kalendar.widget.EventEntryLayout;
import com.github.ekalin.kalendar.widget.WidgetEntry;
import com.github.ekalin.kalendar.widget.WidgetEntryVisualizer;

import static com.github.ekalin.kalendar.util.RemoteViewsUtil.setBackgroundColor;
import static com.github.ekalin.kalendar.util.RemoteViewsUtil.setMultiline;
import static com.github.ekalin.kalendar.util.RemoteViewsUtil.setTextSize;
import static com.github.ekalin.kalendar.util.RemoteViewsUtil.setViewWidth;

public class BirthdayVisualizer extends WidgetEntryVisualizer<BirthdayEntry> {
    private final BirthdayProvider birthdayProvider;

    public BirthdayVisualizer(Context context, int widgetId) {
        super(context, widgetId);
        this.birthdayProvider = new BirthdayProvider(context, widgetId, getSettings());
    }

    @Override
    public RemoteViews getRemoteViews(WidgetEntry eventEntry, int position) {
        BirthdayEntry entry = (BirthdayEntry) eventEntry;
        RemoteViews rv = new RemoteViews(getContext().getPackageName(), R.layout.birthday_entry);
        setColor(entry, rv);
        setDaysToEvent(entry, rv);
        setTitle(entry, rv);
        return rv;
    }

    private void setColor(BirthdayEntry entry, RemoteViews rv) {
        setBackgroundColor(rv, R.id.birthday_entry_color, entry.getEvent().getColor());
    }

    private void setDaysToEvent(BirthdayEntry entry, RemoteViews rv) {
        if (getSettings().getEventEntryLayout() == EventEntryLayout.DEFAULT) {
            rv.setViewVisibility(R.id.birthday_one_line_days, View.GONE);
            rv.setViewVisibility(R.id.birthday_one_line_days_right, View.GONE);
            rv.setViewVisibility(R.id.birthday_one_line_spacer, View.GONE);
        } else {
            if (getSettings().getShowDayHeaders()) {
                rv.setViewVisibility(R.id.birthday_one_line_days, View.GONE);
                rv.setViewVisibility(R.id.birthday_one_line_days_right, View.GONE);
                rv.setViewVisibility(R.id.birthday_one_line_spacer, View.VISIBLE);
            } else {
                int days = entry.getDaysFromToday();
                boolean daysAsText = days >= -1 && days <= 1;
                int viewToShow = daysAsText ? R.id.birthday_one_line_days : R.id.birthday_one_line_days_right;
                int viewToHide = daysAsText ? R.id.birthday_one_line_days_right : R.id.birthday_one_line_days;
                rv.setViewVisibility(viewToHide, View.GONE);
                rv.setViewVisibility(viewToShow, View.VISIBLE);
                setViewWidth(getSettings(), rv, viewToShow, daysAsText
                        ? R.dimen.days_to_event_width
                        : R.dimen.days_to_event_right_width);
                rv.setTextViewText(viewToShow, DateUtil.getDaysFromTodayString(getSettings().getContext(), days));
                setTextSize(getSettings(), rv, viewToShow, R.dimen.event_entry_details);
                setDayHeaderColor(rv, viewToShow, entry);
            }
            setViewWidth(getSettings(), rv, R.id.birthday_one_line_spacer, R.dimen.event_time_width);
            rv.setViewVisibility(R.id.birthday_one_line_spacer, View.VISIBLE);
        }
    }

    private void setTitle(BirthdayEntry entry, RemoteViews rv) {
        int viewId = R.id.birthday_entry_title;
        rv.setTextViewText(viewId, entry.getTitle(getContext().getResources().getString(R.string.birthday_event_text)));
        setTextSize(getSettings(), rv, viewId, R.dimen.event_entry_title);
        setEventTitleColor(rv, viewId, entry);
        setMultiline(rv, viewId, getSettings().getTitleMultiline());
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public List<BirthdayEntry> getEventEntries() {
        return createEntryList(birthdayProvider.getEvents());
    }

    private List<BirthdayEntry> createEntryList(List<BirthdayEvent> events) {
        List<BirthdayEntry> entries = new ArrayList<>();
        for (BirthdayEvent event : events) {
            entries.add(BirthdayEntry.fromEvent(event));
        }
        return entries;
    }

    @Override
    public Class<? extends BirthdayEntry> getSupportedEventEntryType() {
        return BirthdayEntry.class;
    }
}
