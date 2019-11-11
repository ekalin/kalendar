package org.andstatus.todoagenda.provider;

import android.database.Cursor;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Useful for logging and Mocking CalendarContentProvider
 *
 * @author yvolk@yurivolkov.com
 */
public class QueryRow {
    private final Map<String, TypedValue> mRow = new HashMap<>();

    public static QueryRow fromCursor(Cursor cursor) {
        QueryRow row = new QueryRow();
        if (cursor != null && !cursor.isClosed()) {
            for (int ind = 0; ind < cursor.getColumnCount(); ind++) {
                row.mRow.put(cursor.getColumnName(ind), new TypedValue(cursor, ind));
            }
        }
        return row;
    }

    public JSONObject toJson() throws JSONException {
        JSONObject json = new JSONObject();
        for (Map.Entry<String, TypedValue> entry : mRow.entrySet()) {
            json.put(entry.getKey(), entry.getValue().toJson());
        }
        return json;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        QueryRow that = (QueryRow) o;
        if (mRow.size() != that.mRow.size()) {
            return false;
        }
        for (Map.Entry<String, TypedValue> entry : mRow.entrySet()) {
            if (!that.mRow.containsKey(entry.getKey())) {
                return false;
            }
            if (!entry.getValue().equals(that.mRow.get(entry.getKey()))) {
                return false;
            }
        }
        return mRow.equals(that.mRow);

    }

    @Override
    public int hashCode() {
        int result = 0;
        for (Map.Entry<String, TypedValue> entry : mRow.entrySet()) {
            result += 31 * entry.getValue().hashCode();
        }
        return result;
    }

    @Override
    public String toString() {
        try {
            return toJson().toString(2);
        } catch (JSONException e) {
            return this.getClass().getSimpleName() + "Error converting to Json "
                    + e.getMessage() + "; " + mRow.toString();
        }
    }

    private static class TypedValue {
        private static final String KEY_TYPE = "type";
        private static final String KEY_VALUE = "value";

        final CursorFieldType type;
        final Object value;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            TypedValue that = (TypedValue) o;

            if (type != CursorFieldType.UNKNOWN && that.type != CursorFieldType.UNKNOWN) {
                if (type != that.type) return false;
            }
            return !(value != null ? !value.toString().equals(that.value.toString()) : that.value != null);
        }

        @Override
        public int hashCode() {
            int result = type.hashCode();
            result = 31 * result + (value != null ? value.toString().hashCode() : 0);
            return result;
        }

        private enum CursorFieldType {
            UNKNOWN(-1),
            STRING(Cursor.FIELD_TYPE_STRING) {
                @Override
                public Object columnToObject(Cursor cursor, int columnIndex) {
                    return cursor.getString(columnIndex);
                }
            },
            INTEGER(Cursor.FIELD_TYPE_INTEGER) {
                @Override
                public Object columnToObject(Cursor cursor, int columnIndex) {
                    return cursor.getLong(columnIndex);
                }
            },
            BLOB(Cursor.FIELD_TYPE_BLOB) {
                @Override
                public Object columnToObject(Cursor cursor, int columnIndex) {
                    return cursor.getBlob(columnIndex);
                }
            },
            FLOAT(Cursor.FIELD_TYPE_FLOAT) {
                @Override
                public Object columnToObject(Cursor cursor, int columnIndex) {
                    return cursor.getDouble(columnIndex);
                }
            },
            NULL(Cursor.FIELD_TYPE_NULL);

            final int code;

            CursorFieldType(int fieldType) {
                code = fieldType;
            }

            public Object columnToObject(Cursor cursor, int columnIndex) {
                return null;
            }

            public static CursorFieldType fromColumnType(int cursorColumnType) {
                for (CursorFieldType val : values()) {
                    if (val.code == cursorColumnType) {
                        return val;
                    }
                }
                return UNKNOWN;
            }
        }

        public TypedValue(Cursor cursor, int columnIndex) {
            type = CursorFieldType.fromColumnType(cursor.getType(columnIndex));
            value = type.columnToObject(cursor, columnIndex);
        }

        public JSONObject toJson() throws JSONException {
            JSONObject json = new JSONObject();
            json.put(KEY_TYPE, type.code);
            json.put(KEY_VALUE, value);
            return json;
        }
    }
}
