package org.andstatus.todoagenda.task.dmfs;

import android.content.Context;
import android.database.MatrixCursor;
import androidx.test.core.app.ApplicationProvider;

import org.andstatus.todoagenda.prefs.AllSettings;
import org.andstatus.todoagenda.prefs.EventSource;
import org.andstatus.todoagenda.task.TaskEvent;
import org.andstatus.todoagenda.testutil.ContentProviderForTests;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static com.google.common.truth.Truth.assertThat;

@RunWith(RobolectricTestRunner.class)
public class DmfsOpenTasksProviderTest {
    private static final String COLUMN_START_DATE = "EFFECTIVE_START_DATE";

    private ContentProviderForTests contentProvider;
    private DmfsOpenTasksProvider tasksProvider;

    @Before
    public void setup() {
        Context context = ApplicationProvider.getApplicationContext();
        contentProvider = Robolectric.setupContentProvider(ContentProviderForTests.class,
                DmfsOpenTasksContract.TaskLists.PROVIDER_URI.getAuthority());
        tasksProvider = new DmfsOpenTasksProvider(context, 1, AllSettings.instanceFromId(context, 1));
    }

    @Test
    public void getTasks_returnsTasks() {
        setupTasks();

        List<TaskEvent> tasks = tasksProvider.getTasks();

        assertThat(tasks).isEqualTo(expectedEvents());
    }

    private void setupTasks() {
        MatrixCursor matrixCursor = new MatrixCursor(new String[]{
                DmfsOpenTasksContract.Tasks.COLUMN_ID,
                DmfsOpenTasksContract.Tasks.COLUMN_TITLE,
                COLUMN_START_DATE,
                DmfsOpenTasksContract.Tasks.COLUMN_DUE_DATE,
                DmfsOpenTasksContract.Tasks.COLUMN_COLOR});
        for (TaskEvent task : createTaskEvents()) {
            matrixCursor.newRow()
                    .add(DmfsOpenTasksContract.Tasks.COLUMN_ID, task.getId())
                    .add(DmfsOpenTasksContract.Tasks.COLUMN_TITLE, task.getTitle())
                    .add(COLUMN_START_DATE, task.getTaskDate().getMillis())
                    .add(DmfsOpenTasksContract.Tasks.COLUMN_DUE_DATE, task.getTaskDate().getMillis())
                    .add(DmfsOpenTasksContract.Tasks.COLUMN_COLOR, task.getColor());
        }
        contentProvider.setQueryResult(matrixCursor);
    }

    private List<TaskEvent> createTaskEvents() {
        List<TaskEvent> tasks = new ArrayList<>();
        tasks.add(createTaskEvent(6L, "Test task 2", DateTime.now().plusDays(2), 0xff000011));
        tasks.add(createTaskEvent(3L, "Test task 1", DateTime.now().plusDays(3), 0xff000022));
        tasks.add(createTaskEvent(15L, "Test task 3", DateTime.now().plusDays(1), 0xff000033));
        return tasks;
    }

    // Time is not considered for tasks. There's a test just for how dates are set.
    private List<TaskEvent> expectedEvents() {
        List<TaskEvent> taskEvents = createTaskEvents();
        for (TaskEvent taskEvent : taskEvents) {
            taskEvent.setTaskDate(taskEvent.getTaskDate().withTimeAtStartOfDay());
        }
        return taskEvents;
    }

    private TaskEvent createTaskEvent(long id, String title, DateTime taskDate, int color) {
        TaskEvent event = new TaskEvent();
        event.setId(id);
        event.setTitle(title);
        event.setTaskDate(taskDate);
        event.setColor(color);
        return event;
    }

    @Test
    public void getTaskLists_returnsTaskLists() {
        setupTaskLists();

        Collection<EventSource> taskLists = tasksProvider.getTaskLists();

        assertThat(taskLists).isEqualTo(createTaskListsSources());
    }

    private void setupTaskLists() {
        MatrixCursor matrixCursor = new MatrixCursor(new String[]{
                DmfsOpenTasksContract.TaskLists.COLUMN_ID,
                DmfsOpenTasksContract.TaskLists.COLUMN_NAME,
                DmfsOpenTasksContract.TaskLists.COLUMN_COLOR,
                DmfsOpenTasksContract.TaskLists.COLUMN_ACCOUNT_NAME});
        for (EventSource taskList : createTaskListsSources()) {
            matrixCursor.newRow()
                    .add(DmfsOpenTasksContract.TaskLists.COLUMN_ID, taskList.getId())
                    .add(DmfsOpenTasksContract.TaskLists.COLUMN_NAME, taskList.getTitle())
                    .add(DmfsOpenTasksContract.TaskLists.COLUMN_ACCOUNT_NAME, taskList.getSummary())
                    .add(DmfsOpenTasksContract.TaskLists.COLUMN_COLOR, taskList.getColor());
        }
        contentProvider.setQueryResult(matrixCursor);
    }

    private Collection<EventSource> createTaskListsSources() {
        List<EventSource> sources = new ArrayList<>();
        sources.add(new EventSource(2, "My Tasks", "Local account", 0xff000011));
        sources.add(new EventSource(4, "Bills", "remote@account.org", 0xff000022));
        return sources;
    }
}
