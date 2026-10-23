package dev.ausmez.createintervalfilter.mixin;

import com.simibubi.create.content.trains.entity.Carriage;
import com.simibubi.create.content.trains.entity.TravellingPoint;
import com.simibubi.create.content.trains.graph.DimensionPalette;
import com.simibubi.create.content.trains.graph.TrackGraph;
import dev.ausmez.createintervalfilter.train.CarriageOdometer;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Carriage.class)
public abstract class CarriageMixin implements CarriageOdometer {

    @Unique
    private double cif$odometer;
    @Unique
    private int cif$relocations;

    // travel() returns the signed distance moved along the track this tick
    @Inject(method = "travel", at = @At("RETURN"), remap = false)
    private void cif$accumulateOdometer(Level level, TrackGraph graph, double distance, TravellingPoint toFollowForward,
                                        TravellingPoint toFollowBackward, int type, CallbackInfoReturnable<Double> cir) {
        cif$odometer += cir.getReturnValue();
    }

    // Saved with the train so deployer state saved with the contraption still lines up after a reload
    @Inject(method = "write", at = @At("RETURN"), remap = false)
    private void cif$writeOdometer(DimensionPalette dimensions, HolderLookup.Provider registries,
                                   CallbackInfoReturnable<CompoundTag> cir) {
        CompoundTag tag = cir.getReturnValue();
        tag.putDouble("CreateIntervalFilterOdometer", cif$odometer);
        tag.putInt("CreateIntervalFilterRelocations", cif$relocations);
    }

    @Inject(method = "read", at = @At("RETURN"), remap = false)
    private static void cif$readOdometer(CompoundTag tag, HolderLookup.Provider registries, TrackGraph graph,
                                         DimensionPalette dimensions, CallbackInfoReturnable<Carriage> cir) {
        CarriageMixin carriage = (CarriageMixin) (Object) cir.getReturnValue();
        carriage.cif$odometer = tag.getDouble("CreateIntervalFilterOdometer");
        carriage.cif$relocations = tag.getInt("CreateIntervalFilterRelocations");
    }

    @Override
    public double cif$getOdometer() {
        return cif$odometer;
    }

    @Override
    public int cif$getRelocations() {
        return cif$relocations;
    }

    @Override
    public void cif$markRelocated() {
        cif$relocations++;
    }
}
