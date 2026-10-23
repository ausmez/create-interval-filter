package dev.ausmez.createintervalfilter;

import dev.ausmez.createintervalfilter.init.ModNetwork;
import net.fabricmc.api.ClientModInitializer;

public class CreateIntervalFilterClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ModNetwork.registerClient();
    }
}
