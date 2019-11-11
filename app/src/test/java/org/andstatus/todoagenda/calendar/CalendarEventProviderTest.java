package org.andstatus.todoagenda.calendar;

import android.content.Context;
import android.database.MatrixCursor;
import android.net.Uri;
import android.provider.CalendarContract;
import androidx.test.core.app.ApplicationProvider;

import com.google.common.truth.Correspondence;

import org.andstatus.todoagenda.EndedSomeTimeAgo;
import org.andstatus.todoagenda.EnvironmentChangedReceiver;
import org.andstatus.todoagenda.prefs.AllSettings;
import org.andstatus.todoagenda.prefs.EventSource;
import org.andstatus.todoagenda.prefs.InstanceSettingsTestHelper;
import org.andstatus.todoagenda.testutil.ContentProviderForTests;
import org.andstatus.todoagenda.util.DateUtil;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.util.ReflectionHelpers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth.assertWithMessage;

@RunWith(RobolectricTestRunner.class)
public class CalendarEventProviderTest {
    private static final Correspondence<CalendarEvent, String> EVENT_TITLE
            = Correspondence.transforming(CalendarEvent::getTitle, "has title of");

    private Context context;
    private ContentProviderForTests contentProvider;
    private int daysRange;
    private CalendarEventProvider calendarProvider;

    @Before
    public void setup() {
        context = ApplicationProvider.getApplicationContext();
        contentProvider = Robolectric.setupContentProvider(ContentProviderForTests.class,
                CalendarContract.Instances.CONTENT_URI.getAuthority());
        daysRange = AllSettings.instanceFromId(context, 1).getEventRange();
        calendarProvider = new CalendarEventProvider(context, 1);
    }

    @After
    public void reset() {
        AllSettings.delete(context, 1);

        // We need to clear this because otherwise it remains from one test method to the other
        AtomicReference<EnvironmentChangedReceiver> registeredReceiver = ReflectionHelpers.getStaticField(EnvironmentChangedReceiver.class,
                "registeredReceiver");
        registeredReceiver.set(null);
    }

    @Test
    public void getEvents_shouldFilterEventsOutsideSearchRange() {
        setupEvents_forSearchRange();

        List<CalendarEvent> events = calendarProvider.getEvents();
        assertThat(events).comparingElementsUsing(EVENT_TITLE)
                .containsExactly("Overlaps start range", "Inside range", "Overlaps end range");
    }

    private void setupEvents_forSearchRange() {
        DateTime start = DateTime.now();
        DateTime end = start.plusDays(daysRange);

        MatrixCursor cursor = createCalendarCursor();
        addCalendarRow(cursor, start.minusHours(2), start.minusHours(1), "Before start");
        addCalendarRow(cursor, start.minusHours(1), start.plusHours(2), "Overlaps start range");
        addCalendarRow(cursor, start.plusHours(2), start.plusHours(3), "Inside range");
        addCalendarRow(cursor, end.minusHours(3), end.plusHours(1), "Overlaps end range");
        addCalendarRow(cursor, end.plusHours(2), end.plusHours(4), "After end");

        contentProvider.setQueryResult(cursor);
    }

    @Test
    public void getEvents_shouldConsiderEndedSomeTimeAgoSetting() {
        DateTime now = new DateTime(2019, 11, 9, 15, 0, 0, DateTimeZone.getDefault());
        DateUtil.setNow(now);
        new InstanceSettingsTestHelper(context, 1).setEventsEnded(EndedSomeTimeAgo.FOUR_HOURS);

        calendarProvider.getEvents();
        assertDatesWithTolerance(calendarProvider.getStartOfTimeRange(), now.minusHours(4));

        DateUtil.setNow(null);
    }

    @Test
    public void getEvents_shouldConsiderEventRangeSetting_forToday() {
        DateTime now = new DateTime(2019, 11, 9, 15, 0, 0, DateTimeZone.getDefault());
        DateUtil.setNow(now);
        new InstanceSettingsTestHelper(context, 1).setEventRage(0);

        calendarProvider.getEvents();
        assertDatesWithTolerance(calendarProvider.getEndOfTimeRange(), now.withTimeAtStartOfDay().plusDays(1));

        DateUtil.setNow(null);
    }

    @Test
    public void getEvents_shouldConsiderEventRangeSetting_for14Days() {
        DateTime now = new DateTime(2019, 11, 9, 15, 0, 0, DateTimeZone.getDefault());
        DateUtil.setNow(now);
        new InstanceSettingsTestHelper(context, 1).setEventRage(14);

        calendarProvider.getEvents();
        assertDatesWithTolerance(calendarProvider.getEndOfTimeRange(), now.plusDays(14));

        DateUtil.setNow(null);
    }

    private void assertDatesWithTolerance(DateTime actual, DateTime expected) {
        // Since a few ms have elapsed since setNow() and setting of startOfTimeRange/endOfTimeRange, we need a
        // little fuzzyness
        int toleranceMs = 500;
        assertWithMessage("%s is not equal to %s (with tolerance %sms)", actual, expected, toleranceMs)
                .that(actual.getMillis() - expected.getMillis()).isAtMost(toleranceMs);
    }

    @Test
    public void getEvents_shouldSubtractTZOffsetIfNegative() {
        AllSettings.instanceFromId(context, 1).setLockedTimeZoneId("-04:00");

        calendarProvider.getEvents();
        DateTime startOfTimeRange = calendarProvider.getStartOfTimeRange();
        Uri queryUri = contentProvider.getLastQueryUri();

        long expectedStartInQuery = startOfTimeRange.minusHours(4).getMillis();
        assertThat(queryUri.toString()).contains("when/" + expectedStartInQuery + '/');
    }

    @Test
    public void getEvents_shouldNotAddTZOffsetIfPositive() {
        AllSettings.instanceFromId(context, 1).setLockedTimeZoneId("+02:00");

        calendarProvider.getEvents();
        DateTime startOfTimeRange = calendarProvider.getStartOfTimeRange();
        Uri queryUri = contentProvider.getLastQueryUri();

        long expectedStartInQuery = startOfTimeRange.getMillis();
        assertThat(queryUri.toString()).contains("when/" + expectedStartInQuery + '/');
    }

    @Test
    public void getEvents_withoutShowOnlyClosest_returnsAllInstances() {
        setupEvents_forRecurringInstances();

        List<CalendarEvent> events = calendarProvider.getEvents();

        assertThat(events).hasSize(15);
    }

    @Test
    public void getEvents_withShowOnlyClosest_returnsOnlyFirstInstance() {
        InstanceSettingsTestHelper settingsHelper = new InstanceSettingsTestHelper(context, 1);
        settingsHelper.setShowOnlyClosestInstanceOfRecurringEvent(true);

        setupEvents_forRecurringInstances();

        List<CalendarEvent> events = calendarProvider.getEvents();
        assertThat(events).hasSize(1);
    }

    private void setupEvents_forRecurringInstances() {
        MatrixCursor cursor = createCalendarCursor();

        DateTime date = DateTime.now().withTimeAtStartOfDay().plusHours(10);
        for (int ind = 0; ind < 15; ind++) {
            date = date.plusDays(1);
            addCalendarRow(cursor, date, date.plusHours(9), "Work each day");
        }
        contentProvider.setQueryResult(cursor);
    }

    private MatrixCursor createCalendarCursor() {
        return new MatrixCursor(new String[]{
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
    }

    private void addCalendarRow(MatrixCursor cursor, DateTime start, DateTime end, String title) {
        cursor.newRow()
                .add(CalendarContract.Instances.EVENT_ID, 1)
                .add(CalendarContract.Instances.TITLE, title)
                .add(CalendarContract.Instances.BEGIN, start.getMillis())
                .add(CalendarContract.Instances.END, end.getMillis())
                .add(CalendarContract.Instances.ALL_DAY, 0)
                .add(CalendarContract.Instances.EVENT_LOCATION, null)
                .add(CalendarContract.Instances.HAS_ALARM, 0)
                .add(CalendarContract.Instances.RRULE, null)
                .add(CalendarContract.Instances.DISPLAY_COLOR, 0);
    }


    @Test
    public void getCalendars_returnsEventSources() {
        setupCalendars();

        List<EventSource> calendars = calendarProvider.getCalendars();

        assertThat(calendars).isEqualTo(createEventSources());
    }

    private void setupCalendars() {
        MatrixCursor matrixCursor = new MatrixCursor(new String[]{
                CalendarContract.Calendars._ID,
                CalendarContract.Calendars.CALENDAR_DISPLAY_NAME,
                CalendarContract.Calendars.CALENDAR_COLOR,
                CalendarContract.Calendars.ACCOUNT_NAME});
        for (EventSource source : createEventSources()) {
            matrixCursor.newRow()
                    .add(CalendarContract.Calendars._ID, source.getId())
                    .add(CalendarContract.Calendars.CALENDAR_DISPLAY_NAME, source.getTitle())
                    .add(CalendarContract.Calendars.CALENDAR_COLOR, source.getColor())
                    .add(CalendarContract.Calendars.ACCOUNT_NAME, source.getSummary());
        }
        contentProvider.setQueryResult(matrixCursor);
    }

    private Collection<EventSource> createEventSources() {
        List<EventSource> sources = new ArrayList<>();
        sources.add(new EventSource(2, "My Calendar", "Local account", 0xff000011));
        sources.add(new EventSource(4, "Work Items", "remote@account.org", 0xff000022));
        return sources;
    }
}
