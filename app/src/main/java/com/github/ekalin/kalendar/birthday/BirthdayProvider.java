package com.github.ekalin.kalendar.birthday;

import android.Manifest;
import android.content.Context;
import android.graphics.Color;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.github.ekalin.kalendar.prefs.InstanceSettings;
import com.github.ekalin.kalendar.provider.EventProvider;
import com.github.ekalin.kalendar.util.DateUtil;
import com.github.ekalin.kalendar.util.PermissionsUtil;

public class BirthdayProvider extends EventProvider {
    private static final String PERMISSION = Manifest.permission.READ_CONTACTS;

    public BirthdayProvider(Context context, int widgetId, InstanceSettings settings) {
        super(context, widgetId, settings);
    }

    public List<BirthdayEvent> getEvents() {
        if (!settings.getShowBirthdays()) {
            return Collections.emptyList();
        }

        initialiseParameters();

        BirthdayEvent ev1 = new BirthdayEvent();
        ev1.setId(1L);
        ev1.setTitle("Birthday today");
        ev1.setStartDate(DateUtil.now(zone));
        ev1.setColor(Color.GREEN);
        ev1.setZone(zone);

        BirthdayEvent ev2 = new BirthdayEvent();
        ev2.setId(2L);
        ev2.setTitle("Birthday tomorrow");
        ev2.setStartDate(DateUtil.now(zone).plusDays(1));
        ev2.setColor(Color.GREEN);
        ev2.setZone(zone);

        List<BirthdayEvent> events = new ArrayList<>();
        events.add(ev1);
        events.add(ev2);
        return events;
    }

    public static boolean hasPermission(Context context) {
        return PermissionsUtil.isPermissionGranted(context, PERMISSION);
    }

    public static void requestPermission(Fragment fragment) {
        fragment.requestPermissions(new String[]{PERMISSION}, 1);
    }
}
