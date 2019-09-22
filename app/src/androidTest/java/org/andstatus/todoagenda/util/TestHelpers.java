package org.andstatus.todoagenda.util;

import org.andstatus.todoagenda.EventRemoteViewsFactory;

import java.lang.reflect.Field;

public class TestHelpers {
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
