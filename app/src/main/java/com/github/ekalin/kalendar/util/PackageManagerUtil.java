package com.github.ekalin.kalendar.util;

import android.content.Context;
import android.content.pm.PackageManager;

public class PackageManagerUtil {
    public static boolean isPackageInstalled(Context context, String packageName) {
        PackageManager packageManager = context.getPackageManager();
        try {
            packageManager.getPackageInfo(packageName, 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }
}
