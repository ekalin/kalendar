package org.andstatus.todoagenda.task;

import android.content.Context;
import android.support.annotation.NonNull;

import org.andstatus.todoagenda.EndedSomeTimeAgo;
import org.andstatus.todoagenda.prefs.InstanceSettings;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.Test;
import org.mockito.Mockito;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.when;

public class AbstractTaskProviderTest {
    @Test
    public void getTaskDate_withoutDue_withoutStart_returnsMidnightOfToday() {
        AbstractTaskProvider taskProvider = createTaskProvider();

        DateTime taskDate = taskProvider.getTaskDate(null, null);
        assertThat(taskDate).isEqualTo(DateTime.now().withTimeAtStartOfDay());
    }

    @Test
    public void getTaskDate_withDue_withoutStart_returnsMidnightOfDueDate() {
        AbstractTaskProvider taskProvider = createTaskProvider();

        DateTime dueDate = DateTime.now().plusDays(3);
        DateTime taskDate = taskProvider.getTaskDate(dueDate.getMillis(), null);
        assertThat(taskDate).isEqualTo(dueDate.withTimeAtStartOfDay());
    }

    @Test
    public void getTaskDate_withoutDue_withStart_returnsMidnightOfStartDate() {
        AbstractTaskProvider taskProvider = createTaskProvider();

        DateTime startDate = DateTime.now().plusDays(5);
        DateTime taskDate = taskProvider.getTaskDate(null, startDate.getMillis());
        assertThat(taskDate).isEqualTo(startDate.withTimeAtStartOfDay());
    }

    @Test
    public void getTaskDate_withDue_withStart_returnsMidnightOfDueDate() {
        AbstractTaskProvider taskProvider = createTaskProvider();

        DateTime startDate = DateTime.now().plusDays(5);
        DateTime dueDate = DateTime.now().plusDays(15);
        DateTime taskDate = taskProvider.getTaskDate(dueDate.getMillis(), startDate.getMillis());
        assertThat(taskDate).isEqualTo(dueDate.withTimeAtStartOfDay());
    }

    @Test
    public void getTaskDate_withPastDue_returnsMidnightOfToday() {
        AbstractTaskProvider taskProvider = createTaskProvider();

        DateTime dueDate = DateTime.now().minusDays(1);
        DateTime taskDate = taskProvider.getTaskDate(dueDate.getMillis(), null);
        assertThat(taskDate).isEqualTo(DateTime.now().withTimeAtStartOfDay());
    }

    private AbstractTaskProvider createTaskProvider() {
        AbstractTaskProvider taskProvider = new TestTaskProvider(Mockito.mock(Context.class), 1);
        taskProvider.initialiseParameters();
        return taskProvider;
    }
}

class TestTaskProvider extends EmptyTaskProvider {
    private InstanceSettings settings = Mockito.mock(InstanceSettings.class);

    TestTaskProvider(Context context, int widgetId) {
        super(context, widgetId);
        when(settings.getTimeZone()).thenReturn(DateTimeZone.getDefault());
        when(settings.getEventsEnded()).thenReturn(EndedSomeTimeAgo.NONE);
    }

    @NonNull
    @Override
    protected InstanceSettings getSettings() {
        return settings;
    }
}
