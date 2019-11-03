package org.andstatus.todoagenda.util;

import android.content.Context;

import org.andstatus.todoagenda.prefs.InstanceSettings;
import org.andstatus.todoagenda.provider.QueryResult;
import org.andstatus.todoagenda.provider.QueryResultsStorage;
import org.andstatus.todoagenda.provider.WidgetData;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import static org.andstatus.todoagenda.prefs.InstanceSettings.PREF_WIDGET_ID;

public class QueryResultsStorageLoader {
    private static final String KEY_CALENDAR_RESULTS = "results";
    private static final String KEY_TASK_RESULTS = "taskResults";

    public static QueryResultsStorage fromTestData(Context context, JSONObject json) throws JSONException {
        Optional<InstanceSettings> opSettings = WidgetData.fromJson(json).flatMap(data -> getSettings(context, data));
        InstanceSettings settings = opSettings.orElseThrow(() -> new IllegalStateException("fromTestData without " +
                "settings"));

        QueryResultsStorage results = new QueryResultsStorage();
        readResults(json, KEY_CALENDAR_RESULTS, settings.getWidgetId(), results.getCalendarResults());
        readResults(json, KEY_TASK_RESULTS, settings.getWidgetId(), results.getTaskResults());

        if (!results.getCalendarResults().isEmpty()) {
            DateTime now = results.getCalendarResults().get(0).getExecutedAt().toDateTime(DateTimeZone.getDefault());
            DateUtil.setNow(now);
        }
        return results;
    }

    private static Optional<InstanceSettings> getSettings(Context context, WidgetData data) {
        return data.getSettingsFromJson().flatMap(jsonSettings -> createSettings(context, jsonSettings));
    }

    private static Optional<InstanceSettings> createSettings(Context context, JSONObject jsonSettings) {
        int widgetId = jsonSettings.optInt(PREF_WIDGET_ID);
        if (widgetId == 0) {
            return Optional.empty();
        }

        return Optional.of(InstanceSettings.fromJson(context, widgetId, jsonSettings));
    }

    private static void readResults(JSONObject json, String key, int widgetId, List<QueryResult> results) throws JSONException {
        if (!json.has(key)) {
            return;
        }

        JSONArray jsonResults = json.getJSONArray(key);
        for (int ind = 0; ind < jsonResults.length(); ind++) {
            results.add(QueryResult.fromJson(jsonResults.getJSONObject(ind), widgetId));
        }
    }
}
