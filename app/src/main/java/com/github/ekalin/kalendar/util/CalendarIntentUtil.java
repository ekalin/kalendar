package com.github.ekalin.kalendar.util;

import android.app.PendingIntent;
import android.content.ContentUris;
import android.content.Intent;
import android.net.Uri;
import android.provider.CalendarContract;
import android.provider.CalendarContract.Events;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import com.github.ekalin.kalendar.KalendarClickReceiver;
import com.github.ekalin.kalendar.prefs.InstanceSettings;

public class CalendarIntentUtil {
    private static final String KEY_DETAIL_VIEW = "DETAIL_VIEW";
    private static final String TIME = "time";

    public static Intent createOpenCalendarAtDayIntent(DateTime goToTime) {
        Intent intent = createViewIntent();
        Uri.Builder builder = CalendarContract.CONTENT_URI.buildUpon();
        builder.appendPath(TIME);
        if (goToTime.getMillis() != 0) {
            intent.putExtra(KEY_DETAIL_VIEW, true);
            ContentUris.appendId(builder, goToTime.getMillis());
        }
        intent.setData(builder.build());
        return intent;
    }

    public static Intent createOpenCalendarAtDayFillInIntent(DateTime goToTime) {
        Intent intent = new Intent();
        Uri.Builder uriBuilder = CalendarContract.CONTENT_URI.buildUpon();
        uriBuilder.appendPath(TIME);
        ContentUris.appendId(uriBuilder, goToTime.getMillis());
        intent.putExtra(KalendarClickReceiver.VIEW_ENTRY_DATA, uriBuilder.toString());
        return intent;
    }

    public static PendingIntent createOpenCalendarPendingIntent(InstanceSettings settings) {
        return PermissionsUtil.getPermittedPendingActivityIntentMutable(settings,
                createOpenCalendarAtDayIntent(new DateTime(settings.getTimeZone())));
    }

    public static Intent createViewIntent() {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
                | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        return intent;
    }

    public static Intent createNewEventIntent(DateTimeZone timeZone) {
        DateTime beginTime = new DateTime(timeZone).plusHours(1).withMinuteOfHour(0).withSecondOfMinute(0)
                .withMillisOfSecond(0);
        DateTime endTime = beginTime.plusHours(1);
        return new Intent(Intent.ACTION_INSERT, Events.CONTENT_URI)
                .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, beginTime.getMillis())
                .putExtra(CalendarContract.EXTRA_EVENT_END_TIME, endTime.getMillis());
    }

    public static Intent createNewEventIntent() {
        return new Intent(Intent.ACTION_INSERT, Events.CONTENT_URI)
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
                        | Intent.FLAG_ACTIVITY_CLEAR_TOP);
    }
}
