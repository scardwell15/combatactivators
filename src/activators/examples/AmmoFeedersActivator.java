package activators.examples;

import activators.CombatActivator;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.util.Misc;

public class AmmoFeedersActivator extends CombatActivator {
    public AmmoFeedersActivator(ShipAPI ship) {
        super(ship);
    }

    @Override
    public float getBaseActiveDuration() {
        return 5f;
    }

    @Override
    public float getBaseCooldownDuration() {
        return 20f;
    }

    @Override
    public boolean shouldActivateAI(float amount) {
        ShipAPI target = ship.getShipTarget();
        if (target != null) {
            float score = 0f;

            if (target.getFluxTracker().isOverloadedOrVenting()) {
                score += 9f;
            } else {
                score += target.getFluxLevel() * 6f;
            }

            float dist = Misc.getDistance(ship.getLocation(), target.getLocation());
            if (dist > aiData.getEngagementRange()) {
                score -= 3f;
            } else {
                score += 3f;
            }

            float avgRange = aiData.getAverageWeaponRange(false);
            score += Math.min(avgRange / dist, 8f);

            return score > 10f;
        }

        return false;
    }

    @Override
    public void onActivate() {
        stats.getBallisticWeaponFluxCostMod().modifyMult(this.getDisplayText(), 0.5f);
        stats.getBallisticRoFMult().modifyMult(this.getDisplayText(), 0.5f);
    }

    @Override
    public String getDisplayText() {
        return "Ammo Feeders";
    }
}
