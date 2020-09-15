package com.github.ekalin.kalendar.task.astridclone;

import android.net.Uri;

public class AstridCloneTasksContract {
    public static final String APP_PACKAGE = "org.tasks";
    public static final String AUTHORITY = APP_PACKAGE;

    public static class Tasks {
        public static final Uri PROVIDER_URI = Uri.parse("content://" + AUTHORITY + "/todoagenda");
        public static final Uri VIEW_URI = Uri.parse("content://" + AUTHORITY + "/tasks");

        public static final String COLUMN_ID = "_id";
        public static final String COLUMN_TITLE = "title";
        public static final String COLUMN_DUE_DATE = "dueDate";
        public static final String COLUMN_START_DATE = "hideUntil";
        public static final String COLUMN_COLOR_LOCAL = "cdl_color";
        public static final String COLUMN_COLOR_GOOGLE = "gtl_color";
        public static final String COLUMN_COMPLETED = "completed";
        public static final String COLUMN_LIST_ID_LOCAL = "cdl_id";
        public static final String COLUMN_LIST_ID_GOOGLE = "gtl_id";
    }

    public static class TaskLists {
        public static final Uri PROVIDER_URI = Uri.parse("content://" + AUTHORITY + "/lists");

        public static final String COLUMN_ID = "cdl_id";
        public static final String COLUMN_NAME = "cdl_name";
        public static final String COLUMN_COLOR = "cdl_color";
        public static final String COLUMN_ACCOUNT_NAME = "cdl_account";
    }

    public static class GoogleTaskLists {
        public static final Uri PROVIDER_URI = Uri.parse("content://" + AUTHORITY + "/google_lists");

        public static final String COLUMN_ID = "gtl_id";
        public static final String COLUMN_NAME = "gtl_title";
        public static final String COLUMN_COLOR = "gtl_color";
        public static final String COLUMN_ACCOUNT_NAME = "gtl_account";
    }

    public static final String PERMISSION = "org.tasks.permission.READ_TASKS";
}
