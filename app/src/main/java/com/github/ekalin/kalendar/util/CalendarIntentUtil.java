package com.github.ekalin.kalendar.util;

import android.content.ContentUris;
import android.content.Intent;
import android.net.Uri;
import android.provider.CalendarContract;
import android.provider.CalendarContract.Events;

import org.joda.time.DateTime;

import com.github.ekalin.kalendar.KalendarClickReceiver;

public class CalendarIntentUtil {
    private static final String TIME = "time";

    public static Intent createOpenCalendarAtDayFillInIntent(DateTime goToTime) {
        Intent intent = new Intent();
        Uri.Builder uriBuilder = CalendarContract.CONTENT_URI.buildUpon();
        uriBuilder.appendPath(TIME);
        ContentUris.appendId(uriBuilder, goToTime.getMillis());
        intent.putExtra(KalendarClickReceiver.VIEW_ENTRY_DATA, uriBuilder.toString());
        return intent;
    }

    public static Intent createViewIntent() {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
                | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        return intent;
    }

    public static Intent createNewEventIntent() {
        return new Intent(Intent.ACTION_INSERT, Events.CONTENT_URI)
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
                        | Intent.FLAG_ACTIVITY_CLEAR_TOP);
    }
}
