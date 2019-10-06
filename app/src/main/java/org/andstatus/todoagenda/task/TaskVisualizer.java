package org.andstatus.todoagenda.task;

import android.content.Context;
import android.view.View;
import android.widget.RemoteViews;

import org.andstatus.todoagenda.R;
import org.andstatus.todoagenda.prefs.AllSettings;
import org.andstatus.todoagenda.prefs.InstanceSettings;
import org.andstatus.todoagenda.util.DateUtil;
import org.andstatus.todoagenda.widget.EventEntryLayout;
import org.andstatus.todoagenda.widget.TaskEntry;
import org.andstatus.todoagenda.widget.WidgetEntry;
import org.andstatus.todoagenda.widget.WidgetEntryVisualizer;

import java.util.ArrayList;
import java.util.List;

import static org.andstatus.todoagenda.util.RemoteViewsUtil.setMultiline;
import static org.andstatus.todoagenda.util.RemoteViewsUtil.setTextColorFromAttr;
import static org.andstatus.todoagenda.util.RemoteViewsUtil.setTextSize;
import static org.andstatus.todoagenda.util.RemoteViewsUtil.setViewWidth;

public class TaskVisualizer implements WidgetEntryVisualizer<TaskEntry> {
    private final Context context;
    private final int widgetId;
    private final TaskProvider taskProvider;

    public TaskVisualizer(Context context, int widgetId) {
        this.context = context;
        this.widgetId = widgetId;
        this.taskProvider = new TaskProvider(context, widgetId);
    }

    @Override
    public RemoteViews getRemoteViews(WidgetEntry eventEntry, int position) {
        TaskEntry entry = (TaskEntry) eventEntry;
        RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.task_entry);
        setColor(entry, rv);
        setDaysToEvent(entry, rv);
        setTitle(entry, rv);
        rv.setOnClickFillInIntent(R.id.task_entry, taskProvider.createOpenCalendarEventIntent(entry.getEvent()));
        return rv;
    }

    private void setColor(TaskEntry entry, RemoteViews rv) {
        rv.setTextColor(R.id.task_entry_icon, entry.getEvent().getColor());
    }

    private void setDaysToEvent(TaskEntry entry, RemoteViews rv) {
        if (getSettings().getEventEntryLayout() == EventEntryLayout.DEFAULT) {
            rv.setViewVisibility(R.id.task_one_line_days, View.GONE);
            rv.setViewVisibility(R.id.task_one_line_days_right, View.GONE);
            rv.setViewVisibility(R.id.task_one_line_spacer, View.GONE);
        } else {
            if (getSettings().getShowDayHeaders()) {
                rv.setViewVisibility(R.id.task_one_line_days, View.GONE);
                rv.setViewVisibility(R.id.task_one_line_days_right, View.GONE);
                rv.setViewVisibility(R.id.task_one_line_spacer, View.VISIBLE);
            } else {
                int days = entry.getDaysFromToday();
                boolean daysAsText = days >= -1 && days <= 1;
                int viewToShow = daysAsText ? R.id.task_one_line_days : R.id.task_one_line_days_right;
                int viewToHide = daysAsText ? R.id.task_one_line_days_right : R.id.task_one_line_days;
                rv.setViewVisibility(viewToHide, View.GONE);
                rv.setViewVisibility(viewToShow, View.VISIBLE);
                setViewWidth(getSettings(), rv, viewToShow, daysAsText
                        ? R.dimen.days_to_event_width
                        : R.dimen.days_to_event_right_width);
                rv.setTextViewText(viewToShow, DateUtil.getDaysFromTodayString(getSettings().getContext(), days));
                setTextSize(getSettings(), rv, viewToShow, R.dimen.event_entry_details);
                setTextColorFromAttr(getSettings().getEntryThemeContext(), rv, viewToShow, R.attr.dayHeaderTitle);
            }
            setViewWidth(getSettings(), rv, R.id.task_one_line_spacer, R.dimen.event_time_width);
            rv.setViewVisibility(R.id.task_one_line_spacer, View.VISIBLE);
        }
    }

    private void setTitle(TaskEntry entry, RemoteViews rv) {
        int viewId = R.id.task_entry_title;
        rv.setTextViewText(viewId, entry.getTitle());
        setTextSize(getSettings(), rv, R.id.task_entry_icon, R.dimen.event_entry_title);
        setTextSize(getSettings(), rv, viewId, R.dimen.event_entry_title);
        setTextColorFromAttr(getSettings().getEntryThemeContext(), rv, viewId, R.attr.eventEntryTitle);
        setMultiline(rv, viewId, getSettings().isTitleMultiline());
    }

    public InstanceSettings getSettings() {
        return AllSettings.instanceFromId(context, widgetId);
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public List<TaskEntry> getEventEntries() {
        return createEntryList(taskProvider.getEvents());
    }

    private List<TaskEntry> createEntryList(List<TaskEvent> events) {
        List<TaskEntry> entries = new ArrayList<>();
        for (TaskEvent event : events) {
            entries.add(TaskEntry.fromEvent(event));
        }
        return entries;
    }

    @Override
    public Class<? extends TaskEntry> getSupportedEventEntryType() {
        return TaskEntry.class;
    }
}
