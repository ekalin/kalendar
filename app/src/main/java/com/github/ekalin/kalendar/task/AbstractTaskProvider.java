package com.github.ekalin.kalendar.task;

import android.content.Context;
import android.content.Intent;
import androidx.fragment.app.Fragment;

import org.joda.time.DateTime;

import java.util.Collection;
import java.util.List;

import com.github.ekalin.kalendar.prefs.EventSource;
import com.github.ekalin.kalendar.prefs.InstanceSettings;
import com.github.ekalin.kalendar.provider.EventProvider;
import com.github.ekalin.kalendar.util.DateUtil;

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
