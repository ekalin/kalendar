package com.github.ekalin.kalendar.util;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import androidx.core.content.ContextCompat;

/**
 * @author yvolk@yurivolkov.com
 */
public class PermissionsUtil {
    public final static String PERMISSION = Manifest.permission.READ_CALENDAR;

    private PermissionsUtil() {
        // Empty
    }

    public static boolean arePermissionsGranted(Context context) {
        return isPermissionGranted(context, PERMISSION);
    }

    public static boolean isPermissionGranted(Context context, String permission) {
        return isTestMode() || ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Based on
     * http://stackoverflow.com/questions/21367646/how-to-determine-if-android-application-is-started-with-junit-testing-instrument
     */
    private static boolean isTestMode() {
        try {
            Class.forName("com.github.ekalin.kalendar.testutil.ContentProviderForTests");
            return true;
        } catch (ClassNotFoundException e) {
            // Ignore
        }
        return false;
    }
}
