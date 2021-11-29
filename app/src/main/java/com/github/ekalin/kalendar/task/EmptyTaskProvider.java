package com.github.ekalin.kalendar.task;

import android.content.Context;
import android.content.Intent;

import com.github.ekalin.kalendar.prefs.EventSource;
import com.github.ekalin.kalendar.prefs.InstanceSettings;
import com.github.ekalin.kalendar.prefs.PermissionRequester;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class EmptyTaskProvider extends AbstractTaskProvider {
    public EmptyTaskProvider(Context context, int widgetId, InstanceSettings settings) {
        super(context, widgetId, settings);
    }

    @Override
    public List<TaskEvent> getTasks() {
        return new ArrayList<>();
    }

    @Override
    public Collection<EventSource> getTaskLists() {
        return new ArrayList<>();
    }

    @Override
    public Intent createViewIntent(TaskEvent event) {
        return null;
    }

    @Override
    public boolean hasPermission() {
        return true;
    }

    @Override
    public boolean isInstalled() {
        return true;
    }

    @Override
    public String getAppPackage() {
        return null;
    }

    @Override
    public void requestPermission(PermissionRequester permissionRequester) {
        // No action necessary
    }
}
