package activators.examples;

import activators.drones.DroneActivator;
import activators.drones.HoveringFormation;
import activators.drones.SpinningCircleFormation;
import com.fs.starfarer.api.combat.ShipAPI;

/**
 * Spawns two PD drones. The drones are on a separate charge interval than the activator itself. Activation speeds
 * up the formation's spin speed for a short time, and has two charges.
 */
public class SeparateChargePDDroneActivator extends DroneActivator {
    public SeparateChargePDDroneActivator(ShipAPI ship) {
        super(ship);
    }

    @Override
    public float getBaseActiveDuration() {
        return 5f;
    }

    @Override
    public float getBaseCooldownDuration() {
        return 2f;
    }

    @Override
    public float getBaseChargeRechargeDuration() {
        return 10f;
    }

    @Override
    public boolean hasCharges() {
        return true;
    }

    @Override
    public int getMaxCharges() {
        return 2;
    }

    @Override
    public boolean hasSeparateDroneCharges() {
        return true;
    }

    @Override
    public int getMaxDeployedDrones() {
        return 2;
    }

    @Override
    public float getDroneCreationTime() {
        return 8f;
    }

    @Override
    public int getMaxDroneCharges() {
        return 4;
    }

    @Override
    public boolean shouldActivateAI(float amount) {
        return false;
    }

    @Override
    public String getDisplayText() {
        return "Super PD Drones";
    }

    @Override
    public String getDroneVariant() {
        return "drone_pd_example";
    }

    @Override
    public void onActivate() {
        ((SpinningCircleFormation) getFormation()).setRotationSpeed(4f);
    }

    @Override
    public void onFinished() {
        ((SpinningCircleFormation) getFormation()).setRotationSpeed(0.2f);
    }
}
