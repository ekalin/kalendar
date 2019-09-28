package org.andstatus.todoagenda.testutil;

import android.appwidget.AppWidgetManager;
import android.widget.RemoteViews;

import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.shadows.ShadowAppWidgetManager;

@Implements(AppWidgetManager.class)
public class ShadowDummyAppWidgetManager extends ShadowAppWidgetManager {
    @Override
    @Implementation
    protected void updateAppWidget(int appWidgetId, RemoteViews views) {
        // Ignore this call
    }
}
