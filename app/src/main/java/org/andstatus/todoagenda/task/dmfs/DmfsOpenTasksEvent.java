package org.andstatus.todoagenda.task.dmfs;

import android.content.ContentUris;
import android.content.Intent;

import org.andstatus.todoagenda.task.TaskEvent;
import org.andstatus.todoagenda.util.CalendarIntentUtil;

public class DmfsOpenTasksEvent extends TaskEvent {
    @Override
    public Intent createOpenCalendarEventIntent() {
        Intent intent = CalendarIntentUtil.createCalendarIntent();
        intent.setData(ContentUris.withAppendedId(DmfsOpenTasksContract.Tasks.PROVIDER_URI, getId()));
        return intent;
    }

    @Override
    public String toString() {
        return "DmfsOpenTasksEvent{" + super.toString() + '}';
    }
}
