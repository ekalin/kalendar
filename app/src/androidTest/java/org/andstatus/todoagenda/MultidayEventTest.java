package org.andstatus.todoagenda;

import android.test.InstrumentationTestCase;
import android.util.Log;

import org.andstatus.todoagenda.calendar.CalendarEvent;
import org.andstatus.todoagenda.calendar.MockCalendarContentProvider;
import org.andstatus.todoagenda.util.DateUtil;
import org.andstatus.todoagenda.widget.CalendarEntry;
import org.andstatus.todoagenda.widget.WidgetEntry;
import org.joda.time.DateTime;

/**
 * @author yvolk@yurivolkov.com
 */
public class MultidayEventTest extends InstrumentationTestCase {

    private static final String TAG = MultidayEventTest.class.getSimpleName();
    private static final String ARROW = "→";

    private MockCalendarContentProvider provider = null;
    private EventRemoteViewsFactory factory = null;
    private int eventId = 0;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        provider = MockCalendarContentProvider.getContentProvider(this);
        factory = new EventRemoteViewsFactory(provider.getContext(), provider.getWidgetId());
    }

    @Override
    protected void tearDown() throws Exception {
        provider.tearDown();
        super.tearDown();
    }

    /**
     * https://github.com/plusonelabs/calendar-widget/issues/184#issuecomment-142671469
     */
    public void testThreeDaysEvent() {
        DateTime friday = new DateTime(2015, 9, 18, 0, 0, 0, 0, provider.getSettings().getTimeZone());
        DateTime sunday = friday.plusDays(2);
        CalendarEvent event = new CalendarEvent(provider.getContext(), provider.getWidgetId(),
                provider.getSettings().getTimeZone(), false);
        event.setEventId(++eventId);
        event.setTitle("Leader's weekend");
        event.setStartDate(friday.plusHours(19));
        event.setEndDate(sunday.plusHours(15));

        assertSundayEntryAt(event, sunday, friday.plusHours(14));
        assertSundayEntryAt(event, sunday, friday.plusDays(1).plusHours(14));
        assertSundayEntryAt(event, sunday, friday.plusDays(2).plusHours(14));
    }

    private void assertSundayEntryAt(CalendarEvent event, DateTime sunday, DateTime currentDateTime) {
        CalendarEntry entry1 = getSundayEntryAt(event, currentDateTime);
        assertEquals(sunday, entry1.getStartDate());
        assertEquals(event.getEndDate(), entry1.getEndDate());
        assertEquals(event.getTitle(), entry1.getTitle());
        String timeString = entry1.getEventTimeString();
        assertTrue(timeString, timeString.contains(ARROW));
        assertEquals(timeString, timeString.indexOf(ARROW), timeString.lastIndexOf(ARROW));
    }

    private CalendarEntry getSundayEntryAt(CalendarEvent event, DateTime currentDateTime) {
        DateUtil.setNow(currentDateTime);
        provider.clear();
        provider.addRow(event);
        factory.onDataSetChanged();
        Log.i(TAG, "getSundayEntryAt " + currentDateTime);
        factory.logWidgetEntries(TAG);
        CalendarEntry sundayEntry = null;
        for (WidgetEntry item : factory.getWidgetEntries()) {
            if (item instanceof CalendarEntry) {
                CalendarEntry entry = (CalendarEntry) item;
                if (entry.getStartDate().getDayOfMonth() == 20) {
                    assertNull(sundayEntry);
                    sundayEntry = entry;
                }
            }
        }
        assertNotNull(sundayEntry);
        return sundayEntry;
    }
}
