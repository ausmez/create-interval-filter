package dev.ausmez.createintervalfilter.deployer;

import net.createmod.catnip.math.VecHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.phys.Vec3;

public class IntervalState {

    // Key in MovementContext.data. Must not start with "Interval", which marks 1.0.0 state.
    public static final String NBT_KEY = "CreateIntervalFilter";

    // Current step, and the step at the previous visit
    public int count;
    public int stepFrom;
    public boolean pending;
    // Distance from the first grid crossing
    public double distance;
    public boolean anchored;
    public boolean reversing;
    // Non-train contraptions: the first direction moved, in the contraption's own frame; null until anchored
    public Vec3 heading;

    // Values at the previous visit; last is null before the first
    public Vec3 last;
    public Vec3 lastAnchor;
    public double lastOdometer;
    public float lastYaw;
    // Kept across reset() so each relocation is handled once
    public int relocations;

    // Previous position and distance, for finding the step block; prev is null if unavailable
    public Vec3 prev;
    public double prevDistance;

    // Restarts the cycle
    public void reset() {
        count = 0;
        stepFrom = 0;
        pending = false;
        distance = 0;
        anchored = false;
        reversing = false;
        heading = null;
        last = null;
        lastAnchor = null;
        lastOdometer = 0;
        lastYaw = 0;
        prev = null;
        prevDistance = 0;
    }

    public CompoundTag write() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("Count", count);
        tag.putBoolean("Pending", pending);
        tag.putDouble("Distance", distance);
        tag.putBoolean("Anchored", anchored);
        tag.putBoolean("Reversing", reversing);
        if (last != null)
            tag.put("Last", VecHelper.writeNBT(last));
        if (lastAnchor != null)
            tag.put("LastAnchor", VecHelper.writeNBT(lastAnchor));
        if (heading != null)
            tag.put("Heading", VecHelper.writeNBT(heading));
        tag.putDouble("LastOdometer", lastOdometer);
        tag.putFloat("LastYaw", lastYaw);
        tag.putInt("Relocations", relocations);
        return tag;
    }

    public static IntervalState read(CompoundTag tag) {
        IntervalState state = new IntervalState();
        state.count = tag.getInt("Count");
        // Loading isn't a new step
        state.stepFrom = state.count;
        state.pending = tag.getBoolean("Pending");
        state.distance = tag.getDouble("Distance");
        state.anchored = tag.getBoolean("Anchored");
        state.reversing = tag.getBoolean("Reversing");
        if (tag.contains("Last"))
            state.last = VecHelper.readNBT(tag.getList("Last", Tag.TAG_DOUBLE));
        if (tag.contains("LastAnchor"))
            state.lastAnchor = VecHelper.readNBT(tag.getList("LastAnchor", Tag.TAG_DOUBLE));
        if (tag.contains("Heading"))
            state.heading = VecHelper.readNBT(tag.getList("Heading", Tag.TAG_DOUBLE));
        state.lastOdometer = tag.getDouble("LastOdometer");
        state.lastYaw = tag.getFloat("LastYaw");
        state.relocations = tag.getInt("Relocations");
        return state;
    }
}
