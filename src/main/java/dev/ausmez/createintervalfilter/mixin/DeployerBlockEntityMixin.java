package dev.ausmez.createintervalfilter.mixin;

import dev.ausmez.createintervalfilter.item.IntervalFilterItem;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.content.kinetics.deployer.DeployerBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.filtering.FilteringBehaviour;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DeployerBlockEntity.class)
public abstract class DeployerBlockEntityMixin extends KineticBlockEntity {

    @Shadow(remap = false)
    protected FilteringBehaviour filtering;

    public DeployerBlockEntityMixin(BlockEntityType<?> typeIn, BlockPos pos, BlockState state) {
        super(typeIn, pos, state);
    }

    @Inject(method = "start", at = @At("HEAD"), cancellable = true, remap = false)
    private void cif$preventActivationOutsideContraption(CallbackInfo ci) {
        if (filtering.getFilter().getItem() instanceof IntervalFilterItem && !isVirtual()) {
            ci.cancel();
        }
    }
}
