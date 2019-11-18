package org.andstatus.todoagenda.widget;

import org.andstatus.todoagenda.R;
import org.andstatus.todoagenda.prefs.InstanceSettings;
import org.andstatus.todoagenda.util.DateUtil;
import org.andstatus.todoagenda.util.Optional;
import org.andstatus.todoagenda.util.PermissionsUtil;
import org.joda.time.DateTime;

import java.util.List;

/**
 * @author yvolk@yurivolkov.com
 */
public class LastEntry extends WidgetEntry {
    public static Optional<LastEntry> from(InstanceSettings settings, List<WidgetEntry> widgetEntries) {
        return widgetEntries.isEmpty()
                ? Optional.of(new LastEntry(
                PermissionsUtil.arePermissionsGranted(settings.getContext())
                        ? LastEntryType.EMPTY
                        : LastEntryType.NO_PERMISSIONS,
                DateUtil.now(settings.getTimeZone())))
                : Optional.empty();
    }

    public enum LastEntryType {
        NOT_LOADED(R.layout.item_not_loaded),
        NO_PERMISSIONS(R.layout.item_no_permissions),
        EMPTY(R.layout.item_empty_list);

        private final int layoutId;

        LastEntryType(int layoutId) {
            this.layoutId = layoutId;
        }

        public int getLayoutId() {
            return layoutId;
        }
    }

    private final LastEntryType type;

    public LastEntry(LastEntryType type, DateTime date) {
        super(40);
        this.type = type;
        super.setStartDate(date);
    }

    public LastEntryType getType() {
        return type;
    }
}
