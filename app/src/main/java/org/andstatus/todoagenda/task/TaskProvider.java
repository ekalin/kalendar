package org.andstatus.todoagenda.task;

import android.app.Activity;
import android.content.Context;

import org.andstatus.todoagenda.EventProvider;
import org.andstatus.todoagenda.task.dmfs.DmfsOpenTasksProvider;

import java.util.List;

public class TaskProvider extends EventProvider {

    private static final String PROVIDER_NONE = "NONE";
    private static final String PROVIDER_DMFS = "DMFS_OPEN_TASKS";

    public TaskProvider(Context context, int widgetId) {
        super(context, widgetId);
    }

    public List<TaskEvent> getEvents() {
        ITaskProvider provider = getProvider();
        return provider.getTasks();
    }

    public boolean hasPermission() {
        ITaskProvider provider = getProvider();
        return provider.hasPermission();
    }

    public void requestPermission(Activity activity) {
        ITaskProvider provider = getProvider();
        provider.requestPermission(activity);
    }

    private ITaskProvider getProvider() {
        String taskSource = getSettings().getTaskSource();
        if (PROVIDER_DMFS.equals(taskSource)) {
            return new DmfsOpenTasksProvider(context, widgetId);
        }

        return new EmptyTaskProvider();
    }
}
