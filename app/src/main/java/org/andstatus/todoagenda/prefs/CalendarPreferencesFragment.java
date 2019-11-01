package org.andstatus.todoagenda.prefs;

import org.andstatus.todoagenda.calendar.CalendarEventProvider;

import java.util.Collection;
import java.util.Set;

public class CalendarPreferencesFragment extends AbstractEventSourcesPreferencesFragment {
    @Override
    protected Set<String> fetchInitialActiveSources() {
        return instanceSettings.getActiveCalendars();
    }

    @Override
    protected Collection<EventSource> fetchAvailableSources() {
        return new CalendarEventProvider(getActivity(), instanceSettings.getWidgetId()).getCalendars();
    }

    @Override
    protected void storeSelectedSources(Set<String> selectedSources) {
        instanceSettings.setActiveCalendars(selectedSources);
    }
}
