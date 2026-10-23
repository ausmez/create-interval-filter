package dev.ausmez.createintervalfilter;

import com.simibubi.create.AllCreativeModeTabs;
import com.simibubi.create.AllItems;
import com.simibubi.create.foundation.data.CreateRegistrate;
import com.simibubi.create.foundation.item.ItemDescription;
import com.tterrag.registrate.providers.ProviderType;
import dev.ausmez.createintervalfilter.init.*;
import net.createmod.catnip.lang.FontHelper;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(CreateIntervalFilter.MODID)
public class CreateIntervalFilter {
    public static final String MODID = "createintervalfilter";
    public static final CreateRegistrate REGISTRATE = buildRegistrate();

    public CreateIntervalFilter(final FMLJavaModLoadingContext context) {
        IEventBus modEventBus = context.getModEventBus();

        ModMenuTypes.register();
        ModItems.register();

        modEventBus.addListener(this::onCommonSetup);
        modEventBus.addListener(this::onBuildCreativeTab);
        REGISTRATE.registerEventListeners(modEventBus);
    }

    private static CreateRegistrate buildRegistrate() {
        return CreateRegistrate.create(MODID)
                .defaultCreativeTab((ResourceKey<CreativeModeTab>) null)
                .setTooltipModifierFactory(item -> new ItemDescription.Modifier(item, FontHelper.Palette.STANDARD_CREATE))
                .addDataGenerator(ProviderType.LANG, ModLangProvider::provide);
    }

    private void onCommonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(ModNetwork::registerCommonPackets);
    }

    private void onBuildCreativeTab(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == AllCreativeModeTabs.BASE_CREATIVE_TAB.getKey()) {
            event.getEntries().putAfter(
                    AllItems.PACKAGE_FILTER.asStack(),
                    ModItems.INTERVAL_FILTER_ITEM.asStack(),
                    CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS
            );
        }
    }
}
