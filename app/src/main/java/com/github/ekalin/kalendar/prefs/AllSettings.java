package com.github.ekalin.kalendar.prefs;

import android.content.Context;
import androidx.annotation.NonNull;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import com.github.ekalin.kalendar.R;
import com.github.ekalin.kalendar.provider.WidgetData;
import com.github.ekalin.kalendar.util.Optional;

import static com.github.ekalin.kalendar.EventAppWidgetProvider.getWidgetIds;

/**
 * Singleton holder of settings for all widgets
 *
 * @author yvolk@yurivolkov.com
 */
public class AllSettings {
    @NonNull
    public static InstanceSettings instanceFromId(Context context, int widgetId) {
        InstanceSettings settings = new InstanceSettings(context, widgetId);
        settings.setWidgetInstanceNameIfNew(uniqueInstanceName(context, widgetId));
        return settings;
    }

    @NonNull
    private static InstanceSettings existingInstanceFromId(Context context, int widgetId) {
        return new InstanceSettings(context, widgetId);
    }

    public static void delete(Context context, int widgetId) {
        existingInstanceFromId(context, widgetId).delete();
    }

    private static String uniqueInstanceName(Context context, int widgetId) {
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
                .flatMap(WidgetData::getSettingsFromJson)
                .map(jsonSettings -> InstanceSettings.fromJson(context, targetWidgetId, jsonSettings));

        return opSettings.isPresent();
    }
}
