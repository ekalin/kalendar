package com.github.ekalin.kalendar.widget;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.widget.RemoteViews;

import com.github.ekalin.kalendar.Alignment;
import com.github.ekalin.kalendar.R;
import com.github.ekalin.kalendar.util.DateUtil;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

import static com.github.ekalin.kalendar.util.CalendarIntentUtil.createOpenCalendarAtDayFillInIntent;
import static com.github.ekalin.kalendar.util.RemoteViewsUtil.setBackgroundColor;
import static com.github.ekalin.kalendar.util.RemoteViewsUtil.setPadding;
import static com.github.ekalin.kalendar.util.RemoteViewsUtil.setTextSize;

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

        Intent intent = createOpenCalendarAtDayFillInIntent(dayHeader.getStartDate());
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
