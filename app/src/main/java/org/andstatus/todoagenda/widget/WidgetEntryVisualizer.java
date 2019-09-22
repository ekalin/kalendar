package org.andstatus.todoagenda.widget;

import android.widget.RemoteViews;

import java.util.List;

public interface WidgetEntryVisualizer<T extends WidgetEntry> {
    RemoteViews getRemoteViews(WidgetEntry eventEntry);

    int getViewTypeCount();

    List<T> getEventEntries();

    Class<? extends T> getSupportedEventEntryType();
}
