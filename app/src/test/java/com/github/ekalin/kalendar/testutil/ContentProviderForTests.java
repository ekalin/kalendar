package com.github.ekalin.kalendar.testutil;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;

public class ContentProviderForTests extends ContentProvider {
    private Cursor cursor;
    private Uri lastQueryUri;

    @Override
    public boolean onCreate() {
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        lastQueryUri = uri;
        return cursor;
    }

    public void setQueryResult(Cursor cursor) {
        this.cursor = cursor;
    }

    public Uri getLastQueryUri() {
        return lastQueryUri;
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
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        return 0;
    }
}
