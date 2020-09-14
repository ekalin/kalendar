package com.github.ekalin.kalendar.task.astridclone;

import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.github.ekalin.kalendar.prefs.EventSource;
import com.github.ekalin.kalendar.prefs.InstanceSettings;
import com.github.ekalin.kalendar.provider.QueryResult;
import com.github.ekalin.kalendar.provider.QueryResultsStorage;
import com.github.ekalin.kalendar.task.AbstractTaskProvider;
import com.github.ekalin.kalendar.task.TaskEvent;
import com.github.ekalin.kalendar.util.CalendarIntentUtil;
import com.github.ekalin.kalendar.util.PermissionsUtil;

public class AstridCloneTasksProvider extends AbstractTaskProvider {
    private static final String LOCAL_TASK_LIST_PREFIX = "L";
    private static final String GOOGLE_TASK_LIST_PREFIX = "G";

    public AstridCloneTasksProvider(Context context, int widgetId, InstanceSettings settings) {
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
        Uri uri = AstridCloneTasksContract.Tasks.PROVIDER_URI;
        // Currently the projection is ignored; in practice all columns are returned
        String[] projection = {
                AstridCloneTasksContract.Tasks.COLUMN_ID,
                AstridCloneTasksContract.Tasks.COLUMN_TITLE,
                AstridCloneTasksContract.Tasks.COLUMN_START_DATE,
                AstridCloneTasksContract.Tasks.COLUMN_DUE_DATE,
                AstridCloneTasksContract.Tasks.COLUMN_COLOR_LOCAL,
                AstridCloneTasksContract.Tasks.COLUMN_COLOR_GOOGLE,
        };
        String where = getWhereClause();

        QueryResult result = new QueryResult(getSettings(), uri, projection, where);

        List<TaskEvent> taskEvents = queryProviderAndStoreResults(uri, projection, where, result, this::createTask);
        QueryResultsStorage.storeTask(result);

        return taskEvents.stream().filter(task -> !mKeywordsFilter.matched(task.getTitle())).collect(Collectors.toList());
    }

    private String getWhereClause() {
        StringBuilder where = new StringBuilder();

        where.append(AstridCloneTasksContract.Tasks.COLUMN_COMPLETED).append(EQUALS).append(0);
        filterByDate(where);
        filterByTaskList(where);

        return where.toString();
    }

    private void filterByDate(StringBuilder where) {
        // Tasks.org uses 0 in the dates column to represent no date set
        where.append(AND_BRACKET)
                .append(OPEN_BRACKET).append(AstridCloneTasksContract.Tasks.COLUMN_START_DATE).append(NOT_EQUALS).append(0)
                .append(AND).append(AstridCloneTasksContract.Tasks.COLUMN_START_DATE).append(LTE).append(mEndOfTimeRange.getMillis())
                .append(CLOSING_BRACKET)
                .append(OR)
                .append(OPEN_BRACKET).append(AstridCloneTasksContract.Tasks.COLUMN_START_DATE).append(EQUALS).append(0)
                .append(AND).append(AstridCloneTasksContract.Tasks.COLUMN_DUE_DATE).append(LTE).append(mEndOfTimeRange.getMillis())
                .append(CLOSING_BRACKET)
                .append(CLOSING_BRACKET);
    }

    private void filterByTaskList(StringBuilder where) {
        Set<String> taskLists = getSettings().getActiveTaskLists();
        if (!taskLists.isEmpty()) {
            where.append(AND_BRACKET);

            Set<String> localTaskLists = extractLocalTaskLists(taskLists);
            if (!localTaskLists.isEmpty()) {
                where.append(AstridCloneTasksContract.Tasks.COLUMN_LIST_ID_LOCAL)
                        .append(IN)
                        .append(TextUtils.join(",", localTaskLists))
                        .append(CLOSING_BRACKET);
            }

            Set<String> googleTaskLists = extractGoogleTaskLists(taskLists);
            if (!googleTaskLists.isEmpty()) {
                if (!localTaskLists.isEmpty()) {
                    where.append(OR);
                }

                where.append(AstridCloneTasksContract.Tasks.COLUMN_LIST_ID_GOOGLE)
                        .append(IN)
                        .append(TextUtils.join(",", googleTaskLists))
                        .append(CLOSING_BRACKET);
            }

            where.append(CLOSING_BRACKET);
        }
    }

    private Set<String> extractLocalTaskLists(Set<String> taskLists) {
        return extractTaskLists(taskLists, LOCAL_TASK_LIST_PREFIX);
    }

    private Set<String> extractGoogleTaskLists(Set<String> taskLists) {
        return extractTaskLists(taskLists, GOOGLE_TASK_LIST_PREFIX);
    }

    private Set<String> extractTaskLists(Set<String> taskLists, String prefix) {
        return taskLists.stream()
                .filter(id -> id.startsWith(prefix))
                .map(id -> id.substring(1))
                .collect(Collectors.toSet());
    }

    private TaskEvent createTask(Cursor cursor) {
        TaskEvent task = new TaskEvent();
        task.setId(cursor.getLong(cursor.getColumnIndex(AstridCloneTasksContract.Tasks.COLUMN_ID)));
        task.setTitle(cursor.getString(cursor.getColumnIndex(AstridCloneTasksContract.Tasks.COLUMN_TITLE)));
        task.setZone(zone);

        int startDateIdx = cursor.getColumnIndex(AstridCloneTasksContract.Tasks.COLUMN_START_DATE);
        Long startMillis = getNonZero(cursor, startDateIdx);
        int dueDateIdx = cursor.getColumnIndex(AstridCloneTasksContract.Tasks.COLUMN_DUE_DATE);
        Long dueMillis = getNonZero(cursor, dueDateIdx);
        task.setDates(startMillis, dueMillis);

        task.setColor(getAsOpaque(getColor(cursor)));

        return task;
    }

    private Long getNonZero(Cursor cursor, int column) {
        if (cursor.isNull(column)) {
            return null;
        }
        long value = cursor.getLong(column);
        return value != 0 ? value : null;
    }

    private int getColor(Cursor cursor) {
        int localColor = cursor.getColumnIndex(AstridCloneTasksContract.Tasks.COLUMN_COLOR_LOCAL);
        if (!cursor.isNull(localColor)) {
            return cursor.getInt(localColor);
        }

        int googleColor = cursor.getColumnIndex(AstridCloneTasksContract.Tasks.COLUMN_COLOR_GOOGLE);
        return cursor.getInt(googleColor);
    }


    @Override
    public List<EventSource> getTaskLists() {
        List<EventSource> lists = fetchLocalTaskLists();
        lists.addAll(fetchGoogleTaskLists());
        return lists;
    }

    private List<EventSource> fetchLocalTaskLists() {
        return queryTaskLists(AstridCloneTasksContract.TaskLists.PROVIDER_URI,
                AstridCloneTasksContract.TaskLists.COLUMN_ID,
                LOCAL_TASK_LIST_PREFIX,
                AstridCloneTasksContract.TaskLists.COLUMN_NAME,
                AstridCloneTasksContract.TaskLists.COLUMN_ACCOUNT_NAME,
                AstridCloneTasksContract.TaskLists.COLUMN_COLOR);
    }

    private List<EventSource> fetchGoogleTaskLists() {
        return queryTaskLists(AstridCloneTasksContract.GoogleTaskLists.PROVIDER_URI,
                AstridCloneTasksContract.GoogleTaskLists.COLUMN_ID,
                GOOGLE_TASK_LIST_PREFIX,
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
        Intent intent = CalendarIntentUtil.createCalendarIntent();
        intent.setData(ContentUris.withAppendedId(AstridCloneTasksContract.Tasks.VIEW_URI, event.getId()));
        return intent;
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
