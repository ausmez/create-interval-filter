package dev.ausmez.createintervalfilter.mixin;

import com.simibubi.create.content.kinetics.base.DirectionalKineticBlock;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.content.kinetics.deployer.DeployerBlockEntity;
import com.simibubi.create.content.kinetics.deployer.DeployerFakePlayer;
import com.simibubi.create.foundation.blockEntity.behaviour.filtering.FilteringBehaviour;
import dev.ausmez.createintervalfilter.CreateIntervalFilter;
import dev.ausmez.createintervalfilter.compat.sable.SableDeployerTracker;
import dev.ausmez.createintervalfilter.deployer.DeployerAnimation;
import dev.ausmez.createintervalfilter.item.IntervalFilterItem;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// Applied after Sable's mixin, so our world target for sub-level deployers replaces the one it picks
@Mixin(value = DeployerBlockEntity.class, priority = 1500)
public abstract class DeployerBlockEntityMixin extends KineticBlockEntity {

    @Shadow
    protected FilteringBehaviour filtering;
    @Shadow
    protected DeployerFakePlayer player;
    @Shadow
    protected int timer;
    @Shadow
    protected float reach;

    // Arm timer units per tick while unpowered; a retract takes 1000
    @Unique
    private static final int cif$UNPOWERED_TIMER_SPEED = 100;
    // Full reach into the block two in front, as start() computes when nothing is in the way
    @Unique
    private static final float cif$REACH = 1.25f;

    // World block the current sub-level activation should place at, passed to cif$useWorldTarget
    @Unique
    private static final ThreadLocal<BlockPos> cif$TARGET_POS = new ThreadLocal<>();

    @Shadow
    protected abstract void activate();

    public DeployerBlockEntityMixin(BlockEntityType<?> typeIn, BlockPos pos, BlockState state) {
        super(typeIn, pos, state);
    }

    // Interval deployers never fire on Create's own timer; contraptions and Sable sub-levels trigger them by distance
    @Inject(method = "start", at = @At("HEAD"), cancellable = true)
    private void cif$preventActivationOutsideContraption(CallbackInfo ci) {
        if (filtering.getFilter().getItem() instanceof IntervalFilterItem && !isVirtual()) {
            ci.cancel();
        }
    }

    // Create only advances the arm while powered, so give unpowered interval deployers a speed of their own
    @Inject(method = "getTimerSpeed", at = @At("RETURN"), cancellable = true)
    private void cif$unpoweredTimerSpeed(CallbackInfoReturnable<Integer> cir) {
        if (cir.getReturnValueI() == 0 && filtering != null && filtering.getFilter().getItem() instanceof IntervalFilterItem)
            cir.setReturnValue(cif$UNPOWERED_TIMER_SPEED);
    }

    // Runs before Create's speed check, so a deployer on a moving vehicle works without rotation, like on a contraption
    @Inject(method = "tick", at = @At("HEAD"))
    private void cif$activateOnSubLevel(CallbackInfo ci) {
        if (!CreateIntervalFilter.SABLE_LOADED || level == null)
            return;
        ItemStack filter = filtering.getFilter();
        if (!(filter.getItem() instanceof IntervalFilterItem))
            return;
        // Create's tick stops here when unpowered, so retract the arm ourselves (on both sides)
        if (getSpeed() == 0 && timer > 0)
            timer -= cif$UNPOWERED_TIMER_SPEED;
        if (level.isClientSide || player == null)
            return;
        BlockPos target = SableDeployerTracker.tick((DeployerBlockEntity) (Object) this, filter,
                getBlockState().getValue(DirectionalKineticBlock.FACING));
        if (target == null)
            return;

        ItemStack held = player.getMainHandItem();
        if (held.isEmpty() || !filtering.test(held))
            return;
        cif$TARGET_POS.set(target);
        try {
            activate();
        } finally {
            cif$TARGET_POS.remove();
        }

        // Snap the arm out and let it retract. start() normally sets the reach, but it never runs here.
        if (DeployerAnimation.retract((DeployerBlockEntity) (Object) this)) {
            reach = cif$REACH;
            timer = 1000;
            sendData();
        }
    }

    // Sable only redirects the click into the world when something solid is at or beside the target, and may pick
    // the vehicle's own plot instead, so use the world block found by the tracker
    @ModifyArg(
            method = "activate",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/simibubi/create/content/kinetics/deployer/DeployerHandler;activate(Lcom/simibubi/create/content/kinetics/deployer/DeployerFakePlayer;Lnet/minecraft/world/phys/Vec3;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/phys/Vec3;Lcom/simibubi/create/content/kinetics/deployer/DeployerBlockEntity$Mode;)V"
            ),
            index = 2
    )
    private BlockPos cif$useWorldTarget(BlockPos clickedPos) {
        BlockPos target = cif$TARGET_POS.get();
        return target != null ? target : clickedPos;
    }
}
