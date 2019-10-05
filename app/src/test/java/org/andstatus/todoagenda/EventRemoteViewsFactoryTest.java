package org.andstatus.todoagenda;

import android.content.Context;
import androidx.test.core.app.ApplicationProvider;

import org.andstatus.todoagenda.calendar.CalendarEvent;
import org.andstatus.todoagenda.prefs.AllSettings;
import org.andstatus.todoagenda.prefs.ApplicationPreferences;
import org.andstatus.todoagenda.task.TaskEvent;
import org.andstatus.todoagenda.testutil.ShadowDummyAppWidgetManager;
import org.andstatus.todoagenda.widget.CalendarEntry;
import org.andstatus.todoagenda.widget.DayHeader;
import org.andstatus.todoagenda.widget.LastEntry;
import org.andstatus.todoagenda.widget.TaskEntry;
import org.andstatus.todoagenda.widget.WidgetEntry;
import org.andstatus.todoagenda.widget.WidgetEntryVisualizer;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.mockito.quality.Strictness;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.util.ReflectionHelpers;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.doReturn;

@RunWith(RobolectricTestRunner.class)
@Config(shadows = {ShadowDummyAppWidgetManager.class})
public class EventRemoteViewsFactoryTest {
    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule().strictness(Strictness.STRICT_STUBS);

    @Mock
    private WidgetEntryVisualizer eventProvider;

    private Context context = ApplicationProvider.getApplicationContext();
    private DateTime today = DateTime.now();

    @After
    public void reset() {
        AllSettings.delete(context, 1);

        // We need to clear this because otherwise it remains from one test method to the other
        AtomicReference<EnvironmentChangedReceiver> registeredReceiver = ReflectionHelpers.getStaticField(EnvironmentChangedReceiver.class,
                "registeredReceiver");
        registeredReceiver.set(null);
    }

    @Test
    public void getEventEntries_returnsEventsSorted() {
        ApplicationPreferences.fromInstanceSettings(context, 1);
        ApplicationPreferences.setShowDayHeaders(context, false);
        ApplicationPreferences.save(context, 1);

        doReturn(createEventListForSortTest()).when(eventProvider).getEventEntries();

        EventRemoteViewsFactory factory = createFactory(eventProvider);
        factory.onDataSetChanged();

        List<WidgetEntry> widgetEntries = factory.getWidgetEntries();
        removeLastEntry(widgetEntries);
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
        assertThat(widgetEntries).hasSize(5);

        WidgetEntry entry = widgetEntries.get(0);
        assertThat(entry).isInstanceOf(DayHeader.class);

        entry = widgetEntries.get(1);
        assertThat(entry).isInstanceOf(TaskEntry.class);

        entry = widgetEntries.get(2);
        assertThat(entry).isInstanceOf(CalendarEntry.class);
        CalendarEntry calendarEntry = (CalendarEntry) entry;
        assertThat(calendarEntry.getTitle()).isEqualTo("calendar1");

        entry = widgetEntries.get(3);
        assertThat(entry).isInstanceOf(CalendarEntry.class);
        calendarEntry = (CalendarEntry) entry;
        assertThat(calendarEntry.getTitle()).isEqualTo("calendar2");

        entry = widgetEntries.get(4);
        assertThat(entry).isInstanceOf(CalendarEntry.class);
        calendarEntry = (CalendarEntry) entry;
        assertThat(calendarEntry.getTitle()).isEqualTo("calendar3");
    }

    @Test
    public void getEventEntries_addsDayHeaders() {
        ApplicationPreferences.fromInstanceSettings(context, 1);
        ApplicationPreferences.setShowDayHeaders(context, true);
        ApplicationPreferences.setShowDaysWithoutEvents(context, false);
        ApplicationPreferences.save(context, 1);

        doReturn(createEventListForDayHeaderTest()).when(eventProvider).getEventEntries();

        EventRemoteViewsFactory factory = createFactory(eventProvider);
        factory.onDataSetChanged();

        List<WidgetEntry> widgetEntries = factory.getWidgetEntries();
        removeLastEntry(widgetEntries);
        assertDayHeaders(widgetEntries);
    }

    private void assertDayHeaders(List<WidgetEntry> widgetEntries) {
        assertThat(widgetEntries).hasSize(4);

        WidgetEntry entry = widgetEntries.get(0);
        assertThat(entry).isInstanceOf(DayHeader.class);
        assertThat(entry.getStartDay()).isEqualTo(today.withTimeAtStartOfDay());

        entry = widgetEntries.get(2);
        assertThat(entry).isInstanceOf(DayHeader.class);
        assertThat(entry.getStartDay()).isEqualTo(today.plusDays(2).withTimeAtStartOfDay());
    }

    @Test
    public void getEventEntries_addsDayHeadersForDaysWithoutEvents() {
        ApplicationPreferences.fromInstanceSettings(context, 1);
        ApplicationPreferences.setShowDayHeaders(context, true);
        ApplicationPreferences.setShowDaysWithoutEvents(context, true);
        ApplicationPreferences.save(context, 1);

        doReturn(createEventListForDayHeaderTest()).when(eventProvider).getEventEntries();

        EventRemoteViewsFactory factory = createFactory(eventProvider);
        factory.onDataSetChanged();

        List<WidgetEntry> widgetEntries = factory.getWidgetEntries();
        removeLastEntry(widgetEntries);
        assertDayHeadersForDaysWithoutEvents(widgetEntries);
    }

    private void assertDayHeadersForDaysWithoutEvents(List<WidgetEntry> widgetEntries) {
        assertThat(widgetEntries).hasSize(5);

        WidgetEntry entry = widgetEntries.get(0);
        assertThat(entry).isInstanceOf(DayHeader.class);
        assertThat(entry.getStartDay()).isEqualTo(today.withTimeAtStartOfDay());

        entry = widgetEntries.get(2);
        assertThat(entry).isInstanceOf(DayHeader.class);
        assertThat(entry.getStartDay()).isEqualTo(today.plusDays(1).withTimeAtStartOfDay());

        entry = widgetEntries.get(3);
        assertThat(entry).isInstanceOf(DayHeader.class);
        assertThat(entry.getStartDay()).isEqualTo(today.plusDays(2).withTimeAtStartOfDay());
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
        TaskEvent event = new TaskEvent();
        event.setTaskDate(taskDate);
        event.setTitle(title);

        return TaskEntry.fromEvent(event);
    }

    private CalendarEntry createCalendarEntry(DateTime startDate, String title) {
        CalendarEvent event = new CalendarEvent(context, 1, DateTimeZone.getDefault(), false);
        event.setStartDate(startDate);
        event.setTitle(title);
        return CalendarEntry.fromEvent(event, startDate);
    }

    private void removeLastEntry(List<WidgetEntry> widgetEntries) {
        int last = widgetEntries.size() - 1;
        if (widgetEntries.get(last) instanceof LastEntry) {
            widgetEntries.remove(last);
        }
    }
}
