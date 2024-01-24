package com.github.ekalin.kalendar;

import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.widget.RemoteViewsService;

public class KalendarRemoteViewsService extends RemoteViewsService {
    @Override
    public void onCreate() {
        super.onCreate();
        KalendarUpdater.registerReceivers(this, false);
    }

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        int widgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, 0);
        return new KalendarRemoteViewsFactory(getApplicationContext(), widgetId);
    }
}
