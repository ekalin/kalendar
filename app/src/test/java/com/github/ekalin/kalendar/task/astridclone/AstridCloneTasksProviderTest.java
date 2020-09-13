package com.github.ekalin.kalendar.task.astridclone;

import android.content.Context;
import android.database.MatrixCursor;
import androidx.test.core.app.ApplicationProvider;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.github.ekalin.kalendar.prefs.AllSettings;
import com.github.ekalin.kalendar.prefs.EventSource;
import com.github.ekalin.kalendar.testutil.ContentProviderForTests;

import static com.google.common.truth.Truth.assertThat;

@RunWith(RobolectricTestRunner.class)
public class AstridCloneTasksProviderTest {
    private ContentProviderForTests contentProvider;
    private AstridCloneTasksProvider tasksProvider;

    @Before
    public void setup() {
        Context context = ApplicationProvider.getApplicationContext();
        contentProvider = Robolectric.setupContentProvider(ContentProviderForTests.class,
                AstridCloneTasksContract.TaskLists.PROVIDER_URI.getAuthority());
        tasksProvider = new AstridCloneTasksProvider(context, 1, AllSettings.instanceFromId(context, 1));
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
