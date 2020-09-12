package com.github.ekalin.kalendar.provider;

import android.database.Cursor;
import android.net.Uri;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.github.ekalin.kalendar.prefs.InstanceSettings;
import com.github.ekalin.kalendar.util.DateUtil;

/**
 * Useful for logging and mocking CalendarContentProvider
 *
 * @author yvolk@yurivolkov.com
 */
public class QueryResult {
    private static final String TAG = QueryResult.class.getSimpleName();
    private static final String KEY_ROWS = "rows";
    private static final String KEY_EXECUTED_AT = "executedAt";
    private static final String KEY_TIME_ZONE_ID = "timeZoneId";
    private static final String KEY_MILLIS_OFFSET_FROM_UTC_TO_LOCAL = "millisOffsetUtcToLocal";
    private static final String KEY_STANDARD_MILLIS_OFFSET_FROM_UTC_TO_LOCAL = "standardMillisOffsetUtcToLocal";
    private static final String KEY_URI = "uri";
    private static final String KEY_PROJECTION = "projection";
    private static final String KEY_SELECTION = "selection";

    private final DateTime executedAt;
    private final int widgetId;
    private Uri uri;
    private String[] projection;
    private String selection;
    private final List<QueryRow> rows = new ArrayList<>();

    public QueryResult(InstanceSettings settings, Uri uri, String[] projection, String selection) {
        this.widgetId = settings.getWidgetId();
        this.executedAt = DateUtil.now(settings.getTimeZone());
        this.uri = uri;
        this.projection = projection;
        this.selection = selection;
    }

    public int getWidgetId() {
        return widgetId;
    }

    public void addRow(Cursor cursor) {
        addRow(QueryRow.fromCursor(cursor));
    }

    private void addRow(QueryRow row) {
        rows.add(row);
    }

    public Uri getUri() {
        return uri;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        QueryResult that = (QueryResult) o;

        if (!uri.equals(that.uri)) return false;
        if (!Arrays.equals(projection, that.projection)) return false;
        if (!selection.equals(that.selection)) return false;
        if (rows.size() != that.rows.size()) return false;
        for (int ind = 0; ind < rows.size(); ind++) {
            if (!rows.get(ind).equals(that.rows.get(ind))) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        int result = uri.hashCode();
        result = 31 * result + Arrays.hashCode(projection);
        result = 31 * result + selection.hashCode();
        for (int ind = 0; ind < rows.size(); ind++) {
            result = 31 * result + rows.get(ind).hashCode();
        }
        return result;
    }

    @Override
    public String toString() {
        try {
            return toJson().toString(2);
        } catch (JSONException e) {
            return TAG + " Error converting to Json "
                    + e.getMessage() + "; " + rows.toString();
        }
    }

    public JSONObject toJson() throws JSONException {
        JSONObject json = new JSONObject();
        json.put(KEY_EXECUTED_AT, executedAt.getMillis());
        DateTimeZone zone = executedAt.getZone();
        json.put(KEY_TIME_ZONE_ID, zone.getID());
        json.put(KEY_MILLIS_OFFSET_FROM_UTC_TO_LOCAL, zone.getOffset(executedAt));
        json.put(KEY_STANDARD_MILLIS_OFFSET_FROM_UTC_TO_LOCAL, zone.getStandardOffset(executedAt.getMillis()));
        json.put(KEY_URI, uri != null ? uri.toString() : "");
        json.put(KEY_PROJECTION, arrayOfStingsToJson(projection));
        json.put(KEY_SELECTION, selection != null ? selection : "");
        JSONArray jsonArray = new JSONArray();
        for (QueryRow row : rows) {
            jsonArray.put(row.toJson());
        }
        json.put(KEY_ROWS, jsonArray);
        return json;
    }

    private static JSONArray arrayOfStingsToJson(String[] array) {
        JSONArray jsonArray = new JSONArray();
        if (array != null) {
            for (String item : array) {
                jsonArray.put(item);
            }
        }
        return jsonArray;
    }
}
