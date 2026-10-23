package dev.ausmez.createintervalfilter.init;

import dev.ausmez.createintervalfilter.CreateIntervalFilter;
import dev.ausmez.createintervalfilter.network.IntervalFilterPacket;
import me.pepperbell.simplenetworking.SimpleChannel;
import net.minecraft.resources.ResourceLocation;

public class ModNetwork {
    private static SimpleChannel channel;

    public static SimpleChannel getChannel() {
        return channel;
    }

    public static void registerServer() {
        channel = new SimpleChannel(new ResourceLocation(CreateIntervalFilter.MODID, "main"));
        channel.registerC2SPacket(IntervalFilterPacket.class, 0, IntervalFilterPacket::new);
        channel.initServerListener();
    }

    public static void registerClient() {
        channel.initClientListener();
    }
}
