package com.github.ekalin.kalendar.birthday;

import android.Manifest;
import android.content.Context;
import androidx.fragment.app.Fragment;

import com.github.ekalin.kalendar.util.PermissionsUtil;

public class BirthdayProvider {
    private static final String PERMISSION = Manifest.permission.READ_CONTACTS;

    public static boolean hasPermission(Context context) {
        return PermissionsUtil.isPermissionGranted(context, PERMISSION);
    }

    public static void requestPermission(Fragment fragment) {
        fragment.requestPermissions(new String[]{PERMISSION}, 1);
    }
}
