package org.andstatus.todoagenda.prefs;

import android.appwidget.AppWidgetManager;
import android.content.Intent;

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
        Intent intent = getActivity().getIntent();
        int widgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, 0);
        return new CalendarEventProvider(getActivity(), widgetId).getCalendars();
    }

    @Override
    protected void storeSelectedSources(Set<String> selectedSources) {
        instanceSettings.setActiveCalendars(selectedSources);
    }
}
