package dev.ausmez.createintervalfilter.deployer;

// Implemented on Create's MovementContext via MovementContextMixin
public interface IntervalStateHolder {
    // Null until first used
    IntervalState cif$getIntervalState();

    void cif$setIntervalState(IntervalState state);
}
