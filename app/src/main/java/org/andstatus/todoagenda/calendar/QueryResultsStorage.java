package org.andstatus.todoagenda.calendar;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

import org.andstatus.todoagenda.DateUtil;
import org.andstatus.todoagenda.EventRemoteViewsFactory;
import org.andstatus.todoagenda.R;
import org.andstatus.todoagenda.prefs.InstanceSettings;
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
    private static final String KEY_APP_VERSION_NAME = "versionName";
    private static final String KEY_APP_VERSION_CODE = "versionCode";
    private static final String KEY_APP_INFO = "applicationInfo";
    public static final String KEY_SETTINGS = "settings";

    private static final String KEY_DEVICE_INFO = "deviceInfo";
    private static final String KEY_ANDROID_VERSION_CODE = "versionCode";
    private static final String KEY_ANDROID_VERSION_RELEASE = "versionRelease";
    private static final String KEY_ANDROID_VERSION_CODENAME = "versionCodename";
    private static final String KEY_ANDROID_MANUFACTURE = "buildManufacturer";
    private static final String KEY_ANDROID_BRAND = "buildBrand";
    private static final String KEY_ANDROID_MODEL = "buildModel";

    private static volatile QueryResultsStorage theStorage = null;

    private final List<QueryResult> calendarResults = new CopyOnWriteArrayList<>();
    private final List<QueryResult> taskResults = new CopyOnWriteArrayList<>();

    public static boolean storeCalendar(QueryResult result) {
        QueryResultsStorage storage = theStorage;
        if (storage != null) {
            storage.calendarResults.add(result);
            return (storage == theStorage);
        }
        return false;
    }

    public static boolean storeTask(QueryResult result) {
        QueryResultsStorage storage = theStorage;
        if (storage != null) {
            storage.taskResults.add(result);
            return (storage == theStorage);
        }
        return false;
    }

    public static void shareEventsForDebugging(Context context, int widgetId) {
        final String method = "shareEventsForDebugging";
        try {
            Log.i(TAG, method + " started");
            setNeedToStoreResults(true);
            EventRemoteViewsFactory factory = new EventRemoteViewsFactory(context, widgetId);
            factory.onDataSetChanged();
            String results = theStorage.getResultsAsString(context, widgetId);
            if (TextUtils.isEmpty(results)) {
                Log.i(TAG, method + "; Nothing to share");
            } else {
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/plain");
                intent.putExtra(Intent.EXTRA_SUBJECT, TAG);
                intent.putExtra(Intent.EXTRA_TEXT, results);
                context.startActivity(
                        Intent.createChooser(intent, context.getText(R.string.share_events_for_debugging_title)));
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

    private String getResultsAsString(Context context, int widgetId) {
        try {
            return toJson(context, widgetId).toString(2);
        } catch (JSONException e) {
            return "Error while formatting data " + e;
        }
    }

    public JSONObject toJson(Context context, int widgetId) throws JSONException {
        JSONObject json = new JSONObject();
        json.put(KEY_RESULTS_VERSION, RESULTS_VERSION);
        json.put(KEY_DEVICE_INFO, getDeviceInfo());
        json.put(KEY_APP_INFO, getAppInfo(context));
        json.put(KEY_SETTINGS, InstanceSettings.fromId(context, widgetId).toJson());
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

    public static QueryResultsStorage fromJson(Context context, JSONObject json) throws JSONException {
        InstanceSettings settings = InstanceSettings.fromJson(context, json.getJSONObject(KEY_SETTINGS));
        InstanceSettings.getInstances(context).put(settings.getWidgetId(), settings);
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

    private static JSONObject getAppInfo(Context context) throws JSONException {
        JSONObject json = new JSONObject();
        try {
            PackageManager pm = context.getPackageManager();
            PackageInfo pi = pm.getPackageInfo(context.getApplicationContext().getPackageName(), 0);
            json.put(KEY_APP_VERSION_NAME, pi.versionName);
            json.put(KEY_APP_VERSION_CODE, pi.versionCode);
        } catch (PackageManager.NameNotFoundException e) {
            json.put(KEY_APP_VERSION_NAME, "Unable to obtain package information " + e);
            json.put(KEY_APP_VERSION_CODE, -1);
        }
        return json;
    }

    private static JSONObject getDeviceInfo() throws JSONException {
        JSONObject json = new JSONObject();
        json.put(KEY_ANDROID_VERSION_CODE, Build.VERSION.SDK_INT);
        json.put(KEY_ANDROID_VERSION_RELEASE, Build.VERSION.RELEASE);
        json.put(KEY_ANDROID_VERSION_CODENAME, Build.VERSION.CODENAME);
        json.put(KEY_ANDROID_MANUFACTURE, Build.MANUFACTURER);
        json.put(KEY_ANDROID_BRAND, Build.BRAND);
        json.put(KEY_ANDROID_MODEL, Build.MODEL);
        return json;
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
