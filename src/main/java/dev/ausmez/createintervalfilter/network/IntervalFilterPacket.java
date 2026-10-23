package dev.ausmez.createintervalfilter.network;

import dev.ausmez.createintervalfilter.item.ExecutionMode;
import dev.ausmez.createintervalfilter.item.PlacementMethod;
import dev.ausmez.createintervalfilter.menu.IntervalFilterMenu;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record IntervalFilterPacket(Option option, int value) {
    public static void encode(IntervalFilterPacket packet, FriendlyByteBuf buffer) {
        buffer.writeEnum(packet.option);
        buffer.writeVarInt(packet.value);
    }

    public static IntervalFilterPacket decode(FriendlyByteBuf buffer) {
        Option option = buffer.readEnum(Option.class);
        int value = buffer.readVarInt();
        return new IntervalFilterPacket(option, value);
    }

    public static void handle(IntervalFilterPacket packet, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            ServerPlayer player = context.get().getSender();
            if (player == null) return;

            if (!(player.containerMenu instanceof IntervalFilterMenu menu)) return;

            switch (packet.option()) {
                case RESPECT_DATA -> menu.respectNBT = true;
                case IGNORE_DATA -> menu.respectNBT = false;
                case SET_INTERVAL -> menu.interval = Mth.clamp(packet.value(), 1, 64);
                case SET_OFFSET -> menu.offset = Mth.clamp(packet.value(), 0, 16);
                case SET_EXECUTION_MODE ->
                        menu.executionMode = ExecutionMode.values()[Mth.clamp(packet.value(), 0, ExecutionMode.values().length - 1)];
                case SET_PLACEMENT_METHOD ->
                        menu.placementMethod = PlacementMethod.values()[Mth.clamp(packet.value(), 0, PlacementMethod.values().length - 1)];
            }
        });
        context.get().setPacketHandled(true);
    }

    public enum Option {
        RESPECT_DATA, IGNORE_DATA, SET_INTERVAL, SET_OFFSET, SET_EXECUTION_MODE, SET_PLACEMENT_METHOD
    }
}
