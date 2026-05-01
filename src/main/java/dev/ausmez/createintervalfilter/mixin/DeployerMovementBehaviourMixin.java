package dev.ausmez.createintervalfilter.mixin;

import com.simibubi.create.content.contraptions.behaviour.MovementContext;
import com.simibubi.create.content.kinetics.deployer.DeployerFakePlayer;
import com.simibubi.create.content.kinetics.deployer.DeployerMovementBehaviour;
import com.simibubi.create.content.logistics.filter.FilterItemStack;
import dev.ausmez.createintervalfilter.item.ExecutionMode;
import dev.ausmez.createintervalfilter.item.IntervalFilterItem;
import dev.ausmez.createintervalfilter.item.PlacementMethod;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.WeakHashMap;

@Mixin(DeployerMovementBehaviour.class)
public abstract class DeployerMovementBehaviourMixin {

    @Shadow
    protected abstract DeployerFakePlayer getPlayer(MovementContext context);

    @Unique
    private static final String cif$COUNTER_KEY = "IntervalCount";
    @Unique
    private static final String cif$PENDING_KEY = "IntervalPending";
    @Unique
    private static final String cif$SLOT_KEY = "IntervalSlot";

    // Tracks which MovementContext instances have been initialized this session.
    @Unique
    private static final WeakHashMap<MovementContext, Boolean> cif$seenContexts = new WeakHashMap<>();

    @Inject(
            method = "tryGrabbingItem",
            at = @At("HEAD"),
            remap = false
    )
    private void cif$selectSlotForGrab(MovementContext context, CallbackInfo ci) {
        FilterItemStack filter = context.getFilterFromBE();
        if (!(filter.item().getItem() instanceof IntervalFilterItem intervalFilterItem))
            return;

        ItemStackHandler handler = intervalFilterItem.getItemStackHandler(filter.item());
        List<Integer> nonEmpty = new ArrayList<>();
        for (int i = 0; i < handler.getSlots(); i++)
            if (!handler.getStackInSlot(i).isEmpty()) nonEmpty.add(i);

        if (nonEmpty.size() <= 1) return;

        PlacementMethod method = IntervalFilterItem.getPlacementMethod(filter.item());
        int chosenIdx;
        if (method == PlacementMethod.SEQUENTIAL) {
            chosenIdx = context.data.getInt(cif$SLOT_KEY) % nonEmpty.size();
        } else {
            chosenIdx = context.world.random.nextInt(nonEmpty.size());
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
        if (!(filter.item().getItem() instanceof IntervalFilterItem intervalFilterItem))
            return;

        int interval = IntervalFilterItem.getInterval(filter.item());
        int offset = IntervalFilterItem.getOffset(filter.item());
        int count = context.data.getInt(cif$COUNTER_KEY) + 1;
        context.data.putInt(cif$COUNTER_KEY, count);

        if (count <= offset) {
            ci.cancel();
            return;
        }

        int adjustedCount = count - offset;
        boolean isIntervalPosition = (adjustedCount - 1) % interval == 0;

        // Always reset pending at each interval boundary so the prior cycle doesn't bleed over
        if (isIntervalPosition)
            context.data.remove(cif$PENDING_KEY);

        boolean pending = context.data.getBoolean(cif$PENDING_KEY);

        if (!isIntervalPosition && !pending) {
            ci.cancel();
            return;
        }

        // For BEST_EFFORT, check whether the block at this position can be placed on.
        // If blocked, mark pending so the next position also gets a try.
        // On the next interval boundary the pending flag is cleared and the cycle restarts.
        ExecutionMode execMode = IntervalFilterItem.getExecutionMode(filter.item());
        if (execMode == ExecutionMode.BEST_EFFORT && !context.world.getBlockState(pos).canBeReplaced()) {
            context.data.putBoolean(cif$PENDING_KEY, true);
            ci.cancel();
            return;
        }

        context.data.remove(cif$PENDING_KEY);

        // Advance the sequential slot counter now that we know we're activating.
        ItemStackHandler handler = intervalFilterItem.getItemStackHandler(filter.item());
        List<Integer> nonEmpty = new ArrayList<>();
        for (int i = 0; i < handler.getSlots(); i++)
            if (!handler.getStackInSlot(i).isEmpty()) nonEmpty.add(i);

        if (nonEmpty.size() > 1 && IntervalFilterItem.getPlacementMethod(filter.item()) == PlacementMethod.SEQUENTIAL) {
            int current = context.data.getInt(cif$SLOT_KEY);
            context.data.putInt(cif$SLOT_KEY, (current + 1) % nonEmpty.size());
        }
    }

    @Inject(
            method = "visitNewPosition",
            at = @At("RETURN"),
            remap = false
    )
    private void cif$clearActiveSlot(MovementContext context, BlockPos pos, CallbackInfo ci) {
        IntervalFilterItem.ACTIVE_SLOT_INDEX.remove();
    }

    @Inject(
            method = "visitNewPosition",
            at = @At("HEAD"),
            remap = false
    )
    private void cif$resetCounterOnNewContext(MovementContext context, BlockPos pos, CallbackInfo ci) {
        FilterItemStack filter = context.getFilterFromBE();
        if (!(filter.item().getItem() instanceof IntervalFilterItem))
            return;
        if (cif$seenContexts.containsKey(context))
            return;
        cif$seenContexts.put(context, Boolean.TRUE);
        context.data.remove(cif$COUNTER_KEY);
        context.data.remove(cif$PENDING_KEY);
        context.data.remove(cif$SLOT_KEY);
    }
}
