package dev.ausmez.createintervalfilter.item;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

public enum PlacementMethod implements StringRepresentable {
    SEQUENTIAL("sequential"),
    RANDOM("random");

    private final String name;

    PlacementMethod(String name) {
        this.name = name;
    }

    @Override
    public @NotNull String getSerializedName() {
        return name;
    }

    public static final Codec<PlacementMethod> CODEC = StringRepresentable.fromEnum(PlacementMethod::values);
    public static final StreamCodec<ByteBuf, PlacementMethod> STREAM_CODEC =
            ByteBufCodecs.idMapper(id -> PlacementMethod.values()[id], Enum::ordinal);
}
