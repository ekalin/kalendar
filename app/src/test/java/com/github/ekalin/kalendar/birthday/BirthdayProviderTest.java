package com.github.ekalin.kalendar.birthday;

import android.content.Context;
import android.database.MatrixCursor;
import android.provider.ContactsContract;
import androidx.test.core.app.ApplicationProvider;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;

import java.util.List;

import com.github.ekalin.kalendar.prefs.AllSettings;
import com.github.ekalin.kalendar.prefs.InstanceSettingsTestHelper;
import com.github.ekalin.kalendar.testutil.ContentProviderForTests;
import com.github.ekalin.kalendar.util.DateUtil;
import com.google.common.collect.Lists;
import com.google.common.truth.Correspondence;

import static com.google.common.truth.Truth.assertThat;

@RunWith(RobolectricTestRunner.class)
public class BirthdayProviderTest {
    private static final Correspondence<BirthdayEvent, String> EVENT_TITLE
            = Correspondence.transforming(BirthdayEvent::getTitle, "has title of");
    private static final Correspondence<BirthdayEvent, LocalDate> EVENT_DATE
            = Correspondence.transforming(BirthdayEvent::getDate, "has date of");

    private Context context;
    private ContentProviderForTests contentProvider;
    private BirthdayProvider birthdayProvider;

    @Before
    public void setup() {
        context = ApplicationProvider.getApplicationContext();
        contentProvider = Robolectric.setupContentProvider(ContentProviderForTests.class,
                ContactsContract.AUTHORITY);
        birthdayProvider = new BirthdayProvider(context, 1, AllSettings.instanceFromId(context, 1));
    }

    @After
    public void resetDate() {
        DateUtil.setNow(null);
    }

    @Test
    public void getEvents_returnsBirthdays() {
        DateUtil.setNow(new DateTime(2020, 10, 4, 14, 0));
        InstanceSettingsTestHelper settingsHelper = new InstanceSettingsTestHelper(context, 1);
        settingsHelper.setShowBirthdays(true);
        settingsHelper.setEventRage(15);
        setupContacts();

        List<BirthdayEvent> events = birthdayProvider.getEvents();

        assertThat(events).comparingElementsUsing(EVENT_TITLE).containsExactly(
                "Displayed (1)", "Displayed (2)", "Displayed (3)");
        assertThat(events).comparingElementsUsing(EVENT_DATE).containsExactly(
                new LocalDate(2020, 10, 19),
                new LocalDate(2020, 10, 8),
                new LocalDate(2020, 10, 4));
    }

    private void setupContacts() {
        MatrixCursor matrixCursor = new MatrixCursor(new String[]{
                ContactsContract.CommonDataKinds.Event.CONTACT_ID,
                ContactsContract.CommonDataKinds.Event.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Event.START_DATE,
        });

        List<MockContact> contacts = createContacts();
        for (MockContact contact : contacts) {
            matrixCursor.newRow()
                    .add(ContactsContract.CommonDataKinds.Event.CONTACT_ID, contact.id)
                    .add(ContactsContract.CommonDataKinds.Event.DISPLAY_NAME, contact.name)
                    .add(ContactsContract.CommonDataKinds.Event.START_DATE, contact.birthday);
        }
        contentProvider.setQueryResult(ContactsContract.Data.CONTENT_URI, matrixCursor);
    }

    private List<MockContact> createContacts() {
        MockContact c1 = new MockContact(34L, "Past (1)", "1992-10-03");
        MockContact c2 = new MockContact(169L, "Past (2)", "1980-07-15T08:00:00.000Z");
        MockContact c3 = new MockContact(15L, "Displayed (1)", "1987-10-19T08:00:00.000Z");
        MockContact c4 = new MockContact(213L, "Displayed (2)", "1998-10-08");
        MockContact c5 = new MockContact(213L, "Displayed (3)", "1989-10-04");
        MockContact c6 = new MockContact(443L, "Future (1)", "1998-10-20");
        MockContact c7 = new MockContact(98L, "Future (2)", "1952-01-03");
        return Lists.newArrayList(c1, c2, c3, c4, c5, c6, c7);
    }

    private static class MockContact {
        public final long id;
        public final String name;
        public final String birthday;

        public MockContact(long id, String name, String birthday) {
            this.id = id;
            this.name = name;
            this.birthday = birthday;
        }
    }
}
