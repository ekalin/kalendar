package com.github.ekalin.kalendar.provider;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import com.github.ekalin.kalendar.prefs.AllSettings;
import com.github.ekalin.kalendar.prefs.InstanceSettings;
import com.github.ekalin.kalendar.util.Optional;

public class WidgetData {
    private static final String TAG = WidgetData.class.getSimpleName();

    private static final String KEY_DEVICE_INFO = "deviceInfo";
    private static final String KEY_ANDROID_VERSION_CODE = "versionCode";
    private static final String KEY_ANDROID_VERSION_RELEASE = "versionRelease";
    private static final String KEY_ANDROID_VERSION_CODENAME = "versionCodename";
    private static final String KEY_ANDROID_MANUFACTURE = "buildManufacturer";
    private static final String KEY_ANDROID_BRAND = "buildBrand";
    private static final String KEY_ANDROID_MODEL = "buildModel";

    private static final String KEY_APP_INFO = "applicationInfo";
    private static final String KEY_APP_VERSION_NAME = "versionName";
    private static final String KEY_APP_VERSION_CODE = "versionCode";

    public static final String KEY_SETTINGS = "settings";

    private final JSONObject jsonData;

    public static Optional<WidgetData> fromJson(JSONObject jso) {
        return Optional.ofNullable(jso).map(WidgetData::new);
    }

    static Optional<WidgetData> fromWidgetId(Context context, int widgetId) {
        if (context == null || widgetId == 0) {
            return Optional.empty();
        } else {
            return Optional.of(fromSettings(AllSettings.instanceFromId(context, widgetId), false));
        }
    }

    public static WidgetData fromSettingsForBackup(InstanceSettings settings) {
        return fromSettings(settings, true);
    }

    private static WidgetData fromSettings(InstanceSettings settings, boolean forBackup) {
        JSONObject json = new JSONObject();
        try {
            json.put(KEY_DEVICE_INFO, getDeviceInfo());
            json.put(KEY_APP_INFO, getAppInfo(settings.getContext()));
            json.put(KEY_SETTINGS, forBackup ? settings.toJsonForBackup() : settings.toJsonComplete());
        } catch (JSONException e) {
            Log.w(TAG,"fromSettings failed; " + settings, e);
        }
        return new WidgetData(json);
    }

    private WidgetData(JSONObject jsonData) {
        this.jsonData = jsonData;
    }

    private static JSONObject getDeviceInfo() {
            JSONObject json = new JSONObject();
        try {
            json.put(KEY_ANDROID_VERSION_CODE, Build.VERSION.SDK_INT);
            json.put(KEY_ANDROID_VERSION_RELEASE, Build.VERSION.RELEASE);
            json.put(KEY_ANDROID_VERSION_CODENAME, Build.VERSION.CODENAME);
            json.put(KEY_ANDROID_MANUFACTURE, Build.MANUFACTURER);
            json.put(KEY_ANDROID_BRAND, Build.BRAND);
            json.put(KEY_ANDROID_MODEL, Build.MODEL);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json;
    }

    private static JSONObject getAppInfo(Context context) {
        JSONObject json = new JSONObject();
        try {
            try {
                PackageManager pm = context.getPackageManager();
                PackageInfo pi = pm.getPackageInfo(context.getApplicationContext().getPackageName(), 0);
                json.put(KEY_APP_VERSION_NAME, pi.versionName);
                json.put(KEY_APP_VERSION_CODE, pi.versionCode);
            } catch (PackageManager.NameNotFoundException e) {
                json.put(KEY_APP_VERSION_NAME, "Unable to obtain package information " + e);
                json.put(KEY_APP_VERSION_CODE, -1);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json;
    }

    public String toJsonString() {
        try {
            return toJson().toString(2);
        } catch (JSONException e) {
            return TAG + " Error while formatting data " + e;
        }
    }

    JSONObject toJson() {
        return jsonData;
    }

    public Optional<JSONObject> getSettingsFromJson() {
        JSONObject jsonSettings = jsonData.optJSONObject(KEY_SETTINGS);
        return Optional.ofNullable(jsonSettings);
    }
}
