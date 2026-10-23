package dev.ausmez.createintervalfilter;

import com.simibubi.create.foundation.data.CreateRegistrate;
import com.simibubi.create.foundation.item.ItemDescription;
import com.tterrag.registrate.providers.ProviderType;
import dev.ausmez.createintervalfilter.init.ModCreativeModeTabs;
import dev.ausmez.createintervalfilter.init.ModItems;
import dev.ausmez.createintervalfilter.init.ModLangProvider;
import dev.ausmez.createintervalfilter.init.ModMenuTypes;
import dev.ausmez.createintervalfilter.init.ModNetwork;
import net.createmod.catnip.lang.FontHelper;
import net.fabricmc.api.ModInitializer;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;

public class CreateIntervalFilter implements ModInitializer {
	public static final String MODID = "createintervalfilter";
	public static final CreateRegistrate REGISTRATE = buildRegistrate();

	private static CreateRegistrate buildRegistrate() {
		return CreateRegistrate.create(MODID)
				.defaultCreativeTab((ResourceKey<CreativeModeTab>) null)
				.setTooltipModifierFactory(item -> new ItemDescription.Modifier(item, FontHelper.Palette.STANDARD_CREATE))
				.addDataGenerator(ProviderType.LANG, ModLangProvider::provide);
	}

	@Override
	public void onInitialize() {
		ModCreativeModeTabs.register();
		ModItems.register();
		ModMenuTypes.register();
		ModNetwork.registerServer();
		REGISTRATE.register();
	}
}
