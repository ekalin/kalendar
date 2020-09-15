package com.github.ekalin.kalendar.testutil;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;

import java.util.HashMap;
import java.util.Map;

public class ContentProviderForTests extends ContentProvider {
    private Map<Uri, Cursor> results = new HashMap<>();
    private Cursor defaultResults;
    private Uri lastQueryUri;
    private String lastQuerySelection;

    @Override
    public boolean onCreate() {
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        lastQueryUri = uri;
        lastQuerySelection = selection;
        return results.getOrDefault(uri, defaultResults);
    }

    public void setQueryResult(Cursor cursor) {
        defaultResults = cursor;
    }

    public void setQueryResult(Uri uri, Cursor cursor) {
        results.put(uri, cursor);
    }

    public Uri getLastQueryUri() {
        return lastQueryUri;
    }

    public String getLastQuerySelection() {
        return lastQuerySelection;
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        return null;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }
}
