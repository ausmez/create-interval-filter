package dev.ausmez.createintervalfilter.init;

import com.simibubi.create.AllCreativeModeTabs;
import com.simibubi.create.AllItems;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;

public class ModCreativeModeTabs {
    public static void register() {
        ItemGroupEvents.modifyEntriesEvent(AllCreativeModeTabs.BASE_CREATIVE_TAB.key())
                .register(entries -> entries.addAfter(
                        AllItems.PACKAGE_FILTER,
                        ModItems.INTERVAL_FILTER_ITEM.asItem()
                ));
    }
}
