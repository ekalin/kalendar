package org.andstatus.todoagenda.prefs;

import android.os.Bundle;

import org.andstatus.todoagenda.task.TaskProvider;

import java.util.Collection;
import java.util.Set;

public class TaskListsPreferencesFragment extends AbstractEventSourcesPreferencesFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected Set<String> fetchInitialActiveSources() {
        return instanceSettings.getActiveTaskLists();
    }

    @Override
    protected Collection<EventSource> fetchAvailableSources() {
        return new TaskProvider(getActivity(), instanceSettings.getWidgetId(), instanceSettings).getTaskLists(instanceSettings.getTaskSource());
    }

    @Override
    protected void storeSelectedSources(Set<String> selectedSources) {
        instanceSettings.setActiveTaskLists(selectedSources);
    }
}
