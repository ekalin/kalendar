package org.andstatus.todoagenda;

import android.test.InstrumentationTestCase;

import org.andstatus.todoagenda.calendar.MockCalendarContentProvider;
import org.andstatus.todoagenda.prefs.InstanceSettingsTestHelper;
import org.andstatus.todoagenda.provider.QueryResultsStorage;
import org.andstatus.todoagenda.util.DateUtil;
import org.andstatus.todoagenda.util.TestHelpers;
import org.andstatus.todoagenda.widget.CalendarEntry;
import org.joda.time.DateTime;
import org.json.JSONException;

import java.io.IOException;

/**
 * @author yvolk@yurivolkov.com
 */
public class BirthdayTest extends InstrumentationTestCase {

    private static final String TAG = BirthdayTest.class.getSimpleName();

    private MockCalendarContentProvider provider = null;
    private EventRemoteViewsFactory factory = null;

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

    public void testBirthdayOneDayOnly() throws IOException, JSONException {
        QueryResultsStorage inputs = provider.loadResults(this.getInstrumentation().getContext(),
                org.andstatus.todoagenda.tests.R.raw.birthday);
        InstanceSettingsTestHelper settingsHelper = new InstanceSettingsTestHelper(provider.getContext(),
                provider.getWidgetId());

        settingsHelper.setEventsEnded(EndedSomeTimeAgo.NONE);
        settingsHelper.setEventRange(30);

        playAtOneTime(inputs, dateTime(2015, 8, 1, 17, 0), 0);
        playAtOneTime(inputs, dateTime(2015, 8, 9, 23, 59), 0);
        playAtOneTime(inputs, dateTime(2015, 8, 10, 0, 0).plusMillis(1), 2);
        playAtOneTime(inputs, dateTime(2015, 8, 10, 0, 1), 2);

        playAtOneTime(inputs, dateTime(2015, 9, 8, 17, 0), 2);
        playAtOneTime(inputs, dateTime(2015, 9, 8, 23, 30), 2);
        playAtOneTime(inputs, dateTime(2015, 9, 9, 0, 30), 2);
        playAtOneTime(inputs, dateTime(2015, 9, 9, 11, 0), 2);
        playAtOneTime(inputs, dateTime(2015, 9, 9, 17, 0), 2);
        playAtOneTime(inputs, dateTime(2015, 9, 9, 23, 30), 2);
        playAtOneTime(inputs, dateTime(2015, 9, 10, 0, 30), 0);
        playAtOneTime(inputs, dateTime(2015, 9, 10, 11, 0), 0);

        settingsHelper.setEventsEnded(EndedSomeTimeAgo.ONE_HOUR);
        playAtOneTime(inputs, dateTime(2015, 9, 10, 0, 30), 2);
        playAtOneTime(inputs, dateTime(2015, 9, 10, 1, 30), 0);

        settingsHelper.setEventsEnded(EndedSomeTimeAgo.TODAY);
        playAtOneTime(inputs, dateTime(2015, 9, 10, 1, 30), 0);

        settingsHelper.setEventsEnded(EndedSomeTimeAgo.FOUR_HOURS);
        playAtOneTime(inputs, dateTime(2015, 9, 10, 1, 30), 2);
        playAtOneTime(inputs, dateTime(2015, 9, 10, 3, 59), 2);
        playAtOneTime(inputs, dateTime(2015, 9, 10, 4, 0), 0);

        settingsHelper.setEventsEnded(EndedSomeTimeAgo.YESTERDAY);
        playAtOneTime(inputs, dateTime(2015, 9, 10, 4, 0), 2);
        playAtOneTime(inputs, dateTime(2015, 9, 10, 11, 0), 2);
        playAtOneTime(inputs, dateTime(2015, 9, 10, 17, 0), 2);
        playAtOneTime(inputs, dateTime(2015, 9, 10, 23, 30), 2);
        playAtOneTime(inputs, dateTime(2015, 9, 11, 0, 0), 0);
        playAtOneTime(inputs, dateTime(2015, 9, 11, 0, 30), 0);

        // TODO: This doesn't work yet. We need to inject a MockCalendarContentProvider deeper,
        // so it could work in a normal Context also
        provider.refreshWidget();
    }

    private DateTime dateTime(
            int year,
            int monthOfYear,
            int dayOfMonth,
            int hourOfDay,
            int minuteOfHour) {
        return new DateTime(year, monthOfYear, dayOfMonth, hourOfDay, minuteOfHour, 0, 0,
                provider.getSettings().getTimeZone());
    }

    private void playAtOneTime(QueryResultsStorage inputs, DateTime now, int numberOfEntriesExpected) {
        provider.addResults(inputs.getCalendarResults());
        DateUtil.setNow(now);
        TestHelpers.forceReload(factory);
        factory.onDataSetChanged();
        factory.logWidgetEntries(TAG);
        TestHelpers.removeLastEntry(factory.getWidgetEntries());

        assertEquals(numberOfEntriesExpected, factory.getWidgetEntries().size());
        if (numberOfEntriesExpected > 0) {
            CalendarEntry birthday = (CalendarEntry) factory.getWidgetEntries().get(1);
            assertEquals(9, birthday.getStartDate().dayOfMonth().get());
            assertEquals(0, birthday.getStartDate().hourOfDay().get());
            assertEquals(0, birthday.getStartDate().minuteOfHour().get());
            assertEquals(0, birthday.getStartDate().millisOfDay().get());
            assertEquals(true, birthday.isAllDay());
        }
    }
}
