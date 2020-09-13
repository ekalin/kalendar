package com.github.ekalin.kalendar.task.samsung;

import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CalendarContract;
import android.text.TextUtils;
import androidx.fragment.app.Fragment;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.github.ekalin.kalendar.R;
import com.github.ekalin.kalendar.prefs.EventSource;
import com.github.ekalin.kalendar.prefs.InstanceSettings;
import com.github.ekalin.kalendar.provider.QueryResult;
import com.github.ekalin.kalendar.provider.QueryResultsStorage;
import com.github.ekalin.kalendar.task.AbstractTaskProvider;
import com.github.ekalin.kalendar.task.TaskEvent;
import com.github.ekalin.kalendar.util.CalendarIntentUtil;
import com.github.ekalin.kalendar.util.PackageManagerUtil;

public class SamsungTasksProvider extends AbstractTaskProvider {
    public SamsungTasksProvider(Context context, int widgetId, InstanceSettings settings) {
        super(context, widgetId, settings);
    }

    @Override
    public List<TaskEvent> getTasks() {
        initialiseParameters();
        return queryTasks();
    }

    private List<TaskEvent> queryTasks() {
        Uri uri = SamsungTasksContract.Tasks.PROVIDER_URI;
        String[] projection = {
                SamsungTasksContract.Tasks.COLUMN_ID,
                SamsungTasksContract.Tasks.COLUMN_TITLE,
                "COALESCE(" + SamsungTasksContract.Tasks.COLUMN_START_DATE + ',' + SamsungTasksContract.Tasks.COLUMN_DUE_DATE
                        + ") as " + COLUMN_EFFECTIVE_START_DATE,
                SamsungTasksContract.Tasks.COLUMN_DUE_DATE,
                SamsungTasksContract.Tasks.COLUMN_COLOR,
                SamsungTasksContract.Tasks.COLUMN_LIST_ID,
        };
        String where = getWhereClause();

        QueryResult result = new QueryResult(getSettings(), uri, projection, where);

        List<TaskEvent> taskEvents = queryProviderAndStoreResults(uri, projection, where, result, this::createTask);
        QueryResultsStorage.storeTask(result);

        return taskEvents.stream().filter(task -> !mKeywordsFilter.matched(task.getTitle())).collect(Collectors.toList());
    }

    private String getWhereClause() {
        StringBuilder whereBuilder = new StringBuilder();
        whereBuilder.append(SamsungTasksContract.Tasks.COLUMN_COMPLETE).append(EQUALS).append("0");
        whereBuilder.append(AND).append(SamsungTasksContract.Tasks.COLUMN_DELETED).append(EQUALS).append("0");

        whereBuilder.append(AND_BRACKET)
                .append(COLUMN_EFFECTIVE_START_DATE).append(LTE).append(mEndOfTimeRange.getMillis())
                .append(OR)
                .append(COLUMN_EFFECTIVE_START_DATE).append(IS_NULL)
                .append(CLOSING_BRACKET);

        Set<String> taskLists = getSettings().getActiveTaskLists();
        if (!taskLists.isEmpty()) {
            whereBuilder.append(AND);
            whereBuilder.append(SamsungTasksContract.Tasks.COLUMN_LIST_ID);
            whereBuilder.append(" IN ( ");
            whereBuilder.append(TextUtils.join(",", taskLists));
            whereBuilder.append(CLOSING_BRACKET);
        }

        return whereBuilder.toString();
    }

    private TaskEvent createTask(Cursor cursor) {
        TaskEvent task = new TaskEvent();
        task.setId(cursor.getLong(cursor.getColumnIndex(SamsungTasksContract.Tasks.COLUMN_ID)));
        task.setTitle(cursor.getString(cursor.getColumnIndex(SamsungTasksContract.Tasks.COLUMN_TITLE)));
        task.setZone(zone);

        int startDateIdx = cursor.getColumnIndex(COLUMN_EFFECTIVE_START_DATE);
        Long startMillis = null;
        if (!cursor.isNull(startDateIdx)) {
            startMillis = cursor.getLong(startDateIdx);
        }
        int dueDateIdx = cursor.getColumnIndex(SamsungTasksContract.Tasks.COLUMN_DUE_DATE);
        Long dueMillis = null;
        if (!cursor.isNull(dueDateIdx)) {
            dueMillis = cursor.getLong(dueDateIdx);
        }
        task.setDates(startMillis, dueMillis);

        task.setColor(getColor(cursor, cursor.getColumnIndex(SamsungTasksContract.Tasks.COLUMN_COLOR),
                cursor.getInt(cursor.getColumnIndex(SamsungTasksContract.Tasks.COLUMN_LIST_ID))));

        return task;
    }

    @Override
    public Collection<EventSource> getTaskLists() {
        String taskListName = context.getResources().getString(R.string.task_prefs);

        String[] projection = {
                SamsungTasksContract.TaskLists.COLUMN_ID,
                SamsungTasksContract.TaskLists.COLUMN_NAME,
                SamsungTasksContract.TaskLists.COLUMN_COLOR,
        };
        return queryProvider(SamsungTasksContract.TaskLists.PROVIDER_URI, projection, null, cursor -> {
            int idIdx = cursor.getColumnIndex(SamsungTasksContract.TaskLists.COLUMN_ID);
            int nameIdx = cursor.getColumnIndex(SamsungTasksContract.TaskLists.COLUMN_NAME);
            int colorIdx = cursor.getColumnIndex(SamsungTasksContract.TaskLists.COLUMN_COLOR);

            int id = cursor.getInt(idIdx);
            return new EventSource(String.valueOf(id), taskListName, cursor.getString(nameIdx), getColor(cursor, colorIdx, id));
        });
    }

    private int getColor(Cursor cursor, int colorIdx, int accountId) {
        if (!cursor.isNull(colorIdx)) {
            return getAsOpaque(cursor.getInt(colorIdx));
        } else {
            int[] fixedColors = context.getResources().getIntArray(R.array.task_list_colors);
            int arrayIdx = accountId % fixedColors.length;
            return fixedColors[arrayIdx];
        }
    }

    @Override
    public Intent createViewIntent(TaskEvent event) {
        Intent intent = CalendarIntentUtil.createCalendarIntent();
        intent.setData(ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, event.getId()));
        intent.putExtra(SamsungTasksContract.INTENT_EXTRA_TASK, true);
        intent.putExtra(SamsungTasksContract.INTENT_EXTRA_SELECTED, event.getId());
        intent.putExtra(SamsungTasksContract.INTENT_EXTRA_ACTION_VIEW_FOCUS, 0);
        intent.putExtra(SamsungTasksContract.INTENT_EXTRA_DETAIL_MODE, true);
        intent.putExtra(SamsungTasksContract.INTENT_EXTRA_LAUNCH_FROM_WIDGET, true);
        return intent;
    }

    @Override
    public boolean isInstalled() {
        return PackageManagerUtil.isPackageInstalled(context, SamsungTasksContract.APP_PACKAGE);
    }

    @Override
    public Optional<String> getNonInstallableReason(Context context) {
        return Optional.of(context.getString(R.string.task_source_samsung_not_installable));
    }

    @Override
    public String getAppPackage() {
        return null;
    }

    @Override
    public boolean hasPermission() {
        return true;
    }

    @Override
    public void requestPermission(Fragment fragment) {
        // Requires just android.permission.READ_CALENDAR, which is expected to be granted already
    }
}
