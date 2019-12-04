package org.andstatus.todoagenda.task;

import android.content.Context;
import android.content.Intent;
import androidx.fragment.app.Fragment;

import org.andstatus.todoagenda.prefs.EventSource;
import org.andstatus.todoagenda.prefs.InstanceSettings;
import org.andstatus.todoagenda.provider.EventProvider;
import org.andstatus.todoagenda.util.DateUtil;
import org.joda.time.DateTime;

import java.util.Collection;
import java.util.List;

public abstract class AbstractTaskProvider extends EventProvider {
    protected static final String COLUMN_EFFECTIVE_START_DATE = "EFFECTIVE_START_DATE";

    protected DateTime now;

    public AbstractTaskProvider(Context context, int widgetId, InstanceSettings settings) {
        super(context, widgetId, settings);
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

    public abstract Intent createViewIntent(TaskEvent event);

    public abstract boolean hasPermission();

    public abstract void requestPermission(Fragment fragment);
}
