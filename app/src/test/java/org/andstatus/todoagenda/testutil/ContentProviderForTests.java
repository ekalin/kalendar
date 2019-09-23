package org.andstatus.todoagenda.testutil;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.andstatus.todoagenda.provider.QueryResult;

public class ContentProviderForTests extends ContentProvider {
    private Cursor cursor;
    private QueryResult queryResult;
    private Uri lastQueryUri;

    @Override
    public boolean onCreate() {
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection,
                        @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        lastQueryUri = uri;

        if (cursor != null) {
            return cursor;
        }
        if (queryResult != null) {
            return queryResult.query(projection);
        }
        return null;
    }

    public void setQueryResult(Cursor cursor) {
        this.cursor = cursor;
    }

    public void setQueryResult(QueryResult queryResult) {
        this.queryResult = queryResult;
    }

    public Uri getLastQueryUri() {
        return lastQueryUri;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        return null;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection,
                      @Nullable String[] selectionArgs) {
        return 0;
    }
}
