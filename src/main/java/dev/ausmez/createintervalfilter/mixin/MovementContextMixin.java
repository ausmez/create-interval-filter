package dev.ausmez.createintervalfilter.mixin;

import com.simibubi.create.content.contraptions.behaviour.MovementContext;
import dev.ausmez.createintervalfilter.deployer.IntervalState;
import dev.ausmez.createintervalfilter.deployer.IntervalStateHolder;
import net.minecraft.nbt.CompoundTag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MovementContext.class)
public abstract class MovementContextMixin implements IntervalStateHolder {

    @Shadow(remap = false)
    public CompoundTag data;

    @Unique
    private IntervalState cif$intervalState;

    // Save the state with the contraption so the pattern continues after a reload or an unloaded stretch
    @Inject(method = "writeToNBT", at = @At("HEAD"), remap = false)
    private void cif$saveIntervalState(CompoundTag nbt, CallbackInfoReturnable<CompoundTag> cir) {
        if (cif$intervalState != null)
            data.put(IntervalState.NBT_KEY, cif$intervalState.write());
    }

    @Override
    public IntervalState cif$getIntervalState() {
        return cif$intervalState;
    }

    @Override
    public void cif$setIntervalState(IntervalState state) {
        cif$intervalState = state;
    }
}
