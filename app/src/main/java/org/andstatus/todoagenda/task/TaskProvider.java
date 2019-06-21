package org.andstatus.todoagenda.task;

import android.content.Context;
import org.andstatus.todoagenda.task.dmfs.DmfsOpenTasksProvider;

import java.util.List;

public class TaskProvider {
    public List<TaskEvent> getEvents(Context context, int widgetId) {
        return new DmfsOpenTasksProvider(context, widgetId).getEvents();
    }
}
