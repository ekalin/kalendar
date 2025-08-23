package com.github.ekalin.kalendar.provider;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.github.ekalin.kalendar.R;
import com.github.ekalin.kalendar.WidgetEntryFactory;
import com.github.ekalin.kalendar.prefs.AllSettings;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author yvolk@yurivolkov.com
 */
public class QueryResultsStorage {
    private static final String TAG = QueryResultsStorage.class.getSimpleName();

    private static final String KEY_RESULTS_VERSION = "resultsVersion";
    private static final int RESULTS_VERSION = 3;
    private static final String KEY_RESULTS = "results";

    private static volatile QueryResultsStorage theStorage = null;

    private final List<QueryResult> results = new CopyOnWriteArrayList<>();

    public static void storeResult(QueryResult result) {
        if (theStorage != null) {
            theStorage.results.add(result);
        }
    }

    public static void shareEventsForDebugging(Context context, int widgetId) {
        try {
            setNeedToStoreResults(true);
            WidgetEntryFactory factory = new WidgetEntryFactory(context, widgetId, AllSettings.instanceFromId(context, widgetId));
            factory.getWidgetEntries();
            String results = theStorage.toJsonString(context, widgetId);
            if (!TextUtils.isEmpty(results)) {
                String fileName = "Kalendar-" + widgetId + ".json";
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("application/json");
                intent.putExtra(Intent.EXTRA_SUBJECT, fileName);
                intent.putExtra(Intent.EXTRA_TEXT, results);
                context.startActivity(
                        Intent.createChooser(intent, context.getText(R.string.share_events_settings_for_debugging_title)));
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

    private String toJsonString(Context context, int widgetId) {
        try {
            return toJson(context, widgetId).toString(2);
        } catch (JSONException e) {
            return "Error while formatting data " + e;
        }
    }

    private JSONObject toJson(Context context, int widgetId) throws JSONException {
        JSONObject json = WidgetData.fromWidgetId(context, widgetId).map(WidgetData::toJson).orElse(new JSONObject());
        json.put(KEY_RESULTS_VERSION, RESULTS_VERSION);
        json.put(KEY_RESULTS, getResultsArray(widgetId, this.results));
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        QueryResultsStorage that = (QueryResultsStorage) o;

        if (!compareResults(this.results, that.results)) return false;

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
        for (QueryResult queryResult : results) {
            result = 31 * result + queryResult.hashCode();
        }
        return result;
    }

    @Override
    public String toString() {
        return "CalendarQueryResultsStorage{" +
                "results=" + results +
                '}';
    }
}
