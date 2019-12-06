package org.andstatus.todoagenda;

import android.content.Context;
import android.database.MatrixCursor;
import android.provider.CalendarContract;
import androidx.test.core.app.ApplicationProvider;

import org.andstatus.todoagenda.prefs.InstanceSettingsTestHelper;
import org.andstatus.todoagenda.task.dmfs.DmfsOpenTasksContract;
import org.andstatus.todoagenda.testutil.ContentProviderForTests;
import org.andstatus.todoagenda.util.DateUtil;
import org.andstatus.todoagenda.widget.CalendarEntry;
import org.andstatus.todoagenda.widget.DayHeader;
import org.andstatus.todoagenda.widget.TaskEntry;
import org.andstatus.todoagenda.widget.WidgetEntry;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;

import java.util.List;

import static com.google.common.truth.Truth.assertThat;

@RunWith(RobolectricTestRunner.class)
public class EventRemoteViewsFactory_IntegrationTest {
    private static final String COLUMN_START_DATE = "EFFECTIVE_START_DATE";

    private final Context context = ApplicationProvider.getApplicationContext();
    private final int widgetId = 1;
    private final DateTimeZone zone = DateTimeZone.UTC;
    private final DateTime d0 = new DateTime(2019, 11, 10, 0, 0, zone);

    private ContentProviderForTests calendarProvider;
    private ContentProviderForTests taskProvider;

    /**
     * An integration test that tries to exercise as many components as possible.
     * <p>
     * Today, or D0 is Sunday, 2019-11-10.
     * <p>
     * The following entries are created:
     * <p>
     * -- D-1
     * * Finished event [Regular event]
     * <p>
     * -- D0
     * * Write integration test [Task, due on D-1]
     * * Review other tests [Task, starts on D-3, due on D0]
     * * Kalendar birthday [All day event]
     * <p>
     * -- D2
     * * Time off [Start of all day event]
     * * Medical appointment [Regular event]
     * <p>
     * -- D3
     * * Prepare release [Task, starts on D3]
     * * Time off [End of all day event]
     * <p>
     * -- D5
     * * Rentar car [Multi-day event starting at 13h00]
     * * Dentist [Regular event]
     * <p>
     * -- D6
     * * Rental car [Continuation]
     * <p>
     * -- D7
     * * Rental car [End of multi-day event at 16h30]
     */
    @Test
    public void eventsAndTasksTest() {
        setupWidget();
        createEntries();

        EventRemoteViewsFactory factory = new EventRemoteViewsFactory(context, widgetId);
        factory.onDataSetChanged();
        List<WidgetEntry> entries = factory.getWidgetEntries();

        checkEntries(entries);

        tearDown();
    }

    private void setupWidget() {
        DateUtil.setNow(d0);

        calendarProvider = Robolectric.setupContentProvider(ContentProviderForTests.class,
                CalendarContract.Instances.CONTENT_URI.getAuthority());
        taskProvider = Robolectric.setupContentProvider(ContentProviderForTests.class,
                DmfsOpenTasksContract.Tasks.PROVIDER_URI.getAuthority());

        InstanceSettingsTestHelper settingsHelper = new InstanceSettingsTestHelper(context, widgetId);
        settingsHelper.setLockedTimeZoneId(zone.getID());
        settingsHelper.setTaskSource("DMFS_OPEN_TASKS");
        settingsHelper.setEventsEnded(EndedSomeTimeAgo.YESTERDAY);
    }

    private void createEntries() {
        createCalendarEntries();
        createTaskEntries();
    }

    private void createCalendarEntries() {
        MatrixCursor cursor = new MatrixCursor(new String[]{
                CalendarContract.Instances.EVENT_ID,
                CalendarContract.Instances.TITLE,
                CalendarContract.Instances.BEGIN,
                CalendarContract.Instances.END,
                CalendarContract.Instances.ALL_DAY,
                CalendarContract.Instances.EVENT_LOCATION,
                CalendarContract.Instances.HAS_ALARM,
                CalendarContract.Instances.RRULE,
                CalendarContract.Instances.DISPLAY_COLOR,
        });

        addCalendarRow(cursor, 13L, "Kalendar birthday", d0, d0.plusDays(1), true, "Android Studio");
        addCalendarRow(cursor, 15L, "Finished event", d0.minusDays(1).plusHours(16), d0.minusDays(1).plusHours(18),
                false, null);
        addCalendarRow(cursor, 28L, "Rental car", d0.plusDays(5).plusHours(13),
                d0.plusDays(7).plusHours(16).plusMinutes(30), false, null);
        addCalendarRow(cursor, 32L, "Time off", d0.plusDays(2), d0.plusDays(4), true, "");
        addCalendarRow(cursor, 33L, "Medical appointment", d0.plusDays(2).plusHours(9),
                d0.plusDays(2).plusHours(10).plusMinutes(15), false, "Hospital");
        addCalendarRow(cursor, 34L, "Dentist", d0.plusDays(5).plusHours(14).plusMinutes(30),
                d0.plusDays(5).plusHours(15), false, null);
        calendarProvider.setQueryResult(cursor);
    }

    private void addCalendarRow(MatrixCursor cursor, long id, String title, DateTime startTime, DateTime endTime,
                                boolean allDay, String location) {
        cursor.newRow()
                .add(CalendarContract.Instances.EVENT_ID, id)
                .add(CalendarContract.Instances.TITLE, title)
                .add(CalendarContract.Instances.BEGIN, startTime.getMillis())
                .add(CalendarContract.Instances.END, endTime.getMillis())
                .add(CalendarContract.Instances.ALL_DAY, allDay ? 1 : 0)
                .add(CalendarContract.Instances.EVENT_LOCATION, location)
                .add(CalendarContract.Instances.HAS_ALARM, 0)
                .add(CalendarContract.Instances.RRULE, null)
                .add(CalendarContract.Instances.DISPLAY_COLOR, 0);
    }

    private void createTaskEntries() {
        MatrixCursor cursor = new MatrixCursor(new String[]{
                DmfsOpenTasksContract.Tasks.COLUMN_ID,
                DmfsOpenTasksContract.Tasks.COLUMN_TITLE,
                COLUMN_START_DATE,
                DmfsOpenTasksContract.Tasks.COLUMN_DUE_DATE,
                DmfsOpenTasksContract.Tasks.COLUMN_COLOR,
        });

        addTaskRow(cursor, 2L, "Prepare release", d0.plusDays(3), null);
        addTaskRow(cursor, 4L, "Review other tests", d0.minusDays(3), d0);
        addTaskRow(cursor, 3L, "Write integration test", null, d0.minusDays(1));
        taskProvider.setQueryResult(cursor);
    }

    private void addTaskRow(MatrixCursor cursor, long id, String title, DateTime startDate, DateTime dueDate) {
        cursor.newRow()
                .add(DmfsOpenTasksContract.Tasks.COLUMN_ID, id)
                .add(DmfsOpenTasksContract.Tasks.COLUMN_TITLE, title)
                .add(COLUMN_START_DATE, startDate != null ? startDate.getMillis() : null)
                .add(DmfsOpenTasksContract.Tasks.COLUMN_DUE_DATE, dueDate != null ? dueDate.getMillis() : null)
                .add(DmfsOpenTasksContract.Tasks.COLUMN_COLOR, 0);
    }

    private void checkEntries(List<WidgetEntry> entries) {
        int event = 0;
        checkDayHeader(entries.get(event++), d0.minusDays(1));
        checkCalendar(entries.get(event++), "Finished event", "4:00 PM - 6:00 PM", "");

        checkDayHeader(entries.get(event++), d0);
        checkTask(entries.get(event++), "Write integration test");
        checkTask(entries.get(event++), "Review other tests");
        checkCalendar(entries.get(event++), "Kalendar birthday", "", "Android Studio");

        checkDayHeader(entries.get(event++), d0.plusDays(2));
        checkCalendar(entries.get(event++), "Time off", "", "");
        checkCalendar(entries.get(event++), "Medical appointment", "9:00 AM - 10:15 AM", "Hospital");

        checkDayHeader(entries.get(event++), d0.plusDays(3));
        checkTask(entries.get(event++), "Prepare release");
        checkCalendar(entries.get(event++), "Time off", "", "");

        checkDayHeader(entries.get(event++), d0.plusDays(5));
        checkCalendar(entries.get(event++), "Rental car", "1:00 PM →", "");
        checkCalendar(entries.get(event++), "Dentist", "2:30 PM - 3:00 PM", "");

        checkDayHeader(entries.get(event++), d0.plusDays(6));
        checkCalendar(entries.get(event++), "Rental car", "→  →", "");

        checkDayHeader(entries.get(event++), d0.plusDays(7));
        checkCalendar(entries.get(event++), "Rental car", "→ 4:30 PM", "");

        assertThat(entries).hasSize(event);
    }

    private void checkDayHeader(WidgetEntry entry, DateTime day) {
        assertThat(entry).isInstanceOf(DayHeader.class);
        assertThat(entry.getStartDate()).isEqualTo(day);
    }

    private void checkCalendar(WidgetEntry entry, String title, String timeString, String locationString) {
        assertThat(entry).isInstanceOf(CalendarEntry.class);
        CalendarEntry calendarEntry = (CalendarEntry) entry;
        assertThat(calendarEntry.getTitle()).isEqualTo(title);
        assertThat(calendarEntry.getEventTimeString()).isEqualTo(timeString);
        assertThat(calendarEntry.getLocationString()).isEqualTo(locationString);
    }

    private void checkTask(WidgetEntry entry, String title) {
        assertThat(entry).isInstanceOf(TaskEntry.class);
        assertThat(((TaskEntry) entry).getTitle()).isEqualTo(title);
    }

    private void tearDown() {
        DateUtil.setNow(null);
    }
}
