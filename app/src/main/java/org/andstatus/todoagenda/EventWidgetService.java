package org.andstatus.todoagenda;

import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViewsService;

public class EventWidgetService extends RemoteViewsService {
    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(this.getClass().getSimpleName(), "onCreate");
        EnvironmentChangedReceiver.registerReceivers(this);
    }

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        int widgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, 0);
        return new EventRemoteViewsFactory(getApplicationContext(), widgetId);
    }
}
