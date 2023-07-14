package activators.drones

import activators.CombatActivator
import activators.MagicLibRendering
import activators.MagicLibRendering.UIscaling
import activators.advanceAndCheckElapsed
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.DamageType
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.ShipCommand
import com.fs.starfarer.api.combat.ViewportAPI
import com.fs.starfarer.api.fleet.FleetMemberType
import com.fs.starfarer.api.mission.FleetSide
import com.fs.starfarer.api.util.IntervalUtil
import com.fs.starfarer.api.util.Misc
import org.lazywizard.lazylib.combat.CombatUtils
import org.lazywizard.lazylib.ui.LazyFont
import org.lwjgl.util.vector.Vector2f

abstract class DroneActivator(ship: ShipAPI) : CombatActivator(ship) {
    var activeWings: MutableMap<ShipAPI, PIDController> = LinkedHashMap()
    var formation: DroneFormation = SpinningCircleFormation()
    var droneCreationInterval: IntervalUtil = IntervalUtil(0f, 0f)
        get() = if (hasSeparateDroneCharges()) field else chargeInterval
        set(value) = if (hasSeparateDroneCharges()) field = value else chargeInterval = value
    var droneCharges: Int = 0
        get() = if (hasSeparateDroneCharges()) field else charges
        set(value) = if (hasSeparateDroneCharges()) field = value else charges = value

    val droneDeployInterval: IntervalUtil = IntervalUtil(0f, 0f)

    override fun init() {
        super.init()

        formation = getDroneFormation()
        droneDeployInterval.setInterval(getDroneDeployDelay(), getDroneDeployDelay())

        if (hasSeparateDroneCharges()) {
            droneCreationInterval.setInterval(getDroneCreationTime(), getDroneCreationTime())
            droneCharges = getMaxDroneCharges()
        }
    }

    /**
     * Formation object that controls drones. The two implemented by this library are HoveringFormation and
     * SpinningCircleFormation. To create your own, you should extend the DroneFormation class.
     */
    open fun getDroneFormation(): DroneFormation {
        return SpinningCircleFormation()
    }

    /**
     * How many drones can be deployed at once.
     */
    open fun getMaxDeployedDrones(): Int {
        return droneCharges
    }

    /**
     * This is useful if you want a charged activator to use while allowing drones to have their own set of charges.
     */
    open fun hasSeparateDroneCharges(): Boolean {
        return false
    }

    /**
     * Maximum amount of stored drone charges. This is only used if the hasSeparateDroneCharges method returns true.
     */
    open fun getMaxDroneCharges(): Int {
        return 0
    }

    /**
     * Time to store a single drone charge. This is only used if the hasSeparateDroneCharges method returns true.
     */
    open fun getDroneCreationTime(): Float {
        return 0f
    }

    /**
     * Delay between subsequent drone deployments if multiple need to be deployed at once.
     */
    open fun getDroneDeployDelay(): Float {
        return 0.1f
    }

    open fun generatesDroneChargesWhileShipIsDead(): Boolean {
        return false
    }

    /**
     * All drones will explode when the ship dies.
     */
    open fun dronesExplodeWhenShipDies(): Boolean {
        return true
    }

    /**
     * Drones will deploy when ship is dead only if dronesExplodeWhenShipDies returns false.
     * Otherwise, they would explode in the next frame.
     */
    open fun dronesDeployWhenShipIsDead(): Boolean {
        return false
    }

    /**
     * Drones will still advance while the ship is dead no matter what.
     */
    override fun getAdvancesWhileDead(): Boolean {
        return super.getAdvancesWhileDead()
    }

    abstract fun getDroneVariant(): String

    open fun spawnDrone(): ShipAPI {
        Global.getCombatEngine().getFleetManager(ship.owner).isSuppressDeploymentMessages = true
        val fleetSide = FleetSide.values()[ship.owner]
        val fighter = CombatUtils.spawnShipOrWingDirectly(
            getDroneVariant(),
            FleetMemberType.FIGHTER_WING,
            fleetSide,
            0.7f,
            ship.location,
            ship.facing
        )
        activeWings[fighter] = getPIDController()
        fighter.shipAI = null
        fighter.giveCommand(ShipCommand.SELECT_GROUP, null, 99)
        Global.getCombatEngine().getFleetManager(ship.owner).isSuppressDeploymentMessages = false

        return fighter
    }

    open fun getPIDController(): PIDController {
        return PIDController(2f, 2f, 6f, 0.5f)
    }

    private val droneWeaponGroupCheckInterval = IntervalUtil(0.5f, 1f)
    override fun advanceInternal(amount: Float) {
        super.advanceInternal(amount)

        val alive = ship.isAlive && !ship.isHulk && ship.owner != 100
        if (!alive) {
            if (dronesExplodeWhenShipDies()) {
                activeWings.forEach { (drone, _) ->
                    var damageFrom: Vector2f? = Vector2f(drone.location)
                    damageFrom = Misc.getPointWithinRadius(damageFrom, 20f)
                    Global.getCombatEngine().applyDamage(drone, damageFrom, 1000000f, DamageType.ENERGY, 0f, true, false, drone, false)
                }
            }
        }

        if (hasSeparateDroneCharges() && (alive || generatesDroneChargesWhileShipIsDead())) {
            if (droneCharges < getMaxDroneCharges()) {
                if (droneCreationInterval.intervalElapsed()) {
                    droneCharges++
                    droneCreationInterval.advance(0f) //reset
                } else {
                    droneCreationInterval.advance(amount)
                }
            }
        }

        if (!droneDeployInterval.intervalElapsed()) {
            droneDeployInterval.advance(amount)
        }

        activeWings = activeWings.filter { it.key.isAlive && !it.key.isHulk }.toMutableMap()

        if (alive || (!dronesExplodeWhenShipDies() && dronesDeployWhenShipIsDead())) {
            while (activeWings.size < getMaxDeployedDrones() && droneCharges > 0 && (droneDeployInterval.intervalDuration == 0f || droneDeployInterval.intervalElapsed())) {
                spawnDrone()
                droneCharges--
                droneDeployInterval.advance(0f)
            }
        }

        if (activeWings.isEmpty()) return

        if (droneWeaponGroupCheckInterval.advanceAndCheckElapsed(amount)) {
            activeWings
                .forEach { (ship, _) ->
                    ship.weaponGroupsCopy.forEachIndexed { index, group ->
                        if (!group.isAutofiring) {
                            ship.giveCommand(ShipCommand.TOGGLE_AUTOFIRE, null, index);
                        }
                    }
                }
        }

        formation.advance(ship, activeWings, amount)
    }

    /**
     * Override to not display charge filling in favor of separate drone bar if system doesn't use charges.
     */
    override fun getBarFill(): Float {
        if (!usesChargesOnActivate() && !hasSeparateDroneCharges()) {
            var fill = when (state) {
                State.READY -> 0f
                State.IN -> stateInterval.elapsed / (inDuration + activeDuration)
                State.ACTIVE -> if (isToggle && activeElapsed) {
                    1f
                } else {
                    (inDuration + stateInterval.elapsed) / (inDuration + activeDuration)
                }
                State.OUT -> 1f - stateInterval.elapsed / (outDuration + cooldownDuration)
                State.COOLDOWN -> 1f - (outDuration + stateInterval.elapsed) / (outDuration + cooldownDuration)
            }

            return fill.coerceIn(0f, 1f)
        }
        return super.getBarFill()
    }

    override fun drawHUDBar(viewport: ViewportAPI, barLoc: Vector2f) {
        var barLoc = barLoc
        MagicLibRendering.setTextAligned(LazyFont.TextAlignment.LEFT)
        val nameText: String = if (canAssignKey()) {
            val keyText = keyText
            String.format("%s (%s)", displayText, keyText)
        } else {
            String.format("%s", displayText)
        }
        val nameWidth = MagicLibRendering.getTextWidth(nameText)
        MagicLibRendering.addText(ship, nameText, hudColor, Vector2f.add(barLoc, Vector2f(0f, 10f), null))
        barLoc = Vector2f.add(barLoc, Vector2f(nameWidth + nameTextPadding + 2f, 0f), null)
        MagicLibRendering.setTextAligned(LazyFont.TextAlignment.RIGHT)
        MagicLibRendering.addText(ship, ammoText, hudColor, Vector2f.add(barLoc, Vector2f(0f, 10f), null))
        MagicLibRendering.setTextAligned(LazyFont.TextAlignment.LEFT)

        val stateText = stateText
        if (stateText.isNotEmpty()) {
            MagicLibRendering.addText(
                ship, getStateText(),
                hudColor, Vector2f.add(barLoc, Vector2f((12 + 4 + 59).toFloat(), 10f), null)
            )
        }

        MagicLibRendering.addBar(
            ship,
            barFill, hudColor, hudColor, 0f, Vector2f.add(barLoc, Vector2f(12f, 0f), null)
        )

        if (hasSeparateDroneCharges() || !usesChargesOnActivate()) {
            val droneBarPadding = 1 * UIscaling
            val droneBarWidth = ((59 * UIscaling - droneBarPadding * (getMaxDroneCharges() - 1)) / getMaxDroneCharges()).coerceAtLeast(1f)

            val max = (droneCharges + 1).coerceAtMost(getMaxDroneCharges())
            for (i in 0 until max) {
                val droneBarPos = Vector2f.add(barLoc, Vector2f(12f + droneBarWidth * i + droneBarPadding * i, -2 * UIscaling), null)
                val droneBarFill = if (droneCharges < getMaxDroneCharges() && i == max - 1) droneCreationInterval.elapsed / droneCreationInterval.intervalDuration else 1f
                MagicLibRendering.addBar(
                    ship,
                    droneBarFill, hudColor, hudColor, 0f, droneBarPos, 2 * UIscaling, droneBarWidth, false)
            }
        }
    }
}