package sun.util.resources;

import java.util.ListResourceBundle;

public final class CalendarData extends LocaleNamesBundle {
    protected final Object[][] getContents() {
        return new Object[][] {
            { "firstDayOfWeek", "1" },
            { "minimalDaysInFirstWeek", "1" },
        };
    }
}
