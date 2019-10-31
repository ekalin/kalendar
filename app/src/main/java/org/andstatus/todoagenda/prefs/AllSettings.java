package org.andstatus.todoagenda.prefs;

import android.content.Context;
import android.util.Log;
import androidx.annotation.NonNull;

import org.andstatus.todoagenda.EnvironmentChangedReceiver;
import org.andstatus.todoagenda.R;
import org.andstatus.todoagenda.provider.WidgetData;
import org.andstatus.todoagenda.util.Optional;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.andstatus.todoagenda.EventAppWidgetProvider.getWidgetIds;

/**
 * Singleton holder of settings for all widgets
 * @author yvolk@yurivolkov.com
 */
public class AllSettings {
    private static volatile boolean instancesLoaded = false;
    private static final Map<Integer, InstanceSettings> instances = new ConcurrentHashMap<>();

    @NonNull
    public static InstanceSettings instanceFromId(Context context, Integer widgetId) {
        InstanceSettings settings = new InstanceSettings(context, widgetId);
        settings.setWidgetInstanceNameIfNew(uniqueInstanceName(context, widgetId));
        return settings;
    }

    @NonNull
    private static InstanceSettings existingInstanceFromId(Context context, Integer widgetId) {
        return new InstanceSettings(context, widgetId);
    }

    public static void ensureLoadedFromFiles(Context context, boolean reInitialize) {
        // noop
    }

    // FIXME
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
        // noop
    }

    @NonNull
    private static String getStorageKey(int widgetId) {
        return "instanceSettings" + widgetId;
    }

    public static void delete(Context context, int widgetId) {
        existingInstanceFromId(context, widgetId).delete();
    }

    public static String uniqueInstanceName(Context context, int widgetId) {
        Map<Integer, String> instances = getInstances(context);

        String nameByWidgetId = defaultInstanceName(context, widgetId);
        if (!existsInstanceName(widgetId, nameByWidgetId, instances)) {
            return nameByWidgetId;
        }

        int index = 1;
        String name;
        do {
            name = defaultInstanceName(context, index);
            index = index + 1;
        } while (existsInstanceName(widgetId, name, instances));
        return name;
    }

    private static String defaultInstanceName(Context context, int index) {
        return context.getText(R.string.app_name) + " " + index;
    }

    private static boolean existsInstanceName(int widgetId, String name, Map<Integer, String> instances) {
        for (Map.Entry<Integer, String> instance : instances.entrySet()) {
            if (instance.getKey() != widgetId && instance.getValue().equals(name)) {
                return true;
            }
        }
        return false;
    }

    public static Map<Integer, String> getInstances(Context context) {
        Map<Integer, String> instances = new HashMap<>();
        for (int widgetId : getWidgetIds(context)) {
            InstanceSettings settings = existingInstanceFromId(context, widgetId);
            instances.put(widgetId, settings.getWidgetInstanceName());
        }
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
