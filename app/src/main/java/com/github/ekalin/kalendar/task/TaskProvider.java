package com.github.ekalin.kalendar.task;

import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import androidx.core.util.Supplier;

import com.github.ekalin.kalendar.prefs.EventSource;
import com.github.ekalin.kalendar.prefs.InstanceSettings;
import com.github.ekalin.kalendar.prefs.PermissionRequester;
import com.github.ekalin.kalendar.provider.EventProvider;
import com.github.ekalin.kalendar.task.astridclone.AstridCloneTasksProvider;
import com.github.ekalin.kalendar.task.samsung.SamsungTasksProvider;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public class TaskProvider extends EventProvider {
    public static final String PROVIDER_NONE = "NONE";

    private static final String PROVIDER_ASTRID = "ASTRID_CLONE";
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

    public Optional<String> getNonInstallableReason(Context context, String taskSource) {
        AbstractTaskProvider provider = getProvider(taskSource);
        return provider.getNonInstallableReason(context);
    }

    public String getAppPackage(String taskSource) {
        AbstractTaskProvider provider = getProvider(taskSource);
        return provider.getAppPackage();
    }

    public boolean hasPermissionForSource(String taskSource) {
        AbstractTaskProvider provider = getProvider(taskSource);
        return provider.hasPermission();
    }

    public void requestPermission(PermissionRequester requester, String taskSource) {
        AbstractTaskProvider provider = getProvider(taskSource);
        provider.requestPermission(requester);
    }

    public static List<ContentObserver> registerObservers(Context context, Supplier<ContentObserver> observerCreator) {
        List<ContentObserver> observers = new ArrayList<>();
        AstridCloneTasksProvider.registerContentObserver(context, observerCreator)
                .ifPresent(observers::add);
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
        if (PROVIDER_ASTRID.equals(taskSource)) {
            return new AstridCloneTasksProvider(context, widgetId, settings);
        }
        if (PROVIDER_SAMSUNG.equals(taskSource)) {
            return new SamsungTasksProvider(context, widgetId, settings);
        }

        return new EmptyTaskProvider(context, widgetId, settings);
    }
}
