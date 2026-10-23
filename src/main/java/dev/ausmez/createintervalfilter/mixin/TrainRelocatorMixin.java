package dev.ausmez.createintervalfilter.mixin;

import com.simibubi.create.content.trains.entity.Carriage;
import com.simibubi.create.content.trains.entity.Train;
import com.simibubi.create.content.trains.entity.TrainRelocator;
import com.simibubi.create.content.trains.track.BezierTrackPointLocation;
import dev.ausmez.createintervalfilter.train.CarriageOdometer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(TrainRelocator.class)
public abstract class TrainRelocatorMixin {

    @Inject(method = "relocate", at = @At("RETURN"), remap = false)
    private static void cif$markCarriagesRelocated(Train train, Level level, BlockPos pos, BezierTrackPointLocation bezier,
                                                   boolean bezierDirection, Vec3 lookAngle, boolean simulate,
                                                   CallbackInfoReturnable<Boolean> cir) {
        if (simulate || !cir.getReturnValue())
            return;
        for (Carriage carriage : train.carriages)
            ((CarriageOdometer) carriage).cif$markRelocated();
    }
}
