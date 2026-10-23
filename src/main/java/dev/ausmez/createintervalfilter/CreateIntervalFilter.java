package dev.ausmez.createintervalfilter;

import com.simibubi.create.AllCreativeModeTabs;
import com.simibubi.create.AllItems;
import com.simibubi.create.foundation.data.CreateRegistrate;
import com.simibubi.create.foundation.item.ItemDescription;
import com.tterrag.registrate.providers.ProviderType;
import dev.ausmez.createintervalfilter.init.ModDataComponents;
import dev.ausmez.createintervalfilter.init.ModItems;
import dev.ausmez.createintervalfilter.init.ModLangProvider;
import dev.ausmez.createintervalfilter.init.ModMenuTypes;
import dev.ausmez.createintervalfilter.network.IntervalFilterPacket;
import net.createmod.catnip.lang.FontHelper;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

@Mod(CreateIntervalFilter.MODID)
public class CreateIntervalFilter {
    public static final String MODID = "createintervalfilter";
    public static final CreateRegistrate REGISTRATE = buildRegistrate();
    public static final boolean SABLE_LOADED = ModList.get().isLoaded("sable");

    public CreateIntervalFilter(IEventBus modEventBus, ModContainer modContainer) {
        ModDataComponents.register(modEventBus);
        ModMenuTypes.register();
        ModItems.register();

        modEventBus.addListener(this::registerPayloads);
        modEventBus.addListener(this::onBuildCreativeTab);
        REGISTRATE.registerEventListeners(modEventBus);
    }

    private static CreateRegistrate buildRegistrate() {
        return CreateRegistrate.create(MODID)
                .defaultCreativeTab((ResourceKey<CreativeModeTab>) null)
                .setTooltipModifierFactory(item -> new ItemDescription.Modifier(item, FontHelper.Palette.STANDARD_CREATE))
                .addDataGenerator(ProviderType.LANG, ModLangProvider::provide);
    }

    private void registerPayloads(final RegisterPayloadHandlersEvent event) {
        event.registrar("1").playToServer(
                IntervalFilterPacket.TYPE,
                IntervalFilterPacket.STREAM_CODEC,
                IntervalFilterPacket::handle
        );
    }

    private void onBuildCreativeTab(final BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == AllCreativeModeTabs.BASE_CREATIVE_TAB.getKey()) {
            event.insertAfter(
                    AllItems.PACKAGE_FILTER.asStack(),
                    ModItems.INTERVAL_FILTER_ITEM.asStack(),
                    CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS
            );
        }
    }
}
