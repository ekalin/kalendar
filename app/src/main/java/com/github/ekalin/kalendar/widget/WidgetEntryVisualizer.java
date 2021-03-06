package com.github.ekalin.kalendar.widget;

import android.content.Context;
import android.widget.RemoteViews;

import java.util.List;

import com.github.ekalin.kalendar.prefs.AllSettings;
import com.github.ekalin.kalendar.prefs.InstanceSettings;

public abstract class WidgetEntryVisualizer<T extends WidgetEntry> {
    private final Context context;
    private final InstanceSettings settings;

    public WidgetEntryVisualizer(Context context, int widgetId) {
        this.context = context;
        this.settings = AllSettings.instanceFromId(context, widgetId);
    }

    protected Context getContext() {
        return context;
    }

    protected InstanceSettings getSettings() {
        return settings;
    }

    public abstract RemoteViews getRemoteViews(WidgetEntry eventEntry, int position);

    protected void setEventTitleColor(RemoteViews rv, int viewId, WidgetEntry entry) {
        if (entry.isCurrent()) {
            rv.setTextColor(viewId, getSettings().getCurrentEventColor());
        } else {
            rv.setTextColor(viewId, getSettings().getEventColor());
        }
    }

    protected void setDayHeaderColor(RemoteViews rv, int viewId, WidgetEntry entry) {
        if (entry.isCurrent()) {
            rv.setTextColor(viewId, getSettings().getCurrentEventColor());
        } else {
            rv.setTextColor(viewId, getSettings().getDayHeaderColor());
        }
    }

    public abstract int getViewTypeCount();

    public abstract List<T> getEventEntries();

    public abstract Class<? extends T> getSupportedEventEntryType();
}
