package dev.ausmez.createintervalfilter.compat.emi;

import com.simibubi.create.compat.emi.GhostIngredientHandler;
import dev.ausmez.createintervalfilter.menu.IntervalFilterScreen;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;

public class IntervalFilterEmiPlugin implements EmiPlugin {

    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    public void register(EmiRegistry registry) {
        registry.addDragDropHandler(IntervalFilterScreen.class, new GhostIngredientHandler());
    }
}
