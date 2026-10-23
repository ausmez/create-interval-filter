package dev.ausmez.createintervalfilter.mixin;

import dev.ausmez.createintervalfilter.item.IntervalFilterItem;
import com.simibubi.create.content.kinetics.deployer.DeployerBlockEntity;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.filtering.FilteringBehaviour;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FilteringBehaviour.class)
public abstract class FilteringBehaviourMixin extends BlockEntityBehaviour {

    public FilteringBehaviourMixin(SmartBlockEntity be) {
        super(be);
    }

    @Inject(method = "setFilter(Lnet/minecraft/world/item/ItemStack;)Z", at = @At("HEAD"), cancellable = true, remap = false)

    private void cif$restrictFilter(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (stack.getItem() instanceof IntervalFilterItem) {
            if (!(this.blockEntity instanceof DeployerBlockEntity))
                cir.setReturnValue(false);
        }
    }
}
