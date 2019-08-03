package android.text.format;

import android.content.Context;

import org.mockito.Mockito;

import java.util.Formatter;

public interface DateUtilsMock {
    DateUtilsMock INSTANCE = Mockito.mock(DateUtilsMock.class);

    String formatDateTime(Context context, long millis, int flags);

    Formatter formatDateRange(Context context, Formatter formatter, long startMillis,
                              long endMillis, int flags, String timeZone);
}
