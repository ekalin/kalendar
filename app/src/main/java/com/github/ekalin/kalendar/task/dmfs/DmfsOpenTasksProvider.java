package com.github.ekalin.kalendar.task.dmfs;

import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;
import androidx.core.util.Supplier;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import com.github.ekalin.kalendar.prefs.EventSource;
import com.github.ekalin.kalendar.prefs.InstanceSettings;
import com.github.ekalin.kalendar.provider.QueryResult;
import com.github.ekalin.kalendar.provider.QueryResultsStorage;
import com.github.ekalin.kalendar.task.AbstractTaskProvider;
import com.github.ekalin.kalendar.task.TaskEvent;
import com.github.ekalin.kalendar.util.CalendarIntentUtil;
import com.github.ekalin.kalendar.util.Optional;
import com.github.ekalin.kalendar.util.PackageManagerUtil;
import com.github.ekalin.kalendar.util.PermissionsUtil;

public class DmfsOpenTasksProvider extends AbstractTaskProvider {
    public DmfsOpenTasksProvider(Context context, int widgetId, InstanceSettings settings) {
        super(context, widgetId, settings);
    }

    @Override
    public List<TaskEvent> getTasks() {
        if (!hasPermission()) {
            return new ArrayList<>();
        }

        initialiseParameters();

        return queryTasks();
    }

    private List<TaskEvent> queryTasks() {
        Uri uri = DmfsOpenTasksContract.Tasks.PROVIDER_URI;
        String[] projection = {
                DmfsOpenTasksContract.Tasks.COLUMN_ID,
                DmfsOpenTasksContract.Tasks.COLUMN_TITLE,
                "COALESCE(" + DmfsOpenTasksContract.Tasks.COLUMN_START_DATE + ',' + DmfsOpenTasksContract.Tasks.COLUMN_DUE_DATE
                        + ") as " + COLUMN_EFFECTIVE_START_DATE,
                DmfsOpenTasksContract.Tasks.COLUMN_DUE_DATE,
                DmfsOpenTasksContract.Tasks.COLUMN_COLOR,
        };
        String where = getWhereClause();

        QueryResult result = new QueryResult(getSettings(), uri, projection, where, null, null);

        Cursor cursor;
        try {
            cursor = context.getContentResolver().query(uri, projection, where, null, null);
        } catch (IllegalArgumentException e) {
            cursor = null;
        }
        if (cursor == null) {
            return new ArrayList<>();
        }

        List<TaskEvent> tasks = new ArrayList<>();
        try {
            while (cursor.moveToNext()) {
                if (QueryResultsStorage.getNeedToStoreResults()) {
                    result.addRow(cursor);
                }

                TaskEvent task = createTask(cursor);
                if (!mKeywordsFilter.matched(task.getTitle())) {
                    tasks.add(task);
                }
            }
        } finally {
            cursor.close();
        }

        QueryResultsStorage.storeTask(result);

        return tasks;
    }

    private String getWhereClause() {
        StringBuilder whereBuilder = new StringBuilder();

        whereBuilder.append(DmfsOpenTasksContract.Tasks.COLUMN_STATUS).append(NOT_EQUALS).append(DmfsOpenTasksContract.Tasks.STATUS_COMPLETED)
                .append(AND).append(DmfsOpenTasksContract.Tasks.COLUMN_STATUS).append(NOT_EQUALS).append(DmfsOpenTasksContract.Tasks.STATUS_CANCELED);

        whereBuilder.append(AND_BRACKET)
                .append(COLUMN_EFFECTIVE_START_DATE).append(LTE).append(mEndOfTimeRange.getMillis())
                .append(OR)
                .append(COLUMN_EFFECTIVE_START_DATE).append(IS_NULL)
                .append(CLOSING_BRACKET);

        Set<String> taskLists = getSettings().getActiveTaskLists();
        if (!taskLists.isEmpty()) {
            whereBuilder.append(AND);
            whereBuilder.append(DmfsOpenTasksContract.Tasks.COLUMN_LIST_ID);
            whereBuilder.append(" IN ( ");
            whereBuilder.append(TextUtils.join(",", taskLists));
            whereBuilder.append(CLOSING_BRACKET);
        }

        return whereBuilder.toString();
    }

    private TaskEvent createTask(Cursor cursor) {
        TaskEvent task = new TaskEvent();
        task.setId(cursor.getLong(cursor.getColumnIndex(DmfsOpenTasksContract.Tasks.COLUMN_ID)));
        task.setTitle(cursor.getString(cursor.getColumnIndex(DmfsOpenTasksContract.Tasks.COLUMN_TITLE)));
        task.setZone(zone);

        int startDateIdx = cursor.getColumnIndex(COLUMN_EFFECTIVE_START_DATE);
        Long startMillis = null;
        if (!cursor.isNull(startDateIdx)) {
            startMillis = cursor.getLong(startDateIdx);
        }
        int dueDateIdx = cursor.getColumnIndex(DmfsOpenTasksContract.Tasks.COLUMN_DUE_DATE);
        Long dueMillis = null;
        if (!cursor.isNull(dueDateIdx)) {
            dueMillis = cursor.getLong(dueDateIdx);
        }
        task.setDates(startMillis, dueMillis);

        task.setColor(getAsOpaque(cursor.getInt(cursor.getColumnIndex(DmfsOpenTasksContract.Tasks.COLUMN_COLOR))));

        return task;
    }

    @Override
    public Collection<EventSource> getTaskLists() {
        ArrayList<EventSource> eventSources = new ArrayList<>();

        String[] projection = {
                DmfsOpenTasksContract.TaskLists.COLUMN_ID,
                DmfsOpenTasksContract.TaskLists.COLUMN_NAME,
                DmfsOpenTasksContract.TaskLists.COLUMN_COLOR,
                DmfsOpenTasksContract.TaskLists.COLUMN_ACCOUNT_NAME,
        };
        Cursor cursor;
        try {
            cursor = context.getContentResolver().query(DmfsOpenTasksContract.TaskLists.PROVIDER_URI, projection,
                    null, null, null);
        } catch (IllegalArgumentException e) {
            cursor = null;
        }
        if (cursor == null) {
            return eventSources;
        }

        int idIdx = cursor.getColumnIndex(DmfsOpenTasksContract.TaskLists.COLUMN_ID);
        int nameIdx = cursor.getColumnIndex(DmfsOpenTasksContract.TaskLists.COLUMN_NAME);
        int colorIdx = cursor.getColumnIndex(DmfsOpenTasksContract.TaskLists.COLUMN_COLOR);
        int accountIdx = cursor.getColumnIndex(DmfsOpenTasksContract.TaskLists.COLUMN_ACCOUNT_NAME);
        try {
            while (cursor.moveToNext()) {
                EventSource eventSource = new EventSource(cursor.getInt(idIdx), cursor.getString(nameIdx),
                        cursor.getString(accountIdx), cursor.getInt(colorIdx));
                eventSources.add(eventSource);
            }
        } finally {
            cursor.close();
        }

        return eventSources;
    }

    @Override
    public Intent createViewIntent(TaskEvent event) {
        Intent intent = CalendarIntentUtil.createCalendarIntent();
        intent.setData(ContentUris.withAppendedId(DmfsOpenTasksContract.Tasks.PROVIDER_URI, event.getId()));
        return intent;
    }

    @Override
    public boolean isInstalled() {
        return PackageManagerUtil.isPackageInstalled(context, DmfsOpenTasksContract.APP_PACKAGE);
    }

    @Override
    public String getAppPackage() {
        return DmfsOpenTasksContract.APP_PACKAGE;
    }

    @Override
    public boolean hasPermission() {
        return hasPermission(context);
    }

    private static boolean hasPermission(Context context) {
        return PermissionsUtil.isPermissionGranted(context, DmfsOpenTasksContract.PERMISSION);
    }

    @Override
    public void requestPermission(Fragment fragment) {
        fragment.requestPermissions(new String[]{DmfsOpenTasksContract.PERMISSION}, 1);
    }

    public static Optional<ContentObserver> registerContentObserver(Context context,
                                                                    Supplier<ContentObserver> observerCreator) {
        if (hasPermission(context)) {
            ContentObserver observer = observerCreator.get();
            context.getContentResolver().registerContentObserver(DmfsOpenTasksContract.Tasks.PROVIDER_URI, false,
                    observer);
            return Optional.of(observer);
        } else {
            return Optional.empty();
        }
    }
}
