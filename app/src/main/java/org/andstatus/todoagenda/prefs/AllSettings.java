package org.andstatus.todoagenda.prefs;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import androidx.annotation.NonNull;

import org.andstatus.todoagenda.EnvironmentChangedReceiver;
import org.andstatus.todoagenda.R;
import org.andstatus.todoagenda.provider.WidgetData;
import org.andstatus.todoagenda.util.Optional;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.andstatus.todoagenda.EventAppWidgetProvider.getWidgetIds;
import static org.andstatus.todoagenda.prefs.SettingsStorage.loadJsonFromFile;

/**
 * Singleton holder of settings for all widgets
 * @author yvolk@yurivolkov.com
 */
public class AllSettings {
    private static volatile boolean instancesLoaded = false;
    private static final Map<Integer, InstanceSettings> instances = new ConcurrentHashMap<>();

    @NonNull
    public static InstanceSettings instanceFromId(Context context, Integer widgetId) {
        ensureLoadedFromFiles(context, false);
        InstanceSettings settings = instances.get(widgetId);
        return settings == null ? newInstance(context, widgetId) : settings;
    }

    @NonNull
    private static InstanceSettings newInstance(Context context, Integer widgetId) {
        synchronized (instances) {
            InstanceSettings settings = instances.get(widgetId);
            if (settings == null) {
                if (widgetId != 0 && ApplicationPreferences.getWidgetId(context) == widgetId) {
                    settings = InstanceSettings.fromApplicationPreferences(context, widgetId);
                } else {
                    settings = new InstanceSettings(context, widgetId, "");
                }
                if (widgetId != 0) {
                    settings.save();
                    settings.logMe(AllSettings.class, "newInstance put", widgetId);
                    instances.put(widgetId, settings);
                    EnvironmentChangedReceiver.registerReceivers(settings);
                    EnvironmentChangedReceiver.updateWidget(context, widgetId);
                }
            }
            return settings;
        }
    }

    public static void ensureLoadedFromFiles(Context context, boolean reInitialize) {
        if (instancesLoaded && !reInitialize) {
            return;
        }
        synchronized (instances) {
            if (!instancesLoaded || reInitialize) {
                for (int widgetId : getWidgetIds(context)) {
                    try {
                        Optional<InstanceSettings> opSettings = InstanceSettings.fromJson(context, loadJsonFromFile(context,
                                getStorageKey(widgetId)));
                        if (opSettings.isPresent()) {
                            InstanceSettings settings = opSettings.get();
                            settings.logMe(AllSettings.class, "ensureLoadedFromFiles put", widgetId);
                            instances.put(widgetId, settings);
                        } else {
                            newInstance(context, widgetId);
                        }
                    } catch (Exception e) { // Starting from API21 android.system.ErrnoException may be thrown
                        Log.e("loadInstances", "widgetId:" + widgetId, e);
                        newInstance(context, widgetId);
                    }
                }
                instancesLoaded = true;
                if (!instances.isEmpty()) {
                    EnvironmentChangedReceiver.registerReceivers(instances.values().iterator().next());
                }
            }
        }
    }

    public static void loadFromTestData(Context context, InstanceSettings settings) {
        synchronized (instances) {
            instances.clear();
            if (settings.widgetId == 0) {
                settings.logMe(AllSettings.class, "Skipped loadFromTestData", settings.widgetId);
            } else {
                settings.logMe(AllSettings.class, "loadFromTestData put", settings.widgetId);
                instances.put(settings.widgetId, settings);
            }
            instancesLoaded = true;
            EnvironmentChangedReceiver.registerReceivers(instances.values().iterator().next());
        }
    }

    public static void saveFromApplicationPreferences(Context context, Integer widgetId) {
        if (widgetId == 0) {
            return;
        }
        InstanceSettings settings = InstanceSettings.fromApplicationPreferences(context, widgetId);
        InstanceSettings settingStored = instanceFromId(context, widgetId);
        if (settings.widgetId == widgetId && !settings.equals(settingStored)) {
            settings.save();
            settings.logMe(AllSettings.class, "saveFromApplicationPreferences put", widgetId);
            instances.put(widgetId, settings);
        }
    }

    @NonNull
    private static String getStorageKey(int widgetId) {
        return "instanceSettings" + widgetId;
    }

    public static void delete(Context context, int widgetId) {
        ensureLoadedFromFiles(context, false);
        synchronized (instances) {
            instances.remove(widgetId);
            SettingsStorage.delete(context, getStorageKey(widgetId));
            if (ApplicationPreferences.getWidgetId(context) == widgetId) {
                ApplicationPreferences.setWidgetId(context, 0);
            }
        }
    }

    public static String uniqueInstanceName(Context context, int widgetId, String proposedInstanceName) {
        if (proposedInstanceName != null && proposedInstanceName.trim().length() > 0
                && !existsInstanceName(widgetId, proposedInstanceName)) {
            return proposedInstanceName;
        }

        String nameByWidgetId = defaultInstanceName(context, widgetId);
        if (!existsInstanceName(widgetId, nameByWidgetId)) {
            return nameByWidgetId;
        }

        int index = 1;
        String name;
        do {
            name = defaultInstanceName(context, index);
            index = index + 1;
        } while (existsInstanceName(widgetId, name));
        return name;
    }

    private static String defaultInstanceName(Context context, int index) {
        return context.getText(R.string.app_name) + " " + index;
    }

    private static boolean existsInstanceName(int widgetId, String name) {
        for (InstanceSettings settings : instances.values()) {
            if (settings.getWidgetId() != widgetId && settings.getWidgetInstanceName().equals(name)) {
                return true;
            }
        }
        return false;
    }

    public static Map<Integer, InstanceSettings> getInstances(Context context) {
        ensureLoadedFromFiles(context, false);
        return instances;
    }

    public static boolean restoreWidgetSettings(Context context, JSONObject json, int targetWidgetId) {
        Optional<InstanceSettings> opSettings = WidgetData.fromJson(json)
                .flatMap(data -> data.getSettingsForWidget(context, targetWidgetId));
        if (opSettings.isPresent()) {
            InstanceSettings settings = opSettings.get();
            settings.save();
            settings.logMe(AllSettings.class, "restoreWidgetSettings put", settings.widgetId);
            instances.put(settings.widgetId, settings);
            return true;
        } else {
            Log.v(AllSettings.class.getSimpleName(), "Skipped restoreWidgetSettings, widgetId = " + targetWidgetId);
            return false;
        }
    }
}
