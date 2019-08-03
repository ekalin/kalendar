package android.text.format;

import android.content.Context;

import java.util.Formatter;

/**
 * Fake implementation of DateUtils that allows mocking.
 * <p>
 * Necessary to avoid "Method xxxx not mocked".
 */
public class DateUtils {
    public static final int FORMAT_SHOW_WEEKDAY = 0x00002;
    public static final int FORMAT_SHOW_DATE = 0x00010;
    public static final int FORMAT_ABBREV_ALL = 0x80000;

    public static String formatDateTime(Context context, long millis, int flags) {
        return DateUtilsMock.INSTANCE.formatDateTime(context, millis, flags);
    }

    public static Formatter formatDateRange(Context context, Formatter formatter, long startMillis,
                                            long endMillis, int flags, String timeZone) {
        return DateUtilsMock.INSTANCE.formatDateRange(context, formatter, startMillis, endMillis, flags, timeZone);
    }
}
