package org.andstatus.todoagenda;

import android.content.Context;
import android.content.Intent;

import org.andstatus.todoagenda.calendar.CalendarEvent;
import org.andstatus.todoagenda.prefs.ApplicationPreferences;
import org.andstatus.todoagenda.task.TaskEvent;
import org.andstatus.todoagenda.widget.CalendarEntry;
import org.andstatus.todoagenda.widget.DayHeader;
import org.andstatus.todoagenda.widget.TaskEntry;
import org.andstatus.todoagenda.widget.WidgetEntry;
import org.andstatus.todoagenda.widget.WidgetEntryVisualizer;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.mockito.quality.Strictness;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.util.ReflectionHelpers;

import java.util.Arrays;
import java.util.List;

import androidx.test.core.app.ApplicationProvider;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.doReturn;

@RunWith(RobolectricTestRunner.class)
public class EventRemoteViewsFactoryTest {
    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule().strictness(Strictness.STRICT_STUBS);

    @Mock
    private WidgetEntryVisualizer eventProvider;

    private Context context = ApplicationProvider.getApplicationContext();
    private DateTime today = DateTime.now();

    @Test
    public void getEventEntries_returnsEventsSorted() {
        ApplicationPreferences.startEditing(context, 1);
        ApplicationPreferences.setShowDayHeaders(context, false);
        ApplicationPreferences.save(context, 1);

        doReturn(createEventListForSortTest()).when(eventProvider).getEventEntries();

        EventRemoteViewsFactory factory = createFactory(eventProvider);
        factory.onDataSetChanged();

        List<WidgetEntry> widgetEntries = factory.getWidgetEntries();
        assertEventOrder(widgetEntries);
    }

    private List<WidgetEntry> createEventListForSortTest() {
        DayHeader dayHeader = new DayHeader(new DateTime(2019, 8, 8, 0, 0));
        TaskEntry task1 = createTaskEntry(new DateTime(2019, 8, 8, 0, 0), "task1");
        CalendarEntry calendar1 = createCalendarEntry(new DateTime(2019, 8, 8, 0, 0), "calendar1");
        CalendarEntry calendar2 = createCalendarEntry(new DateTime(2019, 8, 8, 15, 30), "calendar2");
        CalendarEntry calendar3 = createCalendarEntry(new DateTime(2019, 8, 9, 10, 0), "calendar3");
        return Arrays.asList(calendar3, calendar2, dayHeader, calendar1, task1);
    }

    private void assertEventOrder(List<WidgetEntry> widgetEntries) {
        assertThat(widgetEntries, hasSize(5));

        WidgetEntry entry = widgetEntries.get(0);
        assertThat(entry, is(instanceOf(DayHeader.class)));

        entry = widgetEntries.get(1);
        assertThat(entry, is(instanceOf(TaskEntry.class)));

        entry = widgetEntries.get(2);
        assertThat(entry, is(instanceOf(CalendarEntry.class)));
        CalendarEntry calendarEntry = (CalendarEntry) entry;
        assertThat(calendarEntry.getTitle(), equalTo("calendar1"));

        entry = widgetEntries.get(3);
        assertThat(entry, is(instanceOf(CalendarEntry.class)));
        calendarEntry = (CalendarEntry) entry;
        assertThat(calendarEntry.getTitle(), equalTo("calendar2"));

        entry = widgetEntries.get(4);
        assertThat(entry, is(instanceOf(CalendarEntry.class)));
        calendarEntry = (CalendarEntry) entry;
        assertThat(calendarEntry.getTitle(), equalTo("calendar3"));
    }

    @Test
    public void getEventEntries_addsDayHeaders() {
        ApplicationPreferences.startEditing(context, 1);
        ApplicationPreferences.setShowDayHeaders(context, true);
        ApplicationPreferences.setShowDaysWithoutEvents(context, false);
        ApplicationPreferences.save(context, 1);

        doReturn(createEventListForDayHeaderTest()).when(eventProvider).getEventEntries();

        EventRemoteViewsFactory factory = createFactory(eventProvider);
        factory.onDataSetChanged();

        List<WidgetEntry> widgetEntries = factory.getWidgetEntries();
        assertDayHeaders(widgetEntries);
    }

    private void assertDayHeaders(List<WidgetEntry> widgetEntries) {
        assertThat(widgetEntries, hasSize(4));

        WidgetEntry entry = widgetEntries.get(0);
        assertThat(entry, is(instanceOf(DayHeader.class)));
        assertThat(entry.getStartDay(), equalTo(today.withTimeAtStartOfDay()));

        entry = widgetEntries.get(2);
        assertThat(entry, is(instanceOf(DayHeader.class)));
        assertThat(entry.getStartDay(), equalTo(today.plusDays(2).withTimeAtStartOfDay()));
    }

    @Test
    public void getEventEntries_addsDayHeadersForDaysWithoutEvents() {
        ApplicationPreferences.startEditing(context, 1);
        ApplicationPreferences.setShowDayHeaders(context, true);
        ApplicationPreferences.setShowDaysWithoutEvents(context, true);
        ApplicationPreferences.save(context, 1);

        doReturn(createEventListForDayHeaderTest()).when(eventProvider).getEventEntries();

        EventRemoteViewsFactory factory = createFactory(eventProvider);
        factory.onDataSetChanged();

        List<WidgetEntry> widgetEntries = factory.getWidgetEntries();
        assertDayHeadersForDaysWithoutEvents(widgetEntries);
    }

    private void assertDayHeadersForDaysWithoutEvents(List<WidgetEntry> widgetEntries) {
        assertThat(widgetEntries, hasSize(5));

        WidgetEntry entry = widgetEntries.get(0);
        assertThat(entry, is(instanceOf(DayHeader.class)));
        assertThat(entry.getStartDay(), equalTo(today.withTimeAtStartOfDay()));

        entry = widgetEntries.get(2);
        assertThat(entry, is(instanceOf(DayHeader.class)));
        assertThat(entry.getStartDay(), equalTo(today.plusDays(1).withTimeAtStartOfDay()));
        entry = widgetEntries.get(3);
        assertThat(entry, is(instanceOf(DayHeader.class)));
        assertThat(entry.getStartDay(), equalTo(today.plusDays(2).withTimeAtStartOfDay()));
    }

    private List<CalendarEntry> createEventListForDayHeaderTest() {
        CalendarEntry calendar1 = createCalendarEntry(today, "calendar1");
        CalendarEntry calendar2 = createCalendarEntry(today.plusDays(2), "calendar2");
        return Arrays.asList(calendar1, calendar2);
    }

    private EventRemoteViewsFactory createFactory(WidgetEntryVisualizer<?> eventProvider) {
        EventRemoteViewsFactory factory = new EventRemoteViewsFactory(context, 1);
        // Make it a unit test
        List<WidgetEntryVisualizer<?>> eventProviders = ReflectionHelpers.getField(factory, "eventProviders");
        eventProviders.clear();
        eventProviders.add(eventProvider);

        return factory;
    }

    private TaskEntry createTaskEntry(DateTime taskDate, String title) {
        TaskEvent event = new TaskEvent() {
            @Override
            public Intent createOpenCalendarEventIntent() {
                return null;
            }
        };
        event.setTaskDate(taskDate);
        event.setTitle(title);

        return TaskEntry.fromEvent(event);
    }

    private CalendarEntry createCalendarEntry(DateTime startDate, String title) {
        CalendarEvent event = new CalendarEvent(context, 1, DateTimeZone.UTC, false);
        event.setStartDate(startDate);
        event.setTitle(title);
        return CalendarEntry.fromEvent(event);
    }
}