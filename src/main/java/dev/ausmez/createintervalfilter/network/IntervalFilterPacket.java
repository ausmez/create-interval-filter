package dev.ausmez.createintervalfilter.network;

import com.simibubi.create.foundation.networking.SimplePacketBase;
import dev.ausmez.createintervalfilter.item.ExecutionMode;
import dev.ausmez.createintervalfilter.item.PlacementMethod;
import dev.ausmez.createintervalfilter.menu.IntervalFilterMenu;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;

public class IntervalFilterPacket extends SimplePacketBase {
    private final Option option;
    private final int value;

    public IntervalFilterPacket(Option option, int value) {
        this.option = option;
        this.value = value;
    }

    public IntervalFilterPacket(FriendlyByteBuf buf) {
        option = Option.values()[buf.readInt()];
        value = buf.readInt();
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeInt(option.ordinal());
        buf.writeInt(value);
    }

    @Override
    public boolean handle(Context context) {
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null || !(player.containerMenu instanceof IntervalFilterMenu menu)) return;
            switch (option) {
                case RESPECT_DATA -> menu.respectNBT = true;
                case IGNORE_DATA -> menu.respectNBT = false;
                case SET_INTERVAL -> menu.interval = Mth.clamp(value, 1, 64);
                case SET_OFFSET -> menu.offset = Mth.clamp(value, 0, 16);
                case SET_EXECUTION_MODE ->
                        menu.executionMode = ExecutionMode.values()[Mth.clamp(value, 0, ExecutionMode.values().length - 1)];
                case SET_PLACEMENT_METHOD ->
                        menu.placementMethod = PlacementMethod.values()[Mth.clamp(value, 0, PlacementMethod.values().length - 1)];
            }
        });
        return true;
    }

    public enum Option {
        RESPECT_DATA, IGNORE_DATA, SET_INTERVAL, SET_OFFSET, SET_EXECUTION_MODE, SET_PLACEMENT_METHOD
    }
}
