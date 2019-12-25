package com.github.ekalin.kalendar.prefs;

import java.util.Collection;
import java.util.Set;

import com.github.ekalin.kalendar.calendar.CalendarEventProvider;

public class CalendarPreferencesFragment extends AbstractEventSourcesPreferencesFragment {
    @Override
    protected Set<String> fetchInitialActiveSources() {
        return instanceSettings.getActiveCalendars();
    }

    @Override
    protected Collection<EventSource> fetchAvailableSources() {
        return new CalendarEventProvider(getActivity(), instanceSettings.getWidgetId(), instanceSettings).getCalendars();
    }

    @Override
    protected void storeSelectedSources(Set<String> selectedSources) {
        instanceSettings.setActiveCalendars(selectedSources);
    }
}
