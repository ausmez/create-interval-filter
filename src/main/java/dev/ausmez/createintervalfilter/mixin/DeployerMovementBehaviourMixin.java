package dev.ausmez.createintervalfilter.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.simibubi.create.content.contraptions.OrientedContraptionEntity;
import com.simibubi.create.content.contraptions.behaviour.MovementContext;
import com.simibubi.create.content.kinetics.deployer.DeployerFakePlayer;
import com.simibubi.create.content.kinetics.deployer.DeployerMovementBehaviour;
import com.simibubi.create.content.logistics.filter.FilterItemStack;
import com.simibubi.create.content.trains.entity.CarriageContraptionEntity;
import dev.ausmez.createintervalfilter.deployer.IntervalLogic;
import dev.ausmez.createintervalfilter.deployer.IntervalState;
import dev.ausmez.createintervalfilter.deployer.IntervalStateHolder;
import dev.ausmez.createintervalfilter.item.ExecutionMode;
import dev.ausmez.createintervalfilter.item.IntervalFilterItem;
import dev.ausmez.createintervalfilter.item.PlacementMethod;
import dev.ausmez.createintervalfilter.train.CarriageOdometer;
import net.createmod.catnip.math.VecHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.Tag;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DeployerMovementBehaviour.class)
public abstract class DeployerMovementBehaviourMixin {

    @Shadow
    protected abstract DeployerFakePlayer getPlayer(MovementContext context);

    // Movement beyond this between visits is a teleport, not travel.
    @Unique
    private static final double cif$JUMP_DISTANCE = 8;
    @Unique
    private static final double cif$MIN_ANCHOR_TRAVEL = 0.05;
    @Unique
    private static final double cif$MAX_LOOKAHEAD = 1;

    // Target block for the current activation, passed to cif$activateAtStepBlock.
    @Unique
    private static final ThreadLocal<BlockPos> cif$TARGET_POS = new ThreadLocal<>();

    @Inject(
            method = "tryGrabbingItem",
            at = @At("HEAD"),
            remap = false
    )
    private void cif$selectSlotForGrab(MovementContext context, CallbackInfo ci) {
        // Cancelling visitNewPosition skips cif$clearActiveSlot, so never trust a slot left by an earlier visit
        IntervalFilterItem.ACTIVE_SLOT_INDEX.remove();
        FilterItemStack filter = context.getFilterFromBE();
        if (!(filter.item().getItem() instanceof IntervalFilterItem intervalFilterItem))
            return;

        ItemStackHandler handler = intervalFilterItem.getItemStackHandler(filter.item());
        int nonEmpty = 0;
        for (int i = 0; i < handler.getSlots(); i++)
            if (!handler.getStackInSlot(i).isEmpty()) nonEmpty++;

        if (nonEmpty <= 1) return;

        PlacementMethod method = IntervalFilterItem.getPlacementMethod(filter.item());
        int chosenIdx;
        if (method == PlacementMethod.SEQUENTIAL) {
            // Pick from the interval cycle so each grid point gets the same item in either direction
            int interval = IntervalFilterItem.getInterval(filter.item());
            int offset = IntervalFilterItem.getOffset(filter.item());
            int cycle = Math.floorDiv(cif$getState(context).count - offset - 1, interval);
            chosenIdx = Math.floorMod(cycle, nonEmpty);
        } else {
            chosenIdx = context.world.random.nextInt(nonEmpty);
        }
        IntervalFilterItem.ACTIVE_SLOT_INDEX.set(chosenIdx);

        DeployerFakePlayer player = getPlayer(context);
        ItemStack currentHand = player.getMainHandItem();
        if (!currentHand.isEmpty() && !filter.test(context.world, currentHand)) {
            ItemStack remainder = ItemHandlerHelper.insertItem(context.contraption.getStorage().getAllItems(), currentHand, false);
            player.setItemInHand(InteractionHand.MAIN_HAND, remainder);
        }
    }

    @Inject(
            method = "visitNewPosition",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/simibubi/create/content/kinetics/deployer/DeployerMovementBehaviour;activate(Lcom/simibubi/create/content/contraptions/behaviour/MovementContext;Lnet/minecraft/core/BlockPos;Lcom/simibubi/create/content/kinetics/deployer/DeployerFakePlayer;Lcom/simibubi/create/content/kinetics/deployer/DeployerBlockEntity$Mode;)V"
            ),
            cancellable = true,
            remap = false
    )
    private void cif$checkIntervalBeforeActivate(MovementContext context, BlockPos pos, CallbackInfo ci) {
        FilterItemStack filter = context.getFilterFromBE();
        if (!(filter.item().getItem() instanceof IntervalFilterItem))
            return;

        IntervalState state = cif$getState(context);
        int interval = IntervalFilterItem.getInterval(filter.item());
        int offset = IntervalFilterItem.getOffset(filter.item());
        cif$TARGET_POS.remove();
        // Not a new step unless cif$trackDistance advanced the counter (e.g. not just a Y crossing on a slope)
        int count = state.count;
        int stepFrom = state.stepFrom;
        if (count == stepFrom) {
            cif$cancel(ci);
            return;
        }

        // Reversing only rewinds the counter; nothing is placed
        if (state.reversing) {
            state.pending = false;
            cif$cancel(ci);
            return;
        }

        // Fast contraptions can skip blocks, so check every step covered. The offset only delays the first
        // placement ahead of the start.
        int gridStep = IntervalLogic.firstIntervalStep(stepFrom, count, interval, offset);
        boolean isIntervalPosition = gridStep != Integer.MIN_VALUE;
        if (!isIntervalPosition && IntervalLogic.inOffset(count, offset)) {
            cif$cancel(ci);
            return;
        }

        // Clear pending at each interval boundary
        if (isIntervalPosition)
            state.pending = false;

        if (!isIntervalPosition && !state.pending) {
            cif$cancel(ci);
            return;
        }

        // BEST_EFFORT: if blocked, keep trying at following positions until the next boundary
        BlockPos target = isIntervalPosition ? cif$findStepBlock(context, state, gridStep, pos) : pos;
        ExecutionMode execMode = IntervalFilterItem.getExecutionMode(filter.item());
        if (execMode == ExecutionMode.BEST_EFFORT && !context.world.getBlockState(target).canBeReplaced()) {
            state.pending = true;
            cif$cancel(ci);
            return;
        }

        state.pending = false;
        cif$TARGET_POS.set(target);
    }

    // Cancelling skips cif$clearActiveSlot at RETURN, so clear the thread-locals here
    @Unique
    private static void cif$cancel(CallbackInfo ci) {
        IntervalFilterItem.ACTIVE_SLOT_INDEX.remove();
        cif$TARGET_POS.remove();
        ci.cancel();
    }

    @WrapOperation(
            method = "visitNewPosition",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/simibubi/create/content/kinetics/deployer/DeployerMovementBehaviour;activate(Lcom/simibubi/create/content/contraptions/behaviour/MovementContext;Lnet/minecraft/core/BlockPos;Lcom/simibubi/create/content/kinetics/deployer/DeployerFakePlayer;Lcom/simibubi/create/content/kinetics/deployer/DeployerBlockEntity$Mode;)V"
            ),
            remap = false
    )
    // Mode is package-private in DeployerBlockEntity, hence the coerced Object
    private void cif$activateAtStepBlock(DeployerMovementBehaviour instance, MovementContext context, BlockPos pos,
                                          DeployerFakePlayer player, @Coerce Object mode, Operation<Void> original) {
        BlockPos target = cif$TARGET_POS.get();
        cif$TARGET_POS.remove();
        original.call(instance, context, target != null ? target : pos, player, mode);
    }

    // Returns the block the deployer is in halfway through the step. The first block entered can be beside the
    // track on bends, so interpolate along the deployer's movement instead, ahead or back as needed.
    @Unique
    private static BlockPos cif$findStepBlock(MovementContext context, IntervalState state, int gridStep, BlockPos pos) {
        if (state.prev == null)
            return pos;
        double d0 = state.prevDistance;
        double d1 = state.distance;
        if (d1 <= d0)
            return pos;

        Vec3 from = state.prev;
        Vec3 to = context.position;
        double f = Math.max(0, (gridStep - 0.5 - d0) / (d1 - d0));
        // Track travel can be far smaller than the deployer's movement while turning, so cap the lookahead
        double length = from.distanceTo(to);
        if (length > 1e-9)
            f = Math.min(f, 1 + cif$MAX_LOOKAHEAD / length);
        return BlockPos.containing(from.lerp(to, f));
    }

    @Inject(
            method = "visitNewPosition",
            at = @At("RETURN"),
            remap = false
    )
    private void cif$clearActiveSlot(MovementContext context, BlockPos pos, CallbackInfo ci) {
        IntervalFilterItem.ACTIVE_SLOT_INDEX.remove();
        cif$TARGET_POS.remove();
    }

    @Inject(
            method = "visitNewPosition",
            at = @At("HEAD"),
            remap = false
    )
    private void cif$trackDistance(MovementContext context, BlockPos pos, CallbackInfo ci) {
        if (context.world.isClientSide || context.position == null)
            return;
        FilterItemStack filter = context.getFilterFromBE();
        if (!(filter.item().getItem() instanceof IntervalFilterItem))
            return;
        IntervalState state = cif$getState(context);

        Vec3 current = context.position;
        CarriageOdometer odometer = cif$getOdometer(context);
        // Usually already handled by cif$resetOnRelocation
        cif$checkRelocated(state, odometer);
        int count = state.count;
        state.prev = null;

        if (state.last != null) {
            Vec3 last = state.last;
            double dx = current.x - last.x;
            double dy = current.y - last.y;
            double dz = current.z - last.z;
            double step = Math.sqrt(dx * dx + dy * dy + dz * dz);

            // Trains: count horizontal travel along the track, ignoring turning, so deployers on both sides stay
            // level through bends. Positive towards the train's front. Other contraptions use the deployer's own
            // movement, positive in the first direction moved.
            double travel = step;
            boolean onTrack = odometer != null && state.lastAnchor != null;
            boolean jumped = step > cif$JUMP_DISTANCE;
            if (onTrack && jumped) {
                // A train only jumps by moving while unloaded or through a portal (relocation is handled
                // separately), so the track distance is still accurate
                travel = odometer.cif$getOdometer() - state.lastOdometer;
            } else if (onTrack) {
                Vec3 anchor = context.contraption.entity.position();
                double ax = anchor.x - state.lastAnchor.x;
                double az = anchor.z - state.lastAnchor.z;
                double anchorStep = Math.sqrt(ax * ax + az * az);
                double trackTravel = odometer.cif$getOdometer() - state.lastOdometer;

                // Remove the movement caused by turning (Create rotates by -yaw around Y)
                double offsetX = last.x - state.lastAnchor.x;
                double offsetZ = last.z - state.lastAnchor.z;
                float yawChange = Mth.wrapDegrees(cif$getYaw(context) - state.lastYaw);
                Vec3 turnedOffset = VecHelper.rotate(new Vec3(offsetX, 0, offsetZ), -yawChange, Direction.Axis.Y);
                double alongX = dx - (turnedOffset.x - offsetX);
                double alongZ = dz - (turnedOffset.z - offsetZ);

                // Tiny along-track changes are carriage spacing corrections, not travel
                travel = anchorStep < 1e-6 || Math.abs(trackTravel) < 1e-3
                        ? 0
                        : Math.signum(trackTravel) * Math.abs(alongX * ax + alongZ * az) / anchorStep;
            } else if (state.heading != null && !jumped) {
                if (cif$toContraptionFrame(context, current.subtract(last)).dot(state.heading) < 0)
                    travel = -step;
            }

            if (jumped && (!onTrack || !state.anchored)) {
                // Teleported, or a train jumped before its pattern started: restart the cycle
                state.reset();
                count = 0;
            } else if (!state.anchored && Math.abs(travel) < cif$MIN_ANCHOR_TRAVEL) {
                // No real travel yet (e.g. carriage snapping onto its bogeys), so wait before anchoring
                state.stepFrom = count;
            } else if (!state.anchored) {
                // Anchor distance at the first real boundary crossing so later ones land near whole numbers.
                // Boundary k sits between steps k and k + 1; add how far the deployer is already past it.
                boolean reversing = travel < 0;
                state.anchored = true;
                state.reversing = reversing;
                if (!onTrack)
                    state.heading = cif$toContraptionFrame(context, current.subtract(last)).normalize();
                double overshoot = Math.abs(travel) * (1 - IntervalLogic.blockChangeFraction(last, current));
                state.distance = reversing ? -overshoot : 1 + overshoot;
                state.stepFrom = count;
                count = reversing ? 0 : 2;
            } else {
                // No track movement (e.g. tilting) keeps the previous direction
                if (travel != 0)
                    state.reversing = travel < 0;
                double distance = state.distance + travel;
                state.prev = last;
                state.prevDistance = state.distance;
                state.distance = distance;
                state.stepFrom = count;
                // Forwards across boundary k enters step k + 1, reverse enters step k. Count boundaries passed
                // rather than rounding, since a fast deployer can be well past one.
                count = state.reversing
                        ? (int) Math.ceil(distance - 1e-6)
                        : 1 + (int) Math.floor(distance + 1e-6);
                if (jumped) {
                    // Nothing was placed along the gap, so carry on from here without placing for it
                    state.prev = null;
                    state.stepFrom = count;
                    state.pending = false;
                }
            }
        }

        if (state.last == null) {
            // First visit since assembly/reset counts as step 1
            state.stepFrom = 0;
            count = 1;
        }

        state.count = count;
        state.last = current;
        if (odometer != null) {
            state.lastOdometer = odometer.cif$getOdometer();
            state.lastAnchor = context.contraption.entity.position();
            state.relocations = odometer.cif$getRelocations();
            state.lastYaw = cif$getYaw(context);
        }
    }

    // Runs every tick so a wrench relocation resets the cycle even before the deployer enters a new block.
    @Inject(
            method = "tick",
            at = @At("HEAD"),
            remap = false
    )
    private void cif$resetOnRelocation(MovementContext context, CallbackInfo ci) {
        if (context.world.isClientSide)
            return;
        if (!(context.getFilterFromBE().item().getItem() instanceof IntervalFilterItem))
            return;
        if (cif$checkRelocated(cif$getState(context), cif$getOdometer(context)))
            context.firstMovement = true;
    }

    // A contraption loaded from a save continues its cycle; a newly assembled one starts fresh, taking the
    // current relocation count so earlier relocations aren't handled again.
    @Unique
    private static IntervalState cif$getState(MovementContext context) {
        IntervalStateHolder holder = (IntervalStateHolder) context;
        IntervalState state = holder.cif$getIntervalState();
        if (state != null)
            return state;
        // Drop state saved to NBT by 1.0.0
        context.data.getAllKeys().removeIf(key -> key.startsWith("Interval"));
        if (context.data.contains(IntervalState.NBT_KEY, Tag.TAG_COMPOUND)) {
            state = IntervalState.read(context.data.getCompound(IntervalState.NBT_KEY));
        } else {
            state = new IntervalState();
            CarriageOdometer odometer = cif$getOdometer(context);
            if (odometer != null)
                state.relocations = odometer.cif$getRelocations();
        }
        holder.cif$setIntervalState(state);
        return state;
    }

    // Resets the cycle if the train was wrench-relocated since the last check. Returns whether it did.
    @Unique
    private static boolean cif$checkRelocated(IntervalState state, CarriageOdometer odometer) {
        if (odometer == null || state.relocations == odometer.cif$getRelocations())
            return false;
        state.reset();
        state.relocations = odometer.cif$getRelocations();
        return true;
    }

    // Rotates a world-space movement into the contraption's own frame, so turning or spinning isn't seen as
    // reversing
    @Unique
    private static Vec3 cif$toContraptionFrame(MovementContext context, Vec3 movement) {
        return context.contraption.entity.reverseRotation(movement, 1);
    }

    @Unique
    private static float cif$getYaw(MovementContext context) {
        return context.contraption.entity instanceof OrientedContraptionEntity oce ? oce.yaw : 0;
    }

    @Unique
    private static CarriageOdometer cif$getOdometer(MovementContext context) {
        if (context.contraption.entity instanceof CarriageContraptionEntity cce && cce.getCarriage() != null)
            return (CarriageOdometer) cce.getCarriage();
        return null;
    }
}
