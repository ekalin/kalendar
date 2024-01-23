package com.github.ekalin.kalendar.widget;

import android.widget.RemoteViews;
import androidx.annotation.StringRes;

import com.github.ekalin.kalendar.KalendarClickReceiver;
import com.github.ekalin.kalendar.R;
import com.github.ekalin.kalendar.prefs.InstanceSettings;

import static com.github.ekalin.kalendar.util.RemoteViewsUtil.setBackgroundColor;
import static com.github.ekalin.kalendar.util.RemoteViewsUtil.setTextSize;

public class EmptyListMessageVisualizer {
    private final InstanceSettings settings;

    public EmptyListMessageVisualizer(InstanceSettings settings) {
        this.settings = settings;
    }

    public RemoteViews getView(Type type) {
        RemoteViews rv = new RemoteViews(settings.getContext().getPackageName(), R.layout.item_empty_list);

        rv.setTextViewText(R.id.event_entry, settings.getContext().getString(type.message));
        setTextSize(settings, rv, R.id.event_entry, R.dimen.event_entry_title);

        rv.setTextColor(R.id.event_entry, settings.getEventColor());
        setBackgroundColor(rv, R.id.event_entry, settings.getBackgroundColor());

        rv.setOnClickPendingIntent(R.id.event_entry,
                KalendarClickReceiver.createImmutablePendingIntentForAction(type.action, settings));

        return rv;
    }

    public enum Type {
        EMPTY(R.string.no_events_to_show, KalendarClickReceiver.KalendarAction.ADD_CALENDAR_EVENT),
        NO_PERMISSIONS(R.string.grant_permissions_verbose, KalendarClickReceiver.KalendarAction.CONFIGURE);

        @StringRes
        private final int message;
        private final KalendarClickReceiver.KalendarAction action;

        Type(@StringRes int message, KalendarClickReceiver.KalendarAction action) {
            this.message = message;
            this.action = action;
        }
    }
}
