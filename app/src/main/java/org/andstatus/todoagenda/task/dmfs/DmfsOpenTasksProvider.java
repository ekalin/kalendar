package org.andstatus.todoagenda.task.dmfs;

import android.content.Context;
import android.database.Cursor;

import org.andstatus.todoagenda.DateUtil;
import org.andstatus.todoagenda.EventProvider;
import org.andstatus.todoagenda.task.TaskEvent;
import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;

public class DmfsOpenTasksProvider extends EventProvider {
    private DateTime now;

    public DmfsOpenTasksProvider(Context context, int widgetId) {
        super(context, widgetId);
    }

    public List<TaskEvent> getEvents() {
        initialiseParameters();
        // Move endOfTime to end of day to include all tasks in the last day of range
        mEndOfTimeRange = mEndOfTimeRange.millisOfDay().withMaximumValue();
        now = DateUtil.now(zone);

        return queryTasks();
    }

    private List<TaskEvent> queryTasks() {
        String[] projection = {
                DmfsOpenTasksContract.COLUMN_ID,
                DmfsOpenTasksContract.COLUMN_TITLE,
                DmfsOpenTasksContract.COLUMN_DUE_DATE
        };
        String where = getWhereClause();

        Cursor cursor = context.getContentResolver().query(DmfsOpenTasksContract.PROVIDER_URI, projection,
                where, null, null);
        if (cursor == null) {
            return new ArrayList<>();
        }

        List<TaskEvent> tasks = new ArrayList<>();
        try {
            while (cursor.moveToNext()) {
                tasks.add(createTask(cursor));
            }
        } finally {
            cursor.close();
        }

        return tasks;
    }

    private String getWhereClause() {
        StringBuilder whereBuilder = new StringBuilder();
        whereBuilder.append(DmfsOpenTasksContract.COLUMN_STATUS).append(NOT_EQUALS).append(DmfsOpenTasksContract.STATUS_COMPLETED);

        whereBuilder.append(AND_BRACKET)
                .append(DmfsOpenTasksContract.COLUMN_DUE_DATE).append("<=").append(mEndOfTimeRange.getMillis())
                .append(OR)
                .append(DmfsOpenTasksContract.COLUMN_DUE_DATE).append(IS_NULL)
                .append(CLOSING_BRACKET);

        return whereBuilder.toString();
    }

    private TaskEvent createTask(Cursor cursor) {
        TaskEvent task = new TaskEvent();
        task.setId(cursor.getLong(cursor.getColumnIndex(DmfsOpenTasksContract.COLUMN_ID)));
        task.setTitle(cursor.getString(cursor.getColumnIndex(DmfsOpenTasksContract.COLUMN_TITLE)));

        int dueDateIdx = cursor.getColumnIndex(DmfsOpenTasksContract.COLUMN_DUE_DATE);
        if (cursor.isNull(dueDateIdx)) {
            task.setStartDate(now.withTimeAtStartOfDay());
        } else {
            long dueMillis = cursor.getLong(dueDateIdx);
            DateTime dueDate = new DateTime(dueMillis, zone);
            if (dueDate.isBefore(now)) {
                dueDate = now;
            }

            task.setStartDate(dueDate.withTimeAtStartOfDay());
        }

        return task;
    }
}
