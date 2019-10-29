package org.andstatus.todoagenda.prefs;

import android.content.Context;
import android.util.Log;
import androidx.annotation.NonNull;

import org.andstatus.todoagenda.EnvironmentChangedReceiver;
import org.andstatus.todoagenda.R;
import org.andstatus.todoagenda.provider.WidgetData;
import org.andstatus.todoagenda.util.Optional;
import org.json.JSONObject;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Singleton holder of settings for all widgets
 * @author yvolk@yurivolkov.com
 */
public class AllSettings {
    private static volatile boolean instancesLoaded = false;
    private static final Map<Integer, InstanceSettings> instances = new ConcurrentHashMap<>();

    @NonNull
    public static InstanceSettings instanceFromId(Context context, Integer widgetId) {
        return new InstanceSettings(context, widgetId, "");
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
        // FIXME: Add delete support
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
