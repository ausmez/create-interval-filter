package dev.ausmez.createintervalfilter.train;

// Implemented on Create's Carriage via CarriageMixin
public interface CarriageOdometer {
    // Signed track distance traveled, positive towards the train's front
    double cif$getOdometer();

    // Incremented on each wrench relocation
    int cif$getRelocations();

    void cif$markRelocated();
}
