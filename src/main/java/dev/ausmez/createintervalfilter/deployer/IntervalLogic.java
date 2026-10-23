package dev.ausmez.createintervalfilter.deployer;

import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

public final class IntervalLogic {

    private IntervalLogic() {
    }

    // The offset only delays the first placement ahead of the start
    public static boolean isIntervalStep(int step, int interval, int offset) {
        return (step < 1 || step > offset) && Math.floorMod(step - offset - 1, interval) == 0;
    }

    // First interval step in (from, to], or Integer.MIN_VALUE
    public static int firstIntervalStep(int from, int to, int interval, int offset) {
        for (int c = from + 1; c <= to; c++)
            if (isIntervalStep(c, interval, offset))
                return c;
        return Integer.MIN_VALUE;
    }

    // Within the offset after the start, where nothing is placed
    public static boolean inOffset(int step, int offset) {
        return step >= 1 && step <= offset;
    }

    // Fraction of the move at which a new block is entered
    public static double blockChangeFraction(Vec3 from, Vec3 to) {
        BlockPos start = BlockPos.containing(from);
        double t = Math.min(axisCrossing(from.x, to.x, start.getX()),
                Math.min(axisCrossing(from.y, to.y, start.getY()), axisCrossing(from.z, to.z, start.getZ())));
        return Math.min(t, 1);
    }

    // Fraction of the move at which the coordinate leaves the block, or infinity if it doesn't move
    private static double axisCrossing(double from, double to, int block) {
        double d = to - from;
        if (d > 0)
            return (block + 1 - from) / d;
        if (d < 0)
            return (block - from) / d;
        return Double.POSITIVE_INFINITY;
    }
}
