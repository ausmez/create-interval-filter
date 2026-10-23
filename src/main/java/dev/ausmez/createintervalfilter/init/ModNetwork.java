package dev.ausmez.createintervalfilter.init;

import dev.ausmez.createintervalfilter.CreateIntervalFilter;
import dev.ausmez.createintervalfilter.network.IntervalFilterPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class ModNetwork {
    private static final String PROTOCOL_VERSION = "1";
    private static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
            ResourceLocation.fromNamespaceAndPath(CreateIntervalFilter.MODID, "network"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );
    private static int pkt = 0;

    public static void registerCommonPackets() {
        registerServerboundMessage(IntervalFilterPacket.class, IntervalFilterPacket::encode, IntervalFilterPacket::decode, IntervalFilterPacket::handle);
    }

    public static <T> void sendToServer(T message) {
        INSTANCE.sendToServer(message);
    }

    private static <M> void registerServerboundMessage(Class<M> messageType, BiConsumer<M, FriendlyByteBuf> encoder,
                                                       Function<FriendlyByteBuf, M> decoder, BiConsumer<M, Supplier<NetworkEvent.Context>> handler) {
        INSTANCE.registerMessage(pkt++, messageType, encoder, decoder, handler, Optional.of(NetworkDirection.PLAY_TO_SERVER));
    }
}
