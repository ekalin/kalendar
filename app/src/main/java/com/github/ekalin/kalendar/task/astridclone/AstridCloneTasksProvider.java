package com.github.ekalin.kalendar.task.astridclone;

import android.content.Context;
import android.content.Intent;
import androidx.fragment.app.Fragment;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.github.ekalin.kalendar.prefs.EventSource;
import com.github.ekalin.kalendar.prefs.InstanceSettings;
import com.github.ekalin.kalendar.task.AbstractTaskProvider;
import com.github.ekalin.kalendar.task.TaskEvent;
import com.github.ekalin.kalendar.util.PermissionsUtil;

public class AstridCloneTasksProvider extends AbstractTaskProvider {
    public AstridCloneTasksProvider(Context context, int widgetId, InstanceSettings settings) {
        super(context, widgetId, settings);
    }

    @Override
    public List<TaskEvent> getTasks() {
        return Collections.emptyList();
    }

    @Override
    public Collection<EventSource> getTaskLists() {
        return Collections.emptyList();
    }

    @Override
    public Intent createViewIntent(TaskEvent event) {
        return null;
    }

    @Override
    public String getAppPackage() {
        return AstridCloneTasksContract.APP_PACKAGE;
    }

    @Override
    public boolean hasPermission() {
        return hasPermission(context);
    }

    private static boolean hasPermission(Context context) {
        return PermissionsUtil.isPermissionGranted(context, AstridCloneTasksContract.PERMISSION);
    }

    @Override
    public void requestPermission(Fragment fragment) {
        fragment.requestPermissions(new String[]{AstridCloneTasksContract.PERMISSION}, 1);
    }
}
