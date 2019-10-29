package org.andstatus.todoagenda.prefs;

import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.preference.ListPreference;
import androidx.preference.Preference;

import org.andstatus.todoagenda.R;
import org.andstatus.todoagenda.task.TaskProvider;

import java.util.Collections;

public class TaskPreferencesFragment extends KalendarPreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {
    private static final String PREF_ACTIVE_TASK_LISTS_BUTTON = "activeTaskListsButton";

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
        showTaskSource();
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
                showTaskSource();
                setGrantPermissionVisibility();
                setTaskListState();
                clearTasksLists();
                break;
        }
    }

    private void showTaskSource() {
        ListPreference preference = findPreference(InstanceSettings.PREF_TASK_SOURCE);
        preference.setSummary(preference.getEntry());
    }

    private void setGrantPermissionVisibility() {
        TaskProvider taskProvider = new TaskProvider(getActivity(), instanceSettings.getWidgetId());
        Preference preference = findPreference("grantTaskPermission");
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
            case InstanceSettings.KEY_PREF_GRANT_TASK_PERMISSION:
                requestTaskPermission();
                return true;
        }

        return super.onPreferenceTreeClick(preference);
    }

    private void requestTaskPermission() {
        TaskProvider taskProvider = new TaskProvider(getActivity(), instanceSettings.getWidgetId());
        taskProvider.requestPermission(this, instanceSettings.getTaskSource());
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        setGrantPermissionVisibility();
    }
}
