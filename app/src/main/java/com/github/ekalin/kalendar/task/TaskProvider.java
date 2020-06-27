package com.github.ekalin.kalendar.task;

import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import androidx.core.util.Supplier;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.github.ekalin.kalendar.prefs.EventSource;
import com.github.ekalin.kalendar.prefs.InstanceSettings;
import com.github.ekalin.kalendar.provider.EventProvider;
import com.github.ekalin.kalendar.task.dmfs.DmfsOpenTasksProvider;
import com.github.ekalin.kalendar.task.samsung.SamsungTasksProvider;
import com.github.ekalin.kalendar.util.Optional;

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

    public boolean isInstalled(String taskSource) {
        AbstractTaskProvider provider = getProvider(taskSource);
        return provider.isInstalled();
    }

    public boolean hasPermissionForSource(String taskSource) {
        AbstractTaskProvider provider = getProvider(taskSource);
        return provider.hasPermission();
    }

    public void requestPermission(Fragment fragment, String taskSource) {
        AbstractTaskProvider provider = getProvider(taskSource);
        provider.requestPermission(fragment);
    }

    public static List<ContentObserver> registerObservers(Context context, Supplier<ContentObserver> observerCreator) {
        List<ContentObserver> observers = new ArrayList<>();
        Optional<ContentObserver> opObserver = DmfsOpenTasksProvider.registerContentObserver(context,
                observerCreator);
        opObserver.ifPresent(observers::add);
        return observers;
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
