package dev.ausmez.createintervalfilter.network;

import dev.ausmez.createintervalfilter.CreateIntervalFilter;
import dev.ausmez.createintervalfilter.item.ExecutionMode;
import dev.ausmez.createintervalfilter.item.PlacementMethod;
import dev.ausmez.createintervalfilter.menu.IntervalFilterMenu;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public record IntervalFilterPacket(Option option, int value) implements CustomPacketPayload {
    public static final Type<IntervalFilterPacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(CreateIntervalFilter.MODID, "interval_filter_screen")
    );
    public static final StreamCodec<ByteBuf, IntervalFilterPacket> STREAM_CODEC =
            StreamCodec.composite(
                    Option.STREAM_CODEC, IntervalFilterPacket::option,
                    ByteBufCodecs.VAR_INT, IntervalFilterPacket::value,
                    IntervalFilterPacket::new
            );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(IntervalFilterPacket packet, IPayloadContext context) {
        if (!(context.player().containerMenu instanceof IntervalFilterMenu menu))
            return;
        switch (packet.option()) {
            case RESPECT_DATA -> menu.respectNBT = true;
            case IGNORE_DATA -> menu.respectNBT = false;
            case SET_INTERVAL -> menu.interval = Math.clamp(packet.value(), 1, 64);
            case SET_OFFSET -> menu.offset = Math.clamp(packet.value(), 0, 16);
            case SET_EXECUTION_MODE -> menu.executionMode = ExecutionMode.values()[Math.clamp(packet.value(), 0, ExecutionMode.values().length - 1)];
            case SET_PLACEMENT_METHOD -> menu.placementMethod = PlacementMethod.values()[Math.clamp(packet.value(), 0, PlacementMethod.values().length - 1)];
        }
    }

    public enum Option {
        RESPECT_DATA, IGNORE_DATA, SET_INTERVAL, SET_OFFSET, SET_EXECUTION_MODE, SET_PLACEMENT_METHOD;

        public static final StreamCodec<ByteBuf, Option> STREAM_CODEC =
                ByteBufCodecs.idMapper(id -> Option.values()[id], Enum::ordinal);
    }
}
