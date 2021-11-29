package com.github.ekalin.kalendar.task;

import android.content.Context;
import android.content.Intent;

import org.joda.time.DateTime;

import com.github.ekalin.kalendar.prefs.EventSource;
import com.github.ekalin.kalendar.prefs.InstanceSettings;
import com.github.ekalin.kalendar.prefs.PermissionRequester;
import com.github.ekalin.kalendar.provider.EventProvider;
import com.github.ekalin.kalendar.util.DateUtil;
import com.github.ekalin.kalendar.util.PackageManagerUtil;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

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

    public boolean isInstalled() {
        return PackageManagerUtil.isPackageInstalled(context, getAppPackage());
    }

    public Optional<String> getNonInstallableReason(Context context) {
        return Optional.empty();
    }

    public abstract String getAppPackage();

    public abstract boolean hasPermission();

    public abstract void requestPermission(PermissionRequester requester);
}
