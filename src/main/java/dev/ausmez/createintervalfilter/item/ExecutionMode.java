package dev.ausmez.createintervalfilter.item;

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
}
