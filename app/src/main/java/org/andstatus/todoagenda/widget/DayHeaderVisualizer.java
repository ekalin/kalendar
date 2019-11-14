package org.andstatus.todoagenda.widget;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.widget.RemoteViews;

import org.andstatus.todoagenda.Alignment;
import org.andstatus.todoagenda.R;
import org.andstatus.todoagenda.util.DateUtil;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

import static org.andstatus.todoagenda.util.CalendarIntentUtil.createOpenCalendarAtDayIntent;
import static org.andstatus.todoagenda.util.RemoteViewsUtil.setBackgroundColor;
import static org.andstatus.todoagenda.util.RemoteViewsUtil.setPadding;
import static org.andstatus.todoagenda.util.RemoteViewsUtil.setTextSize;

public class DayHeaderVisualizer extends WidgetEntryVisualizer<DayHeader> {
    public DayHeaderVisualizer(Context context, int widgetId) {
        super(context, widgetId);
    }

    @Override
    public RemoteViews getRemoteViews(WidgetEntry eventEntry, int position) {
        DayHeader dayHeader = (DayHeader) eventEntry;

        Alignment alignment = Alignment.valueOf(getSettings().getDayHeaderAlignment());
        RemoteViews rv = new RemoteViews(getContext().getPackageName(), R.layout.day_header_separator_below);
        rv.setInt(R.id.day_header_title_wrapper, "setGravity", alignment.gravity);

        setBackgroundColor(rv, R.id.day_header,
                dayHeader.getStartDay().plusDays(1).isBefore(DateUtil.now(getSettings().getTimeZone())) ?
                        getSettings().getPastEventsBackgroundColor() : Color.TRANSPARENT);
        setDayHeaderTitle(dayHeader, position, rv);

        Intent intent = createOpenCalendarAtDayIntent(dayHeader.getStartDate());
        rv.setOnClickFillInIntent(R.id.day_header, intent);
        return rv;
    }

    private void setDayHeaderTitle(DayHeader dayHeader, int position, RemoteViews rv) {
        String dateString = DateUtil.createDayHeaderTitle(getSettings(), dayHeader.getStartDate())
                .toUpperCase(Locale.getDefault());
        rv.setTextViewText(R.id.day_header_title, dateString);
        setTextSize(getSettings(), rv, R.id.day_header_title, R.dimen.day_header_title);
        setDayHeaderColor(rv, R.id.day_header_title, dayHeader);
        setBackgroundColor(rv, R.id.day_header_separator, getSettings().getDayHeaderColor());

        int paddingTop = position == 0 ? R.dimen.day_header_padding_top_first : R.dimen.day_header_padding_top;
        setPadding(getSettings(), rv, R.id.day_header_title,
                R.dimen.day_header_padding_left, paddingTop,
                R.dimen.day_header_padding_right, R.dimen.day_header_padding_bottom);
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public List<DayHeader> getEventEntries() {
        return Collections.emptyList();
    }

    @Override
    public Class<? extends DayHeader> getSupportedEventEntryType() {
        return DayHeader.class;
    }
}
