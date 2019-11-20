package org.andstatus.todoagenda.util;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import org.andstatus.todoagenda.MainActivity;
import org.andstatus.todoagenda.prefs.InstanceSettings;

/**
 * @author yvolk@yurivolkov.com
 */
public class PermissionsUtil {

    public final static String PERMISSION = Manifest.permission.READ_CALENDAR;

    private PermissionsUtil() {
        // Empty
    }

    @NonNull
    public static PendingIntent getPermittedPendingBroadcastIntent(InstanceSettings settings, Intent intent) {
        // We need unique request codes for each widget
        int requestCode = (intent.getAction() == null ? 1 : intent.getAction().hashCode()) + settings.getWidgetId();
        return arePermissionsGranted(settings.getContext())
                ? PendingIntent.getBroadcast(settings.getContext(), requestCode, intent,
                PendingIntent.FLAG_UPDATE_CURRENT)
                : getNoPermissionsPendingIntent(settings);
    }

    public static PendingIntent getNoPermissionsPendingIntent(InstanceSettings settings) {
        return PendingIntent.getActivity(settings.getContext(), settings.getWidgetId(),
                MainActivity.intentToStartMe(settings.getContext()), PendingIntent.FLAG_UPDATE_CURRENT);
    }

    @NonNull
    public static PendingIntent getPermittedPendingActivityIntent(InstanceSettings settings, Intent intent) {
        Intent intentPermitted = getPermittedActivityIntent(settings.getContext(), intent);
        return PendingIntent.getActivity(settings.getContext(), settings.getWidgetId(), intentPermitted, PendingIntent
                .FLAG_UPDATE_CURRENT);
    }

    @NonNull
    public static Intent getPermittedActivityIntent(@NonNull Context context, @NonNull Intent intent) {
        return arePermissionsGranted(context) ? intent : MainActivity.intentToStartMe(context);
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
            Class.forName("org.andstatus.todoagenda.testutil.ContentProviderForTests");
            return true;
        } catch (ClassNotFoundException e) {
            // Ignore
        }
        return false;
    }
}
