package com.github.ekalin.kalendar.prefs;

import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.preference.Preference;

import java.util.Collections;

import com.github.ekalin.kalendar.R;
import com.github.ekalin.kalendar.task.TaskProvider;

public class TaskPreferencesFragment extends KalendarPreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {
    private static final String PREF_ACTIVE_TASK_LISTS_BUTTON = "activeTaskListsButton";
    private static final String KEY_PREF_GRANT_TASK_PERMISSION = "grantTaskPermission";

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        super.onCreatePreferences(savedInstanceState, rootKey);
        setPreferencesFromResource(R.xml.preferences_task, rootKey);
        setGrantPermissionVisibility();
        setTaskListState();
    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        switch (key) {
            case InstanceSettings.PREF_TASK_SOURCE:
                setGrantPermissionVisibility();
                setTaskListState();
                clearTasksLists();
                break;
        }
    }

    private void setGrantPermissionVisibility() {
        TaskProvider taskProvider = new TaskProvider(getActivity(), instanceSettings.getWidgetId(), instanceSettings);
        Preference preference = findPreference(KEY_PREF_GRANT_TASK_PERMISSION);
        preference.setVisible(!taskProvider.hasPermissionForSource(instanceSettings.getTaskSource()));
    }

    private void setTaskListState() {
        Preference taskListButton = findPreference(PREF_ACTIVE_TASK_LISTS_BUTTON);
        taskListButton.setEnabled(!instanceSettings.getTaskSource().equals(TaskProvider.PROVIDER_NONE));
    }

    private void clearTasksLists() {
        instanceSettings.setActiveTaskLists(Collections.emptySet());
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        switch (preference.getKey()) {
            case KEY_PREF_GRANT_TASK_PERMISSION:
                requestTaskPermission();
                return true;
        }

        return super.onPreferenceTreeClick(preference);
    }

    private void requestTaskPermission() {
        TaskProvider taskProvider = new TaskProvider(getActivity(), instanceSettings.getWidgetId(), instanceSettings);
        taskProvider.requestPermission(this, instanceSettings.getTaskSource());
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        setGrantPermissionVisibility();
    }
}
