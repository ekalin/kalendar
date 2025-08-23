package com.github.ekalin.kalendar;

import android.content.Context;
import androidx.test.core.app.ApplicationProvider;

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
import org.robolectric.util.ReflectionHelpers;

import com.github.ekalin.kalendar.calendar.CalendarEvent;
import com.github.ekalin.kalendar.prefs.AllSettings;
import com.github.ekalin.kalendar.prefs.InstanceSettingsTestHelper;
import com.github.ekalin.kalendar.task.TaskEvent;
import com.github.ekalin.kalendar.util.DateUtil;
import com.github.ekalin.kalendar.widget.CalendarEntry;
import com.github.ekalin.kalendar.widget.DayHeader;
import com.github.ekalin.kalendar.widget.TaskEntry;
import com.github.ekalin.kalendar.widget.WidgetEntry;
import com.github.ekalin.kalendar.widget.WidgetEntryVisualizer;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import com.google.common.truth.Correspondence;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.doReturn;

@RunWith(RobolectricTestRunner.class)
public class WidgetEntryFactoryTest {
    private static final Correspondence<WidgetEntry, String> EVENT_TITLE
            = Correspondence.transforming(WidgetEntryFactoryTest::getEventTitle, "has title of");

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule().strictness(Strictness.STRICT_STUBS);

    @Mock
    private WidgetEntryVisualizer<WidgetEntry> eventProvider;

    private final Context context = ApplicationProvider.getApplicationContext();
    private final DateTime today = DateTime.now();

    @After
    public void reset() {
        DateUtil.setNow(null);

        AllSettings.delete(context, 1);

        // We need to clear this because otherwise it remains from one test method to the other
        AtomicReference<KalendarUpdater> registeredReceiver =
                ReflectionHelpers.getStaticField(KalendarUpdater.class,
                        "registeredReceiver");
        registeredReceiver.set(null);
    }

    @Test
    public void getEventEntries_returnsEventsSorted() {
        DateUtil.setNow(new DateTime(2019, 8, 7, 0, 0, DateTimeZone.getDefault()));

        new InstanceSettingsTestHelper(context, 1).setShowDayHeaders(false);

        doReturn(createEventListForSortTest()).when(eventProvider).getEventEntries();

        WidgetEntryFactory factory = createFactory(eventProvider);

        List<WidgetEntry> widgetEntries = factory.getWidgetEntries();
        assertThat(widgetEntries).comparingElementsUsing(EVENT_TITLE).containsExactly(
                "Day Header for 2019-08-08", "task1", "task2", "calendar1", "calendar2", "calendar3").inOrder();
    }

    private List<WidgetEntry> createEventListForSortTest() {
        DayHeader dayHeader = new DayHeader(new DateTime(2019, 8, 8, 0, 0), DateTimeZone.getDefault());
        TaskEntry task1 = createTaskEntry(new DateTime(2019, 8, 8, 12, 0),
                new DateTime(2019, 8, 9, 15, 0, 0), "task1");
        TaskEntry task2 = createTaskEntry(new DateTime(2019, 8, 8, 10, 0),
                new DateTime(2019, 8, 9, 16, 0, 0), "task2");
        CalendarEntry calendar1 = createCalendarEntry(new DateTime(2019, 8, 8, 0, 0), "calendar1");
        CalendarEntry calendar2 = createCalendarEntry(new DateTime(2019, 8, 8, 15, 30), "calendar2");
        CalendarEntry calendar3 = createCalendarEntry(new DateTime(2019, 8, 9, 10, 0), "calendar3");
        return Arrays.asList(task2, calendar3, calendar2, dayHeader, calendar1, task1);
    }

    @Test
    public void getEventEntries_addsDayHeaders() {
        InstanceSettingsTestHelper settingsHelper = new InstanceSettingsTestHelper(context, 1);
        settingsHelper.setShowDayHeaders(true);
        settingsHelper.setShowDaysWithoutEvents(false);

        doReturn(createEventListForDayHeaderTest()).when(eventProvider).getEventEntries();

        WidgetEntryFactory factory = createFactory(eventProvider);

        List<WidgetEntry> widgetEntries = factory.getWidgetEntries();
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
        InstanceSettingsTestHelper settingsHelper = new InstanceSettingsTestHelper(context, 1);
        settingsHelper.setShowDayHeaders(true);
        settingsHelper.setShowDaysWithoutEvents(true);

        doReturn(createEventListForDayHeaderTest()).when(eventProvider).getEventEntries();

        WidgetEntryFactory factory = createFactory(eventProvider);

        List<WidgetEntry> widgetEntries = factory.getWidgetEntries();
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

    private WidgetEntryFactory createFactory(WidgetEntryVisualizer<?> entryVisualizer) {
        WidgetEntryFactory factory = new WidgetEntryFactory(context, 1, AllSettings.instanceFromId(context, 1));
        // Make it a unit test
        List<WidgetEntryVisualizer<?>> eventVisualizers = ReflectionHelpers.getField(factory, "eventVisualizers");
        eventVisualizers.clear();
        eventVisualizers.add(eventProvider);

        return factory;
    }

    private TaskEntry createTaskEntry(DateTime startDate, DateTime dueDate, String title) {
        TaskEvent event = new TaskEvent();
        event.setZone(DateTimeZone.getDefault());
        event.setDates(startDate.getMillis(), dueDate.getMillis());
        event.setTitle(title);

        return TaskEntry.fromEvent(event);
    }

    private CalendarEntry createCalendarEntry(DateTime startDate, String title) {
        CalendarEvent event = new CalendarEvent(AllSettings.instanceFromId(context, 1), DateTimeZone.getDefault(),
                false);
        event.setStartMillis(startDate.getMillis());
        event.setTitle(title);
        return CalendarEntry.fromEvent(event, startDate);
    }

    private static String getEventTitle(WidgetEntry widgetEntry) {
        if (widgetEntry instanceof DayHeader) {
            return "Day Header for " + widgetEntry.getStartDay().toString("yyyy-MM-dd");
        } else if (widgetEntry instanceof CalendarEntry) {
            return ((CalendarEntry) widgetEntry).getTitle();
        } else if (widgetEntry instanceof TaskEntry) {
            return ((TaskEntry) widgetEntry).getTitle();
        }
        return "Unknown entry type";
    }
}
