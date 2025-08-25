package com.github.ekalin.kalendar.birthday;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.util.Log;
import androidx.core.util.Supplier;

import org.joda.time.LocalDate;

import com.github.ekalin.kalendar.KalendarClickReceiver;
import com.github.ekalin.kalendar.prefs.InstanceSettings;
import com.github.ekalin.kalendar.prefs.PermissionRequester;
import com.github.ekalin.kalendar.provider.EventProvider;
import com.github.ekalin.kalendar.provider.QueryResult;
import com.github.ekalin.kalendar.provider.QueryResultsStorage;
import com.github.ekalin.kalendar.util.DateUtil;
import com.github.ekalin.kalendar.util.PermissionsUtil;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class BirthdayProvider extends EventProvider {
    private static final String TAG = BirthdayProvider.class.getSimpleName();
    private static final String PERMISSION = Manifest.permission.READ_CONTACTS;
    private static final Uri CONTACTS_URI = ContactsContract.Data.CONTENT_URI;

    private LocalDate startDate;
    private LocalDate endDate;

    public BirthdayProvider(Context context, int widgetId, InstanceSettings settings) {
        super(context, widgetId, settings);
    }

    public List<BirthdayEvent> getEvents() {
        if (!settings.getShowBirthdays() || !hasPermission(context)) {
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

        QueryResult result = new QueryResult(getSettings(), QueryResult.QueryResultType.BIRTHDAY, CONTACTS_URI, projection, selection);

        List<BirthdayEvent> birthdayEvents = queryProviderAndStoreResults(CONTACTS_URI, projection, selection, result, this::createBirthday);
        QueryResultsStorage.storeResult(result);

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
        event.setId(cursor.getLong(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Event._ID)));
        event.setLookupKey(cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Event.LOOKUP_KEY)));
        event.setTitle(cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Event.DISPLAY_NAME)));
        event.setZone(zone);
        event.setColor(settings.getBirthdayColor());

        LocalDate birthDate = DateUtil.parseContactDate(
                cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Event.START_DATE)));
        LocalDate eventDate = DateUtil.birthDateToDisplayedBirthday(birthDate, settings);
        event.setDate(eventDate);

        return event;
    }

    private boolean inDisplayRange(BirthdayEvent event) {
        return !event.getDate().isBefore(startDate)
                && !event.getDate().isAfter(endDate);
    }

    public Intent createViewIntent(BirthdayEvent event) {
        Intent intent = new Intent();
        intent.putExtra(KalendarClickReceiver.VIEW_ENTRY_DATA,
                ContactsContract.Contacts.getLookupUri(event.getId(), event.getLookupKey()).toString());
        return intent;
    }

    public static boolean hasPermission(Context context) {
        return PermissionsUtil.isPermissionGranted(context, PERMISSION);
    }

    public static void requestPermission(PermissionRequester requester) {
        requester.requestPermission(PERMISSION);
    }

    public static List<ContentObserver> registerObservers(Context context, Supplier<ContentObserver> observerCreator) {
        if (hasPermission(context)) {
            ContentObserver observer = observerCreator.get();
            context.getContentResolver().registerContentObserver(CONTACTS_URI, false, observer);
            Log.d(TAG, "Registered contentObserver");
            return Collections.singletonList(observer);
        } else {
            return Collections.emptyList();
        }
    }
}
