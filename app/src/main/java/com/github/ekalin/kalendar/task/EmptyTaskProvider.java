package com.github.ekalin.kalendar.task;

import android.content.Context;
import android.content.Intent;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.github.ekalin.kalendar.prefs.EventSource;
import com.github.ekalin.kalendar.prefs.InstanceSettings;

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
    public void requestPermission(Fragment fragment) {
        // No action necessary
    }
}
