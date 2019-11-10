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
import org.andstatus.todoagenda.provider.QueryResult;
import org.andstatus.todoagenda.provider.QueryRow;
import org.andstatus.todoagenda.testutil.ContentProviderForTests;
import org.andstatus.todoagenda.testutil.ShadowDummyAppWidgetManager;
import org.andstatus.todoagenda.util.DateUtil;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.util.ReflectionHelpers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth.assertWithMessage;

@RunWith(RobolectricTestRunner.class)
@Config(shadows = {ShadowDummyAppWidgetManager.class})
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
        DateTimeZone zone = start.getZone();

        QueryResult queryResult = new QueryResult(1, DateTime.now());
        addCalendarRow(queryResult, createEvent(start.minusHours(2), start.minusHours(1), "Before start", zone));
        addCalendarRow(queryResult, createEvent(start.minusHours(1), start.plusHours(2), "Overlaps start range", zone));
        addCalendarRow(queryResult, createEvent(start.plusHours(2), start.plusHours(3), "Inside range", zone));
        addCalendarRow(queryResult, createEvent(end.minusHours(3), end.plusHours(1), "Overlaps end range", zone));
        addCalendarRow(queryResult, createEvent(end.plusHours(2), end.plusHours(4), "After end", zone));

        contentProvider.setQueryResult(queryResult);
    }

    private CalendarEvent createEvent(DateTime start, DateTime endDate, String title, DateTimeZone zone) {
        CalendarEvent event = new CalendarEvent(context, 1, zone, false);
        event.setStartMillis(start.getMillis());
        event.setEndMillis(endDate.getMillis());
        event.setTitle(title);
        return event;
    }

    private void addCalendarRow(QueryResult queryResult, CalendarEvent event) {
        queryResult.addRow(new QueryRow()
                .setEventId(event.getEventId())
                .setTitle(event.getTitle())
                .setBegin(event.getStartMillis())
                .setEnd(event.getEndMillis())
                .setDisplayColor(event.getColor())
                .setAllDay(event.isAllDay() ? 1 : 0)
                .setEventLocation(event.getLocation())
                .setHasAlarm(event.isAlarmActive() ? 1 : 0)
                .setRRule(event.isRecurring() ? "FREQ=WEEKLY;WKST=MO;BYDAY=MO,WE,FR" : null));
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
        QueryResult queryResult = new QueryResult(1, DateTime.now());
        DateTime date = DateTime.now().withTimeAtStartOfDay();
        long millis = date.getMillis() + TimeUnit.HOURS.toMillis(10);
        for (int ind = 0; ind < 15; ind++) {
            millis += TimeUnit.DAYS.toMillis(1);
            queryResult.addRow(new QueryRow().setEventId(3).setTitle("Work each day")
                    .setBegin(millis).setEnd(millis + TimeUnit.HOURS.toMillis(9)));
        }
        contentProvider.setQueryResult(queryResult);
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
