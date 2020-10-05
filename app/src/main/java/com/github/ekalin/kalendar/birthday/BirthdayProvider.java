package com.github.ekalin.kalendar.birthday;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.provider.ContactsContract;
import androidx.core.util.Supplier;
import androidx.fragment.app.Fragment;

import org.joda.time.LocalDate;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import com.github.ekalin.kalendar.prefs.InstanceSettings;
import com.github.ekalin.kalendar.provider.EventProvider;
import com.github.ekalin.kalendar.util.CalendarIntentUtil;
import com.github.ekalin.kalendar.util.DateUtil;
import com.github.ekalin.kalendar.util.PermissionsUtil;

public class BirthdayProvider extends EventProvider {
    private static final String PERMISSION = Manifest.permission.READ_CONTACTS;
    private static final Uri CONTACTS_URI = ContactsContract.Data.CONTENT_URI;

    private LocalDate startDate;
    private LocalDate endDate;

    public BirthdayProvider(Context context, int widgetId, InstanceSettings settings) {
        super(context, widgetId, settings);
    }

    public List<BirthdayEvent> getEvents() {
        if (!settings.getShowBirthdays()) {
            return Collections.emptyList();
        }

        initialiseParameters();

        String[] projection = {
                ContactsContract.CommonDataKinds.Event._ID,
                ContactsContract.CommonDataKinds.Event.LOOKUP_KEY,
                ContactsContract.CommonDataKinds.Event.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Event.START_DATE,
        };
        String selection = getWhereClause();

        List<BirthdayEvent> birthdayEvents = queryProvider(CONTACTS_URI, projection, selection, this::createBirthday);

        return birthdayEvents.stream().filter(this::inDisplayRange).collect(Collectors.toList());
    }

    @Override
    protected void initialiseParameters() {
        super.initialiseParameters();
        startDate = mStartOfTimeRange.toLocalDate();
        endDate = mEndOfTimeRange.toLocalDate();
    }

    private String getWhereClause() {
        return ContactsContract.CommonDataKinds.Event.MIMETYPE + EQUALS + "'" + ContactsContract.CommonDataKinds.Event.CONTENT_ITEM_TYPE + "'"
                + AND + ContactsContract.CommonDataKinds.Event.TYPE + EQUALS + ContactsContract.CommonDataKinds.Event.TYPE_BIRTHDAY;
    }

    private BirthdayEvent createBirthday(Cursor cursor) {
        BirthdayEvent event = new BirthdayEvent();
        event.setId(cursor.getLong(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Event._ID)));
        event.setLookupKey(cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Event.LOOKUP_KEY)));
        event.setTitle(cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Event.DISPLAY_NAME)));
        event.setZone(zone);
        event.setColor(Color.GREEN);

        LocalDate birthDate = DateUtil.parseContactDate(
                cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Event.START_DATE)));
        LocalDate eventDate = DateUtil.birthDateToDisplayedBirthday(birthDate, settings);
        event.setDate(eventDate);

        return event;
    }

    private boolean inDisplayRange(BirthdayEvent event) {
        return !event.getDate().isBefore(startDate)
                && !event.getDate().isAfter(endDate);
    }

    public Intent createViewIntent(BirthdayEvent event) {
        Intent intent = CalendarIntentUtil.createViewIntent();
        intent.setData(ContactsContract.Contacts.getLookupUri(event.getId(), event.getLookupKey()));
        return intent;
    }

    public static boolean hasPermission(Context context) {
        return PermissionsUtil.isPermissionGranted(context, PERMISSION);
    }

    public static void requestPermission(Fragment fragment) {
        fragment.requestPermissions(new String[]{PERMISSION}, 1);
    }

    public static List<ContentObserver> registerObservers(Context context, Supplier<ContentObserver> observerCreator) {
        if (hasPermission(context)) {
            ContentObserver observer = observerCreator.get();
            context.getContentResolver().registerContentObserver(CONTACTS_URI, false, observer);
            return Collections.singletonList(observer);
        } else {
            return Collections.emptyList();
        }
    }
}
