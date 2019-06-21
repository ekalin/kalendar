package org.andstatus.todoagenda;

import android.content.Context;
import android.support.annotation.NonNull;
import org.andstatus.todoagenda.calendar.KeywordsFilter;
import org.andstatus.todoagenda.prefs.InstanceSettings;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

public abstract class EventProvider {
    protected final Context context;
    protected final int widgetId;

    // Below are parameters, which may change in settings
    protected DateTimeZone zone;
    protected KeywordsFilter mKeywordsFilter;
    protected DateTime mStartOfTimeRange;
    protected DateTime mEndOfTimeRange;

    public EventProvider(Context context, int widgetId) {
        this.context = context;
        this.widgetId = widgetId;
    }

    protected void initialiseParameters() {
        zone = getSettings().getTimeZone();
        mKeywordsFilter = new KeywordsFilter(getSettings().getHideBasedOnKeywords());
        mStartOfTimeRange = getSettings().getEventsEnded().endedAt(DateUtil.now(zone));
        mEndOfTimeRange = getEndOfTimeRange(DateUtil.now(zone));
    }

    private DateTime getEndOfTimeRange(DateTime now) {
        int dateRange = getSettings().getEventRange();
        return dateRange > 0
                ? now.plusDays(dateRange)
                : now.withTimeAtStartOfDay().plusDays(1);
    }

    @NonNull
    protected InstanceSettings getSettings() {
        return InstanceSettings.fromId(context, widgetId);
    }
}
