package com.github.ekalin.kalendar.task.samsung;

import android.content.Context;
import android.database.MatrixCursor;
import androidx.test.core.app.ApplicationProvider;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.github.ekalin.kalendar.prefs.AllSettings;
import com.github.ekalin.kalendar.prefs.EventSource;
import com.github.ekalin.kalendar.task.TaskEvent;
import com.github.ekalin.kalendar.testutil.ContentProviderForTests;

import static com.google.common.truth.Truth.assertThat;

@RunWith(RobolectricTestRunner.class)
public class SamsungTasksProviderTest {
    private static final String COLUMN_START_DATE = "EFFECTIVE_START_DATE";

    private ContentProviderForTests contentProvider;
    private SamsungTasksProvider tasksProvider;

    @Before
    public void setup() {
        Context context = ApplicationProvider.getApplicationContext();
        contentProvider = Robolectric.setupContentProvider(ContentProviderForTests.class,
                SamsungTasksContract.TaskLists.PROVIDER_URI.getAuthority());
        tasksProvider = new SamsungTasksProvider(context, 1, AllSettings.instanceFromId(context, 1));
    }

    @Test
    public void getTasks_returnsTasks() {
        List<TaskEvent> createdTasks = setupTasks();

        List<TaskEvent> tasks = tasksProvider.getTasks();

        assertThat(tasks).isEqualTo(createdTasks);
    }

    private List<TaskEvent> setupTasks() {
        MatrixCursor matrixCursor = new MatrixCursor(new String[]{
                SamsungTasksContract.Tasks.COLUMN_ID,
                SamsungTasksContract.Tasks.COLUMN_TITLE,
                COLUMN_START_DATE,
                SamsungTasksContract.Tasks.COLUMN_DUE_DATE,
                SamsungTasksContract.Tasks.COLUMN_COLOR,
                SamsungTasksContract.Tasks.COLUMN_LIST_ID});

        List<TaskEvent> taskEvents = createTaskEvents();
        for (TaskEvent task : taskEvents) {
            matrixCursor.newRow()
                    .add(SamsungTasksContract.Tasks.COLUMN_ID, task.getId())
                    .add(SamsungTasksContract.Tasks.COLUMN_TITLE, task.getTitle())
                    .add(COLUMN_START_DATE, task.getStartDate().getMillis())
                    .add(SamsungTasksContract.Tasks.COLUMN_DUE_DATE, task.getDueDate().getMillis())
                    .add(SamsungTasksContract.Tasks.COLUMN_COLOR, task.getColor())
                    .add(SamsungTasksContract.Tasks.COLUMN_LIST_ID, 1);
        }
        contentProvider.setQueryResult(SamsungTasksContract.Tasks.PROVIDER_URI, matrixCursor);

        return taskEvents;
    }

    private List<TaskEvent> createTaskEvents() {
        List<TaskEvent> tasks = new ArrayList<>();
        tasks.add(createTaskEvent(6L, "Test task 2", DateTime.now().plusDays(1), DateTime.now().plusDays(2),
                0xff000011));
        tasks.add(createTaskEvent(3L, "Test task 1", DateTime.now().minusDays(10), DateTime.now().plusDays(3),
                0xff000022));
        tasks.add(createTaskEvent(15L, "Test task 3", DateTime.now(), DateTime.now().plusDays(1), 0xff000033));
        return tasks;
    }

    private TaskEvent createTaskEvent(long id, String title, DateTime startDate, DateTime dueDate, int color) {
        TaskEvent event = new TaskEvent();
        event.setId(id);
        event.setTitle(title);
        event.setZone(DateTimeZone.getDefault());
        event.setDates(startDate.getMillis(), dueDate.getMillis());
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
                SamsungTasksContract.TaskLists.COLUMN_ID,
                SamsungTasksContract.TaskLists.COLUMN_NAME,
                SamsungTasksContract.TaskLists.COLUMN_COLOR});
        for (EventSource taskList : createTaskListsSources()) {
            matrixCursor.newRow()
                    .add(SamsungTasksContract.TaskLists.COLUMN_ID, taskList.getId())
                    .add(SamsungTasksContract.TaskLists.COLUMN_NAME, taskList.getSummary())
                    .add(SamsungTasksContract.TaskLists.COLUMN_COLOR, taskList.getColor());
        }
        contentProvider.setQueryResult(SamsungTasksContract.TaskLists.PROVIDER_URI, matrixCursor);
    }

    private Collection<EventSource> createTaskListsSources() {
        List<EventSource> sources = new ArrayList<>();
        sources.add(new EventSource("2", "Tasks", "Local account", 0xff000011));
        sources.add(new EventSource("4", "Tasks", "remote@account.org", 0xff000022));
        return sources;
    }
}
