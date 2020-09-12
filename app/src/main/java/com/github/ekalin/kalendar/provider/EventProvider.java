package com.github.ekalin.kalendar.provider;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.util.Log;
import androidx.annotation.NonNull;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import com.github.ekalin.kalendar.prefs.InstanceSettings;
import com.github.ekalin.kalendar.util.DateUtil;

import static android.graphics.Color.argb;
import static android.graphics.Color.blue;
import static android.graphics.Color.green;
import static android.graphics.Color.red;

public abstract class EventProvider {
    protected static final String AND_BRACKET = " AND (";
    protected static final String OPEN_BRACKET = "( ";
    protected static final String CLOSING_BRACKET = " )";
    protected static final String AND = " AND ";
    protected static final String OR = " OR ";
    protected static final String EQUALS = " = ";
    protected static final String NOT_EQUALS = " != ";
    protected static final String LTE = " <= ";
    protected static final String IS_NULL = " IS NULL";

    protected final Context context;
    protected final int widgetId;
    protected final InstanceSettings settings;

    // Below are parameters, which may change in settings
    protected DateTimeZone zone;
    protected KeywordsFilter mKeywordsFilter;
    protected DateTime mStartOfTimeRange;
    protected DateTime mEndOfTimeRange;

    public EventProvider(Context context, int widgetId, InstanceSettings settings) {
        this.context = context;
        this.widgetId = widgetId;
        this.settings = settings;
    }

    protected void initialiseParameters() {
        zone = getSettings().getTimeZone();
        mKeywordsFilter = new KeywordsFilter(getSettings().getHideBasedOnKeywords());
        mStartOfTimeRange = getSettings().getEventsEnded().endedAt(DateUtil.now(zone));
        mEndOfTimeRange = getEndOfTimeRange(DateUtil.now(zone));
    }

    private DateTime getEndOfTimeRange(DateTime now) {
        int dateRange = getSettings().getEventRange();
        return dateRange > 0
                ? now.plusDays(dateRange)
                : now.withTimeAtStartOfDay().plusDays(1);
    }

    @NonNull
    protected InstanceSettings getSettings() {
        return settings;
    }

    protected int getAsOpaque(int color) {
        return argb(255, red(color), green(color), blue(color));
    }

    protected <T> List<T> queryProviderAndStoreResults(Uri uri, String[] projection, String where,
                                                       QueryResult result,
                                                       Function<Cursor, T> converter) {
        return queryProvider(uri, projection, where, cursor -> {
            if (QueryResultsStorage.getNeedToStoreResults()) {
                result.addRow(cursor);
            }

            return converter.apply(cursor);
        });
    }

    protected <T> List<T> queryProvider(Uri uri, String[] projection, String where, Function<Cursor, T> converter) {
        List<T> results = new ArrayList<>();

        Cursor cursor;
        try {
            cursor = context.getContentResolver().query(uri, projection, where, null, null);
        } catch (SQLiteException | IllegalArgumentException e) {
            Log.i(getClass().getSimpleName(), e.getMessage());
            cursor = null;
        }
        if (cursor == null) {
            return results;
        }

        try {
            while (cursor.moveToNext()) {
                T created = converter.apply(cursor);
                if (!results.contains(created)) {
                    results.add(created);
                }
            }
        } finally {
            cursor.close();
        }

        return results;
    }
}
