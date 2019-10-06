package org.andstatus.todoagenda.util;

import org.andstatus.todoagenda.EventRemoteViewsFactory;
import org.andstatus.todoagenda.widget.LastEntry;
import org.andstatus.todoagenda.widget.WidgetEntry;

import java.lang.reflect.Field;
import java.util.List;

public class TestHelpers {
    public static void removeLastEntry(List<WidgetEntry> widgetEntries) {
        int last = widgetEntries.size() - 1;
        if (widgetEntries.get(last) instanceof LastEntry) {
            widgetEntries.remove(last);
        }
    }


    public static void forceReload(EventRemoteViewsFactory factory) {
        setField(factory, "prevReloadFinishedAt", 0);
    }

    private static void setField(Object object, String fieldName, Object value) {
        try {
            Field field = object.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(object, value);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new IllegalStateException("Could not set field " + fieldName, e);
        }
    }
}
