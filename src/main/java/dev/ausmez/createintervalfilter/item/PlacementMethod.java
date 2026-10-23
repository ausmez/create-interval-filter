package dev.ausmez.createintervalfilter.item;

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
}
