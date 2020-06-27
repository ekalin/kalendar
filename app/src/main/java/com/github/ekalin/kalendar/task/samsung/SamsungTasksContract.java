package com.github.ekalin.kalendar.task.samsung;

import android.net.Uri;

public class SamsungTasksContract {
    public static final String APP_PACKAGE = "com.samsung.android.calendar";
    public static final String AUTHORITY = "com.android.calendar";

    public static class Tasks {
        public static final Uri PROVIDER_URI = Uri.parse("content://" + AUTHORITY + "/syncTasks");

        public static final String COLUMN_ID = "_id";
        public static final String COLUMN_TITLE = "subject";
        public static final String COLUMN_START_DATE = "utc_start_date";
        public static final String COLUMN_DUE_DATE = "utc_due_date";
        public static final String COLUMN_COLOR = "secAccountColor";
        public static final String COLUMN_COMPLETE = "complete";
        public static final String COLUMN_DELETED = "deleted";
        public static final String COLUMN_LIST_ID = "accountKey";
    }

    public static class TaskLists {
        public static final Uri PROVIDER_URI = Uri.parse("content://" + AUTHORITY + "/TasksAccounts");

        public static final String COLUMN_ID = "_sync_account_key";
        public static final String COLUMN_NAME = "displayName";
        public static final String COLUMN_COLOR = "secAccountColor";
    }

    public static final String INTENT_EXTRA_TASK = "task";
    public static final String INTENT_EXTRA_SELECTED = "selected";
    public static final String INTENT_EXTRA_ACTION_VIEW_FOCUS = "action_view_focus";
    public static final String INTENT_EXTRA_DETAIL_MODE = "DetailMode";
    public static final String INTENT_EXTRA_LAUNCH_FROM_WIDGET = "launch_from_widget";
}
