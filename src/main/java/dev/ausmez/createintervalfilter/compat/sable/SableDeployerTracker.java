package dev.ausmez.createintervalfilter.compat.sable;

import com.simibubi.create.content.kinetics.deployer.DeployerBlockEntity;
import dev.ausmez.createintervalfilter.deployer.IntervalLogic;
import dev.ausmez.createintervalfilter.deployer.IntervalState;
import dev.ausmez.createintervalfilter.item.ExecutionMode;
import dev.ausmez.createintervalfilter.item.IntervalFilterItem;
import dev.ryanhcode.sable.companion.SableCompanion;
import dev.ryanhcode.sable.companion.SubLevelAccess;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import java.util.WeakHashMap;

// Interval tracking for deployers on Sable sublevels by their target's world position
public final class SableDeployerTracker {

    // Movement beyond this between ticks is a teleport, not travel
    private static final double JUMP_DISTANCE = 8;
    private static final double MIN_TRAVEL = 1e-3;
    // Movement needed before forward direction is set
    private static final double MIN_ANCHOR_TRAVEL = 0.25;

    // Per-deployer interval state. Server thread only.
    private static final WeakHashMap<DeployerBlockEntity, IntervalState> STATES = new WeakHashMap<>();

    private SableDeployerTracker() {
    }

    // World block to place at this tick, or null.
    public static BlockPos tick(DeployerBlockEntity deployer, ItemStack filter, Direction facing) {
        SubLevelAccess subLevel = SableCompanion.INSTANCE.getContaining(deployer);
        if (subLevel == null) {
            STATES.remove(deployer);
            return null;
        }

        Vec3 current = subLevel.logicalPose().transformPosition(Vec3.atCenterOf(deployer.getBlockPos().relative(facing, 2)));
        IntervalState state = STATES.computeIfAbsent(deployer, d -> new IntervalState());
        Vec3 last = state.last;
        // First block entered after tracking starts is step 1
        if (last == null || current.distanceTo(last) > JUMP_DISTANCE) {
            state.reset();
            state.last = current;
            return null;
        }

        // Only horizontal travel counts
        Vec3 move = new Vec3(current.x - last.x, 0, current.z - last.z);
        double step = move.length();
        // Hold the last position so slow movement accumulates and parked jitter cancels out
        if (step < MIN_TRAVEL)
            return null;
        state.last = current;
        Vec3 localMove = subLevel.logicalPose().transformNormalInverse(move);

        if (!state.anchored) {
            // Sum movement in the vehicle's frame so a lurch when setting off doesn't decide forward
            state.heading = state.heading == null ? localMove : state.heading.add(localMove);
            Vec3 flatCurrent = new Vec3(current.x, last.y, current.z);
            if (state.heading.length() < MIN_ANCHOR_TRAVEL
                    || BlockPos.containing(flatCurrent).equals(BlockPos.containing(last)))
                return null;
            // Anchor at this crossing so later ones land near whole numbers
            state.anchored = true;
            state.heading = state.heading.normalize();
            state.distance = step * (1 - IntervalLogic.blockChangeFraction(last, flatCurrent));
        } else {
            // Moving against the heading winds the counter back
            double travel = localMove.dot(state.heading) < 0 ? -step : step;
            state.reversing = travel < 0;
            state.distance += travel;
        }

        // Forwards across boundary k enters step k + 1, reverse enters step k
        int count = state.reversing
                ? (int) Math.ceil(state.distance - 1e-6)
                : 1 + (int) Math.floor(state.distance + 1e-6);
        int stepFrom = state.count;
        if (count == stepFrom)
            return null;
        state.stepFrom = stepFrom;
        state.count = count;

        // Reversing only rewinds the counter; nothing is placed
        if (state.reversing) {
            state.pending = false;
            return null;
        }

        int interval = IntervalFilterItem.getInterval(filter);
        int offset = IntervalFilterItem.getOffset(filter);
        boolean isIntervalPosition = IntervalLogic.firstIntervalStep(stepFrom, count, interval, offset) != Integer.MIN_VALUE;
        if (!isIntervalPosition && IntervalLogic.inOffset(count, offset))
            return null;

        // Clear pending at each interval boundary
        if (isIntervalPosition)
            state.pending = false;
        if (!isIntervalPosition && !state.pending)
            return null;

        // BEST_EFFORT: if blocked, keep trying at following positions until the next boundary
        BlockPos target = BlockPos.containing(current);
        if (IntervalFilterItem.getExecutionMode(filter) == ExecutionMode.BEST_EFFORT
                && !deployer.getLevel().getBlockState(target).canBeReplaced()) {
            state.pending = true;
            return null;
        }

        state.pending = false;
        return target;
    }
}
