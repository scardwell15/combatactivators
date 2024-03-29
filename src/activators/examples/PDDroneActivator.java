package activators.examples;

import activators.drones.DroneActivator;
import com.fs.starfarer.api.combat.ShipAPI;

/**
 * Spawns a PD drone. Has no usable key and doesn't take a key index.
 */
public class PDDroneActivator extends DroneActivator {
    public PDDroneActivator(ShipAPI ship) {
        super(ship);
    }

    @Override
    public boolean canAssignKey() {
        return false;
    }

    @Override
    public float getBaseActiveDuration() {
        return 0;
    }

    @Override
    public float getBaseCooldownDuration() {
        return 0;
    }

    @Override
    public boolean shouldActivateAI(float amount) {
        return canActivate();
    }

    @Override
    public float getBaseChargeRechargeDuration() {
        return 10f;
    }

    @Override
    public boolean canActivate() {
        return false;
    }

    @Override
    public String getDisplayText() {
        return "PD Drones";
    }

    @Override
    public String getStateText() {
        return "";
    }

    @Override
    public float getBarFill() {
        float fill = 0f;
        if (charges < getMaxCharges()) {
            fill = chargeInterval.getElapsed() / chargeInterval.getIntervalDuration();
        }

        return fill;
    }

    @Override
    public int getMaxCharges() {
        return 2;
    }

    @Override
    public int getMaxDeployedDrones() {
        return 2;
    }

    @Override
    public boolean usesChargesOnActivate() {
        return false;
    }

    @Override
    public String getDroneVariant() {
        return "drone_pd_example";
    }
}
