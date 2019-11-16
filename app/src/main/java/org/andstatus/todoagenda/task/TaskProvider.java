package org.andstatus.todoagenda.task;

import android.content.Context;
import android.content.Intent;
import androidx.fragment.app.Fragment;

import org.andstatus.todoagenda.prefs.EventSource;
import org.andstatus.todoagenda.prefs.InstanceSettings;
import org.andstatus.todoagenda.provider.EventProvider;
import org.andstatus.todoagenda.task.dmfs.DmfsOpenTasksProvider;
import org.andstatus.todoagenda.task.samsung.SamsungTasksProvider;

import java.util.Collection;
import java.util.List;

public class TaskProvider extends EventProvider {
    public static final String PROVIDER_NONE = "NONE";

    private static final String PROVIDER_DMFS = "DMFS_OPEN_TASKS";
    private static final String PROVIDER_SAMSUNG = "SAMSUNG";

    public TaskProvider(Context context, int widgetId, InstanceSettings settings) {
        super(context, widgetId, settings);
    }

    public List<TaskEvent> getEvents() {
        AbstractTaskProvider provider = getProvider();
        return provider.getTasks();
    }

    public Collection<EventSource> getTaskLists(String taskSource) {
        AbstractTaskProvider provider = getProvider(taskSource);
        return provider.getTaskLists();
    }

    public boolean hasPermissionForSource(String taskSource) {
        AbstractTaskProvider provider = getProvider(taskSource);
        return provider.hasPermission();
    }

    public void requestPermission(Fragment fragment, String taskSource) {
        AbstractTaskProvider provider = getProvider(taskSource);
        provider.requestPermission(fragment);
    }

    public Intent createOpenCalendarEventIntent(TaskEvent event) {
        AbstractTaskProvider provider = getProvider();
        return provider.createViewIntent(event);
    }

    private AbstractTaskProvider getProvider() {
        String taskSource = getSettings().getTaskSource();
        return getProvider(taskSource);
    }

    // This is called from the settings activity, when the task source that the user
    // selected has not been saved to settings yet
    private AbstractTaskProvider getProvider(String taskSource) {
        if (PROVIDER_DMFS.equals(taskSource)) {
            return new DmfsOpenTasksProvider(context, widgetId, settings);
        }
        if (PROVIDER_SAMSUNG.equals(taskSource)) {
            return new SamsungTasksProvider(context, widgetId, settings);
        }

        return new EmptyTaskProvider(context, widgetId, settings);
    }
}
