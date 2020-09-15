package com.github.ekalin.kalendar.task.astridclone;

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
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.github.ekalin.kalendar.prefs.AllSettings;
import com.github.ekalin.kalendar.prefs.EventSource;
import com.github.ekalin.kalendar.prefs.InstanceSettingsTestHelper;
import com.github.ekalin.kalendar.task.TaskEvent;
import com.github.ekalin.kalendar.testutil.ContentProviderForTests;

import static com.google.common.truth.Truth.assertThat;

@RunWith(RobolectricTestRunner.class)
public class AstridCloneTasksProviderTest {
    private Context context;
    private ContentProviderForTests contentProvider;
    private AstridCloneTasksProvider tasksProvider;

    @Before
    public void setup() {
        context = ApplicationProvider.getApplicationContext();
        contentProvider = Robolectric.setupContentProvider(ContentProviderForTests.class,
                AstridCloneTasksContract.TaskLists.PROVIDER_URI.getAuthority());
        tasksProvider = new AstridCloneTasksProvider(context, 1, AllSettings.instanceFromId(context, 1));
    }

    @Test
    public void getTasks_returnsTasks() {
        List<TaskEvent> createdTasks = setupTasks();

        List<TaskEvent> tasks = tasksProvider.getTasks();

        assertThat(tasks).isEqualTo(createdTasks);
    }

    private List<TaskEvent> setupTasks() {
        MatrixCursor matrixCursor = new MatrixCursor(new String[]{
                AstridCloneTasksContract.Tasks.COLUMN_ID,
                AstridCloneTasksContract.Tasks.COLUMN_TITLE,
                AstridCloneTasksContract.Tasks.COLUMN_START_DATE,
                AstridCloneTasksContract.Tasks.COLUMN_DUE_DATE,
                AstridCloneTasksContract.Tasks.COLUMN_COLOR_LOCAL,
                AstridCloneTasksContract.Tasks.COLUMN_COLOR_GOOGLE,
        });

        List<TaskEvent> taskEvents = createTaskEvents();
        for (TaskEvent task : taskEvents) {
            String colorColumn = task.getId() % 2 == 0
                    ? AstridCloneTasksContract.Tasks.COLUMN_COLOR_LOCAL
                    : AstridCloneTasksContract.Tasks.COLUMN_COLOR_GOOGLE;
            matrixCursor.newRow()
                    .add(AstridCloneTasksContract.Tasks.COLUMN_ID, task.getId())
                    .add(AstridCloneTasksContract.Tasks.COLUMN_TITLE, task.getTitle())
                    .add(AstridCloneTasksContract.Tasks.COLUMN_START_DATE, task.getStartDate().getMillis())
                    .add(AstridCloneTasksContract.Tasks.COLUMN_DUE_DATE, task.getDueDate().getMillis())
                    .add(colorColumn, task.getColor());
        }
        contentProvider.setQueryResult(AstridCloneTasksContract.Tasks.PROVIDER_URI, matrixCursor);

        return taskEvents;
    }

    private List<TaskEvent> createTaskEvents() {
        List<TaskEvent> tasks = new ArrayList<>();
        tasks.add(createTaskEvent(6L, "Test task 2", DateTime.now().plusDays(1), DateTime.now().plusDays(2),
                0xff000011));
        tasks.add(createTaskEvent(3L, "Test task 1", DateTime.now(), DateTime.now().plusDays(3), 0xff000022));
        tasks.add(createTaskEvent(15L, "Test task 3", DateTime.now().minusDays(1), DateTime.now().plusDays(1),
                0xff000033));
        return tasks;
    }

    private TaskEvent createTaskEvent(long id, String title, DateTime startDate, DateTime endDate, int color) {
        TaskEvent event = new TaskEvent();
        event.setId(id);
        event.setTitle(title);
        event.setZone(DateTimeZone.getDefault());
        event.setDates(startDate.getMillis(), endDate.getMillis());
        event.setColor(color);
        return event;
    }

    @Test
    public void getTasks_filtersByStateAndDate() {
        new InstanceSettingsTestHelper(context, 1).setActiveTaskLists(Collections.emptySet());

        tasksProvider.getTasks();

        String expectedQuery = String.format("%s = 0 AND \\(\\( %s != 0 AND %s <= (\\d+) \\) OR \\( %s = 0 AND %s <= \\1 \\) \\)",
                AstridCloneTasksContract.Tasks.COLUMN_COMPLETED,
                AstridCloneTasksContract.Tasks.COLUMN_START_DATE, AstridCloneTasksContract.Tasks.COLUMN_START_DATE,
                AstridCloneTasksContract.Tasks.COLUMN_START_DATE, AstridCloneTasksContract.Tasks.COLUMN_DUE_DATE);
        assertThat(contentProvider.getLastQuerySelection()).matches(expectedQuery);
    }

    @Test
    public void getTasks_withLocalTaskLists_filtersByTheirIds() {
        Set<String> taskLists = new HashSet<>();
        taskLists.add("L2");
        taskLists.add("L17");
        new InstanceSettingsTestHelper(context, 1).setActiveTaskLists(taskLists);

        tasksProvider.getTasks();

        String expectedQuery = String.format("AND (%s IN ( 2,17 ) )",
                AstridCloneTasksContract.Tasks.COLUMN_LIST_ID_LOCAL);
        assertThat(contentProvider.getLastQuerySelection()).endsWith(expectedQuery);
    }

    @Test
    public void getTasks_withGoogleTaskLists_filtersByTheirIds() {
        Set<String> taskLists = new HashSet<>();
        taskLists.add("G7");
        taskLists.add("G29");
        new InstanceSettingsTestHelper(context, 1).setActiveTaskLists(taskLists);

        tasksProvider.getTasks();

        String expectedQuery = String.format("AND (%s IN ( 7,29 ) )",
                AstridCloneTasksContract.Tasks.COLUMN_LIST_ID_GOOGLE);
        assertThat(contentProvider.getLastQuerySelection()).endsWith(expectedQuery);
    }

    @Test
    public void getTasks_withLocalAndGoogleTaskLists_filtersByTheirIds() {
        Set<String> taskLists = new HashSet<>();
        taskLists.add("L12");
        taskLists.add("G7");
        taskLists.add("G29");
        new InstanceSettingsTestHelper(context, 1).setActiveTaskLists(taskLists);

        tasksProvider.getTasks();

        String expectedQuery = String.format("AND (%s IN ( 12 ) OR %s IN ( 7,29 ) )",
                AstridCloneTasksContract.Tasks.COLUMN_LIST_ID_LOCAL,
                AstridCloneTasksContract.Tasks.COLUMN_LIST_ID_GOOGLE);
        assertThat(contentProvider.getLastQuerySelection()).endsWith(expectedQuery);
    }

    @Test
    public void getTaskLists_returnsTaskLists() {
        setupLocalTaskLists();
        setupGoogleTaskLists();

        Collection<EventSource> taskLists = tasksProvider.getTaskLists();

        assertThat(taskLists).isEqualTo(expectedTaskLists());
    }

    private void setupLocalTaskLists() {
        MatrixCursor matrixCursor = new MatrixCursor(new String[]{
                AstridCloneTasksContract.TaskLists.COLUMN_ID,
                AstridCloneTasksContract.TaskLists.COLUMN_NAME,
                AstridCloneTasksContract.TaskLists.COLUMN_ACCOUNT_NAME,
                AstridCloneTasksContract.TaskLists.COLUMN_COLOR,});
        for (EventSource taskList : createLocalTaskListsSources()) {
            matrixCursor.newRow()
                    .add(AstridCloneTasksContract.TaskLists.COLUMN_ID, taskList.getId())
                    .add(AstridCloneTasksContract.TaskLists.COLUMN_NAME, taskList.getTitle())
                    .add(AstridCloneTasksContract.TaskLists.COLUMN_ACCOUNT_NAME, taskList.getSummary())
                    .add(AstridCloneTasksContract.TaskLists.COLUMN_COLOR, taskList.getColor());
        }
        contentProvider.setQueryResult(AstridCloneTasksContract.TaskLists.PROVIDER_URI, matrixCursor);
    }

    private void setupGoogleTaskLists() {
        MatrixCursor matrixCursor = new MatrixCursor(new String[]{
                AstridCloneTasksContract.GoogleTaskLists.COLUMN_ID,
                AstridCloneTasksContract.GoogleTaskLists.COLUMN_NAME,
                AstridCloneTasksContract.GoogleTaskLists.COLUMN_ACCOUNT_NAME,
                AstridCloneTasksContract.GoogleTaskLists.COLUMN_COLOR,});
        for (EventSource taskList : createGoogleTaskListsSources()) {
            matrixCursor.newRow()
                    .add(AstridCloneTasksContract.GoogleTaskLists.COLUMN_ID, taskList.getId())
                    .add(AstridCloneTasksContract.GoogleTaskLists.COLUMN_NAME, taskList.getTitle())
                    .add(AstridCloneTasksContract.GoogleTaskLists.COLUMN_ACCOUNT_NAME, taskList.getSummary())
                    .add(AstridCloneTasksContract.GoogleTaskLists.COLUMN_COLOR, taskList.getColor());
        }
        contentProvider.setQueryResult(AstridCloneTasksContract.GoogleTaskLists.PROVIDER_URI, matrixCursor);
    }

    private Collection<EventSource> createLocalTaskListsSources() {
        List<EventSource> sources = new ArrayList<>();
        sources.add(new EventSource("2", "My Tasks", "local", 0xff000011));
        sources.add(new EventSource("4", "Bills", "local", 0xff000022));
        return sources;
    }

    private Collection<EventSource> createGoogleTaskListsSources() {
        List<EventSource> sources = new ArrayList<>();
        sources.add(new EventSource("2", "TODO", "user@gmail.com", 0xff000033));
        sources.add(new EventSource("10", "Another Google List", "user@gmail.com", 0xff000044));
        return sources;
    }

    private Collection<EventSource> expectedTaskLists() {
        Stream<EventSource> localLists = createLocalTaskListsSources().stream()
                .map(local -> new EventSource("L" + local.getId(), local.getTitle(), local.getSummary(), local.getColor()));
        Stream<EventSource> googleLists = createGoogleTaskListsSources().stream()
                .map(local -> new EventSource("G" + local.getId(), local.getTitle(), local.getSummary(), local.getColor()));
        return Stream.concat(localLists, googleLists)
                .collect(Collectors.toList());
    }
}
