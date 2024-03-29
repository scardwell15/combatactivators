package activators.drones

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.util.Misc
import org.lazywizard.lazylib.MathUtils

class HoveringFormation : DroneFormation() {
    override fun advance(ship: ShipAPI, drones: Map<ShipAPI, PIDController>, amount: Float) {
        val angleIncrease = 360f / drones.size

        drones.onEachIndexed { index, (drone, controller) ->
            var shipLoc = ship.location
            var angle = angleIncrease * (index - 1)
            var point = MathUtils.getPointOnCircumference(shipLoc, ship.collisionRadius * 1.5f, angle)
            controller.move(point, drone)

            var iter = Global.getCombatEngine().shipGrid.getCheckIterator(drone.location, 1000f, 1000f)

            var target: ShipAPI? = null
            var distance = 100000f
            for (it in iter) {
                if (it is ShipAPI) {
                    if (it.isFighter) continue
                    if (Global.getCombatEngine().getFleetManager(it.owner).owner == Global.getCombatEngine()
                            .getFleetManager(drone.owner).owner
                    ) continue
                    if (it.isHulk) continue
                    var distanceBetween = MathUtils.getDistance(it, ship)
                    if (distance > distanceBetween) {
                        distance = distanceBetween
                        target = it
                    }
                }
            }

            if (target != null) {
                controller.rotate(Misc.getAngleInDegrees(drone.location, target.location), drone)
            } else {
                controller.rotate(ship.facing + MathUtils.getRandomNumberInRange(-10f, 10f), drone)
            }
        }
    }
}