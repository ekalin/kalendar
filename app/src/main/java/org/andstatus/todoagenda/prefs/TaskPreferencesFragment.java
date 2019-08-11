package org.andstatus.todoagenda.prefs;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;

import org.andstatus.todoagenda.EventAppWidgetProvider;
import org.andstatus.todoagenda.R;
import org.andstatus.todoagenda.task.TaskProvider;

import java.util.Collections;

public class TaskPreferencesFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

    private static final String PREF_ACTIVE_TASK_LISTS_BUTTON = "activeTaskListsButton";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences_task);
        setGrantPermissionVisibility(false);
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
            case ApplicationPreferences.PREF_TASK_SOURCE:
                showTaskSource();
                setGrantPermissionVisibility(true);
                setTaskListState();
                clearTasksLists();
                break;
        }
    }

    private void showTaskSource() {
        ListPreference preference = (ListPreference) findPreference(ApplicationPreferences.PREF_TASK_SOURCE);
        preference.setSummary(preference.getEntry());
    }

    private void setGrantPermissionVisibility(boolean forceDisplay) {
        TaskProvider taskProvider = new TaskProvider(getActivity(), ApplicationPreferences.getWidgetId(getActivity()));
        if (taskProvider.hasPermission()) {
            Preference preference = findPreference("grantTaskPermission");
            if (preference != null) {
                PreferenceScreen screen = getPreferenceScreen();
                screen.removePreference(preference);
            }
        } else {
            if (forceDisplay) {
                getActivity().recreate();
            }
        }
    }

    private void setTaskListState() {
        Preference taskListButton = findPreference(PREF_ACTIVE_TASK_LISTS_BUTTON);
        taskListButton.setEnabled(!ApplicationPreferences.getTaskSource(getActivity()).equals(TaskProvider.PROVIDER_NONE));
    }

    private void clearTasksLists() {
        ApplicationPreferences.setActiveTaskLists(getActivity(), Collections.<String>emptySet());
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        switch (preference.getKey()) {
            case ApplicationPreferences.KEY_PREF_GRANT_TASK_PERMISSION:
                requestTaskPermission();
                return true;
        }

        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    private void requestTaskPermission() {
        TaskProvider taskProvider = new TaskProvider(getActivity(), ApplicationPreferences.getWidgetId(getActivity()));
        taskProvider.requestPermission(getActivity());
    }

    public void gotPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        setGrantPermissionVisibility(false);
    }
}
