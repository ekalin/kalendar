package com.github.ekalin.kalendar.task.dmfs;

import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import androidx.core.util.Supplier;

import com.github.ekalin.kalendar.KalendarClickReceiver;
import com.github.ekalin.kalendar.prefs.EventSource;
import com.github.ekalin.kalendar.prefs.InstanceSettings;
import com.github.ekalin.kalendar.prefs.PermissionRequester;
import com.github.ekalin.kalendar.provider.QueryResult;
import com.github.ekalin.kalendar.provider.QueryResultsStorage;
import com.github.ekalin.kalendar.task.AbstractTaskProvider;
import com.github.ekalin.kalendar.task.TaskEvent;
import com.github.ekalin.kalendar.util.PermissionsUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class DmfsOpenTasksProvider extends AbstractTaskProvider {
    private static final String TAG = DmfsOpenTasksProvider.class.getSimpleName();

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

        QueryResult result = new QueryResult(getSettings(), QueryResult.QueryResultType.TASK, uri, projection, where);

        List<TaskEvent> taskEvents = queryProviderAndStoreResults(uri, projection, where, result, this::createTask);
        QueryResultsStorage.storeResult(result);

        return taskEvents.stream().filter(task -> !mKeywordsFilter.matched(task.getTitle())).collect(Collectors.toList());
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
            whereBuilder.append(IN);
            whereBuilder.append(TextUtils.join(",", taskLists));
            whereBuilder.append(CLOSING_BRACKET);
        }

        return whereBuilder.toString();
    }

    private TaskEvent createTask(Cursor cursor) {
        TaskEvent task = new TaskEvent();
        task.setId(cursor.getLong(cursor.getColumnIndexOrThrow(DmfsOpenTasksContract.Tasks.COLUMN_ID)));
        task.setTitle(cursor.getString(cursor.getColumnIndexOrThrow(DmfsOpenTasksContract.Tasks.COLUMN_TITLE)));
        task.setZone(zone);

        int startDateIdx = cursor.getColumnIndexOrThrow(COLUMN_EFFECTIVE_START_DATE);
        Long startMillis = null;
        if (!cursor.isNull(startDateIdx)) {
            startMillis = cursor.getLong(startDateIdx);
        }
        int dueDateIdx = cursor.getColumnIndexOrThrow(DmfsOpenTasksContract.Tasks.COLUMN_DUE_DATE);
        Long dueMillis = null;
        if (!cursor.isNull(dueDateIdx)) {
            dueMillis = cursor.getLong(dueDateIdx);
        }
        task.setDates(startMillis, dueMillis);

        task.setColor(getAsOpaque(cursor.getInt(cursor.getColumnIndexOrThrow(DmfsOpenTasksContract.Tasks.COLUMN_COLOR))));

        return task;
    }

    @Override
    public Collection<EventSource> getTaskLists() {
        String[] projection = {
                DmfsOpenTasksContract.TaskLists.COLUMN_ID,
                DmfsOpenTasksContract.TaskLists.COLUMN_NAME,
                DmfsOpenTasksContract.TaskLists.COLUMN_ACCOUNT_NAME,
                DmfsOpenTasksContract.TaskLists.COLUMN_COLOR,
        };

        return queryProvider(DmfsOpenTasksContract.TaskLists.PROVIDER_URI, projection, null, cursor -> {
            int idIdx = cursor.getColumnIndexOrThrow(DmfsOpenTasksContract.TaskLists.COLUMN_ID);
            int nameIdx = cursor.getColumnIndexOrThrow(DmfsOpenTasksContract.TaskLists.COLUMN_NAME);
            int accountIdx = cursor.getColumnIndexOrThrow(DmfsOpenTasksContract.TaskLists.COLUMN_ACCOUNT_NAME);
            int colorIdx = cursor.getColumnIndexOrThrow(DmfsOpenTasksContract.TaskLists.COLUMN_COLOR);
            return new EventSource(String.valueOf(cursor.getInt(idIdx)), cursor.getString(nameIdx),
                    cursor.getString(accountIdx), cursor.getInt(colorIdx));
        });
    }

    @Override
    public Intent createViewIntent(TaskEvent event) {
        Intent intent = new Intent();
        intent.putExtra(KalendarClickReceiver.VIEW_ENTRY_DATA,
                ContentUris.withAppendedId(DmfsOpenTasksContract.Tasks.PROVIDER_URI, event.getId()).toString());
        return intent;
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
    public void requestPermission(PermissionRequester requester) {
        requester.requestPermission(DmfsOpenTasksContract.PERMISSION);
    }

    public static Optional<ContentObserver> registerContentObserver(Context context,
                                                                    Supplier<ContentObserver> observerCreator) {
        if (hasPermission(context)) {
            ContentObserver observer = observerCreator.get();
            context.getContentResolver().registerContentObserver(DmfsOpenTasksContract.Tasks.PROVIDER_URI, false,
                    observer);
            Log.d(TAG, "Registered contentObserver");
            return Optional.of(observer);
        } else {
            return Optional.empty();
        }
    }
}
