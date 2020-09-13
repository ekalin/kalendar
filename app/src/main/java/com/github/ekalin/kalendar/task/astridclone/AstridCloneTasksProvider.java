package com.github.ekalin.kalendar.task.astridclone;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import androidx.fragment.app.Fragment;

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
    public List<EventSource> getTaskLists() {
        List<EventSource> lists = getLocalTaskLists();
        lists.addAll(getGoogleTaskLists());
        return lists;
    }

    private List<EventSource> getLocalTaskLists() {
        return queryTaskLists(AstridCloneTasksContract.TaskLists.PROVIDER_URI,
                AstridCloneTasksContract.TaskLists.COLUMN_ID,
                "L",
                AstridCloneTasksContract.TaskLists.COLUMN_NAME,
                AstridCloneTasksContract.TaskLists.COLUMN_ACCOUNT_NAME,
                AstridCloneTasksContract.TaskLists.COLUMN_COLOR);
    }

    private List<EventSource> getGoogleTaskLists() {
        return queryTaskLists(AstridCloneTasksContract.GoogleTaskLists.PROVIDER_URI,
                AstridCloneTasksContract.GoogleTaskLists.COLUMN_ID,
                "G",
                AstridCloneTasksContract.GoogleTaskLists.COLUMN_NAME,
                AstridCloneTasksContract.GoogleTaskLists.COLUMN_ACCOUNT_NAME,
                AstridCloneTasksContract.GoogleTaskLists.COLUMN_COLOR);
    }

    private List<EventSource> queryTaskLists(Uri uri,
                                             String columnId, String idPrefix,
                                             String columnName, String columnAccountName, String columnColor) {
        String[] projection = {
                columnId,
                columnName,
                columnAccountName,
                columnColor,
        };

        return queryProvider(uri, projection, null, cursor -> {
            int idIdx = cursor.getColumnIndex(columnId);
            int nameIdx = cursor.getColumnIndex(columnName);
            int accountIdx = cursor.getColumnIndex(columnAccountName);
            int colorIdx = cursor.getColumnIndex(columnColor);

            String id = idPrefix + cursor.getInt(idIdx);
            return new EventSource(id, cursor.getString(nameIdx),
                    cursor.getString(accountIdx), cursor.getInt(colorIdx));
        });
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
