package dev.ausmez.createintervalfilter.item;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

public enum ExecutionMode implements StringRepresentable {
    STRICT("strict"),
    BEST_EFFORT("best_effort");

    private final String name;

    ExecutionMode(String name) {
        this.name = name;
    }

    @Override
    public @NotNull String getSerializedName() {
        return name;
    }

    public static final Codec<ExecutionMode> CODEC = StringRepresentable.fromEnum(ExecutionMode::values);
    public static final StreamCodec<ByteBuf, ExecutionMode> STREAM_CODEC =
            ByteBufCodecs.idMapper(id -> ExecutionMode.values()[id], Enum::ordinal);
}
