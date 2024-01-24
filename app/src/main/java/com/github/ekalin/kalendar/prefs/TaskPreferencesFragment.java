package com.github.ekalin.kalendar.prefs;

import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.preference.Preference;

import com.github.ekalin.kalendar.KalendarUpdater;
import com.github.ekalin.kalendar.R;
import com.github.ekalin.kalendar.task.TaskProvider;
import com.github.ekalin.kalendar.util.PackageManagerUtil;

import java.util.Collections;
import java.util.Optional;

public class TaskPreferencesFragment extends KalendarPreferenceFragment
        implements SharedPreferences.OnSharedPreferenceChangeListener, PermissionRequester {
    private static final String PREF_ACTIVE_TASK_LISTS_BUTTON = "activeTaskListsButton";
    private static final String KEY_PREF_GRANT_TASK_PERMISSION = "grantTaskPermission";
    private static final String KEY_APP_NOT_INSTALLED = "taskAppNotInstalled";

    private final ActivityResultLauncher<String> requestPermissionLauncher;

    public TaskPreferencesFragment() {
        requestPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        onPermissionGranted();
                    }
                });
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        super.onCreatePreferences(savedInstanceState, rootKey);
        setPreferencesFromResource(R.xml.preferences_task, rootKey);
        updateState();
    }

    @Override
    public void onResume() {
        super.onResume();
        updateState();
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
                updateState();
                clearTasksLists();
                break;
        }
    }

    private void updateState() {
        setInstalledVisibility();
        setGrantPermissionVisibility();
        setTaskListState();
    }

    private void setInstalledVisibility() {
        TaskProvider taskProvider = new TaskProvider(getActivity(), instanceSettings.getWidgetId(), instanceSettings);
        Preference preference = findPreference(KEY_APP_NOT_INSTALLED);
        if (taskProvider.isInstalled(instanceSettings.getTaskSource())) {
            preference.setVisible(false);
        } else {
            preference.setVisible(true);
            Optional<String> nonInstallableReason =
                    taskProvider.getNonInstallableReason(getActivity(), instanceSettings.getTaskSource());
            nonInstallableReason.ifPresent(reason -> {
                preference.setEnabled(false);
                preference.setSummary(reason);
            });
            if (!nonInstallableReason.isPresent()) {
                preference.setSummary(R.string.task_app_install);
            }
        }
    }

    private void setGrantPermissionVisibility() {
        Preference preference = findPreference(KEY_PREF_GRANT_TASK_PERMISSION);
        String selectedTaskSource = instanceSettings.getTaskSource();

        TaskProvider taskProvider = new TaskProvider(getActivity(), instanceSettings.getWidgetId(), instanceSettings);
        preference.setVisible(taskProvider.isInstalled(selectedTaskSource) && !taskProvider.hasPermissionForSource(selectedTaskSource));
    }

    private void setTaskListState() {
        String selectedTaskSource = instanceSettings.getTaskSource();
        Preference taskListButton = findPreference(PREF_ACTIVE_TASK_LISTS_BUTTON);

        TaskProvider taskProvider = new TaskProvider(getActivity(), instanceSettings.getWidgetId(), instanceSettings);
        taskListButton.setVisible(!selectedTaskSource.equals(TaskProvider.PROVIDER_NONE)
                && taskProvider.isInstalled(selectedTaskSource)
                && taskProvider.hasPermissionForSource(selectedTaskSource));
        taskListButton.setEnabled(!selectedTaskSource.equals(TaskProvider.PROVIDER_NONE));
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
            case KEY_APP_NOT_INSTALLED:
                installTaskApp();
                return true;
        }

        return super.onPreferenceTreeClick(preference);
    }

    private void requestTaskPermission() {
        TaskProvider taskProvider = new TaskProvider(getActivity(), instanceSettings.getWidgetId(), instanceSettings);
        taskProvider.requestPermission(this, instanceSettings.getTaskSource());
    }

    @Override
    public void requestPermission(String permission) {
        requestPermissionLauncher.launch(permission);
    }

    private void onPermissionGranted() {
        setGrantPermissionVisibility();
        setTaskListState();
        KalendarUpdater.registerReceivers(getActivity(), true);
    }

    private void installTaskApp() {
        TaskProvider taskProvider = new TaskProvider(getActivity(), instanceSettings.getWidgetId(), instanceSettings);
        PackageManagerUtil.goToAppOnMarket(getActivity(), taskProvider.getAppPackage(instanceSettings.getTaskSource()));
    }
}
