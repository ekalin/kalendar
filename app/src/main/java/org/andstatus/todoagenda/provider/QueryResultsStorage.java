package org.andstatus.todoagenda.provider;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;

import org.andstatus.todoagenda.EventRemoteViewsFactory;
import org.andstatus.todoagenda.R;
import org.andstatus.todoagenda.prefs.InstanceSettings;
import org.andstatus.todoagenda.util.DateUtil;
import org.andstatus.todoagenda.util.Optional;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author yvolk@yurivolkov.com
 */
public class QueryResultsStorage {
    private static final String TAG = QueryResultsStorage.class.getSimpleName();

    private static final String KEY_RESULTS_VERSION = "resultsVersion";
    private static final int RESULTS_VERSION = 2;
    private static final String KEY_CALENDAR_RESULTS = "results";
    private static final String KEY_TASK_RESULTS = "taskResults";

    private static volatile QueryResultsStorage theStorage = null;

    private final List<QueryResult> calendarResults = new CopyOnWriteArrayList<>();
    private final List<QueryResult> taskResults = new CopyOnWriteArrayList<>();

    public static void storeCalendar(QueryResult result) {
        if (theStorage != null) {
            theStorage.calendarResults.add(result);
        }
    }

    public static void storeTask(QueryResult result) {
        if (theStorage != null) {
            theStorage.taskResults.add(result);
        }
    }

    public static void shareEventsForDebugging(Context context, int widgetId) {
        final String method = "shareEventsForDebugging";
        try {
            Log.i(TAG, method + " started");
            setNeedToStoreResults(true);
            EventRemoteViewsFactory factory = new EventRemoteViewsFactory(context, widgetId);
            factory.onDataSetChanged();
            String results = theStorage.toJsonString(context, widgetId);
            if (TextUtils.isEmpty(results)) {
                Log.i(TAG, method + "; Nothing to share");
            } else {
                String fileName = "Todo_Agenda-" + widgetId + ".json";
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("application/json");
                intent.putExtra(Intent.EXTRA_SUBJECT, fileName);
                intent.putExtra(Intent.EXTRA_TEXT, results);
                context.startActivity(
                        Intent.createChooser(intent, context.getText(R.string.share_events_settings_for_debugging_title)));
                Log.i(TAG, method + "; Shared " + results);
            }
        } finally {
            setNeedToStoreResults(false);
        }
    }

    public static boolean getNeedToStoreResults() {
        return theStorage != null;
    }

    public static void setNeedToStoreResults(boolean needToStoreResults) {
        if (needToStoreResults) {
            theStorage = new QueryResultsStorage();
        } else {
            theStorage = null;
        }
    }

    public static QueryResultsStorage getStorage() {
        return theStorage;
    }

    public List<QueryResult> getCalendarResults() {
        return calendarResults;
    }

    public List<QueryResult> getTaskResults() {
        return taskResults;
    }

    private String toJsonString(Context context, int widgetId) {
        try {
            return toJson(context, widgetId).toString(2);
        } catch (JSONException e) {
            return "Error while formatting data " + e;
        }
    }

    public JSONObject toJson(Context context, int widgetId) throws JSONException {
        JSONObject json = WidgetData.fromWidgetId(context, widgetId).map(WidgetData::toJson).orElse(new JSONObject());
        json.put(KEY_RESULTS_VERSION, RESULTS_VERSION);
        json.put(KEY_CALENDAR_RESULTS, getResultsArray(widgetId, this.calendarResults));
        json.put(KEY_TASK_RESULTS, getResultsArray(widgetId, this.taskResults));
        return json;
    }

    private JSONArray getResultsArray(int widgetId, List<QueryResult> results) throws JSONException {
        JSONArray jsonArray = new JSONArray();
        for (QueryResult result : results) {
            if (result.getWidgetId() == widgetId) {
                jsonArray.put(result.toJson());
            }
        }
        return jsonArray;
    }

    public static QueryResultsStorage fromTestData(Context context, JSONObject json) throws JSONException {
        Optional<InstanceSettings> opSettings =  WidgetData.fromJson(json).flatMap(data -> data.getSettings(context));
        if (!opSettings.isPresent()) {
            throw new IllegalStateException("fromTestData without settings");
        }
        InstanceSettings settings = opSettings.get();

        // FIXME
        // AllSettings.getInstances(context).put(settings.getWidgetId(), settings);
        QueryResultsStorage results = new QueryResultsStorage();
        readResults(json, KEY_CALENDAR_RESULTS, settings.getWidgetId(), results.calendarResults);
        readResults(json, KEY_TASK_RESULTS, settings.getWidgetId(), results.taskResults);

        if (!results.calendarResults.isEmpty()) {
            DateTime now = results.calendarResults.get(0).getExecutedAt().toDateTime(DateTimeZone.getDefault());
            DateUtil.setNow(now);
        }
        return results;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        QueryResultsStorage results = (QueryResultsStorage) o;

        if (!compareResults(this.calendarResults, results.calendarResults)) return false;
        if (!compareResults(this.taskResults, results.taskResults)) return false;

        return true;
    }

    private boolean compareResults(List<QueryResult> r1, List<QueryResult> r2) {
        if (r1.size() != r2.size()) {
            return false;
        }
        for (int ind = 0; ind < r1.size(); ind++) {
            if (!r1.get(ind).equals(r2.get(ind))) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        int result = 0;
        for (int ind = 0; ind < calendarResults.size(); ind++) {
            result = 31 * result + calendarResults.get(ind).hashCode();
        }
        for (int ind = 0; ind < taskResults.size(); ind++) {
            result = 31 * result + taskResults.get(ind).hashCode();
        }
        return result;
    }

    @Override
    public String toString() {
        return "CalendarQueryResultsStorage{" +
                "results=" + calendarResults +
                ",taksResults=" + taskResults +
                '}';
    }
}
