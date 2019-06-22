package org.andstatus.todoagenda.task;

import android.content.Context;
import android.util.Log;

import org.andstatus.todoagenda.EventProvider;
import org.andstatus.todoagenda.task.dmfs.DmfsOpenTasksProvider;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TaskProvider extends EventProvider {

    private static final String PROVIDER_NONE = "NONE";
    private static final String PROVIDER_DMFS = "DMFS_OPEN_TASKS";

    public TaskProvider(Context context, int widgetId) {
        super(context, widgetId);
    }

    public List<TaskEvent> getEvents() {
        String taskSource = getSettings().getTaskSource();
        ITaskProvider provider = getProvider(taskSource);
        if (provider != null) {
            return provider.getTasks();
        } else {
            return new ArrayList<>();
        }
    }

    private ITaskProvider getProvider(String taskSource) {
        if (PROVIDER_DMFS.equals(taskSource)) {
            return new DmfsOpenTasksProvider(context, widgetId);
        }

        return null;
    }
}
