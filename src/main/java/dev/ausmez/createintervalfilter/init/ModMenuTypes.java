package dev.ausmez.createintervalfilter.init;

import dev.ausmez.createintervalfilter.CreateIntervalFilter;
import dev.ausmez.createintervalfilter.menu.IntervalFilterMenu;
import dev.ausmez.createintervalfilter.menu.IntervalFilterScreen;
import com.tterrag.registrate.util.entry.MenuEntry;

public class ModMenuTypes {
    public static final MenuEntry<IntervalFilterMenu> INTERVAL_FILTER =
            CreateIntervalFilter.REGISTRATE.menu("interval_filter",
                    IntervalFilterMenu::new,
                    () -> IntervalFilterScreen::new
            ).register();

    public static void register() {}
}
