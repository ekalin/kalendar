package org.andstatus.todoagenda.task;

import android.app.Activity;
import android.content.Context;

import org.andstatus.todoagenda.DateUtil;
import org.andstatus.todoagenda.EventProvider;
import org.andstatus.todoagenda.prefs.EventSource;
import org.joda.time.DateTime;

import java.util.Collection;
import java.util.List;

public abstract class AbstractTaskProvider extends EventProvider {

    protected DateTime now;

    public AbstractTaskProvider(Context context, int widgetId) {
        super(context, widgetId);
    }

    @Override
    protected void initialiseParameters() {
        super.initialiseParameters();
        // Move endOfTime to end of day to include all tasks in the last day of range
        mEndOfTimeRange = mEndOfTimeRange.millisOfDay().withMaximumValue();

        now = DateUtil.now(zone);
    }

    public abstract List<TaskEvent> getTasks();

    public abstract Collection<EventSource> getTaskLists();

    public abstract boolean hasPermission();

    public abstract void requestPermission(Activity activity);

    protected DateTime getTaskDate(Long dueMillis, Long startMillis) {
        DateTime dueDate;
        if (dueMillis != null) {
            dueDate = new DateTime(dueMillis, zone);
        } else {
            if (startMillis != null) {
                dueDate = new DateTime(startMillis, zone);
            } else {
                dueDate = now;
            }
        }

        if (dueDate.isBefore(now)) {
            dueDate = now;
        }

        return dueDate.withTimeAtStartOfDay();
    }
}
