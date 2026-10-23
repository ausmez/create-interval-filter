package dev.ausmez.createintervalfilter.deployer;

import com.simibubi.create.content.kinetics.deployer.DeployerBlockEntity;

import java.lang.reflect.Field;

// Sets the deployer arm state reflectively, as DeployerBlockEntity.State is package-private
public final class DeployerAnimation {

    private static final Field STATE;
    private static final Object RETRACTING;

    static {
        Field field = null;
        Object retracting = null;
        try {
            field = DeployerBlockEntity.class.getDeclaredField("state");
            field.setAccessible(true);
            for (Object constant : field.getType().getEnumConstants())
                if (((Enum<?>) constant).name().equals("RETRACTING"))
                    retracting = constant;
        } catch (ReflectiveOperationException | RuntimeException e) {
            field = null;
        }
        STATE = retracting != null ? field : null;
        RETRACTING = retracting;
    }

    private DeployerAnimation() {
    }

    // Returns whether it succeeded
    public static boolean retract(DeployerBlockEntity deployer) {
        if (STATE == null)
            return false;
        try {
            STATE.set(deployer, RETRACTING);
            return true;
        } catch (IllegalAccessException e) {
            return false;
        }
    }
}
