package activators

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.ShipAPI
import data.scripts.util.MagicSettings
import org.lwjgl.util.vector.Vector2f

object ActivatorManager {
    @JvmStatic
    fun addActivator(ship: ShipAPI, activator: CombatActivator) {
        var activators: MutableMap<Class<*>, CombatActivator>? =
            ship.customData["combatActivators"] as MutableMap<Class<*>, CombatActivator>?
        if (activators == null) {
            activators = LinkedHashMap()
            ship.setCustomData("combatActivators", activators)
        }

        if (!activators.containsKey(activator.javaClass)) {
            activators[activator.javaClass] = activator

            val keyList = MagicSettings.getList("combatactivators", "keys")
            if (activators.size > keyList.size) {
                activator.key = "N/A"
            } else {
                activator.key = MagicSettings.getList("combatactivators", "keys")[activators.size - 1]
            }

            activator.init()
        }
    }

    @JvmStatic
    fun advanceActivators(amount: Float) {
        val ships: List<ShipAPI> = Global.getCombatEngine().ships
        for (ship in ships) {
            getActivators(ship)?.forEach {
                if (!Global.getCombatEngine().isPaused) {
                    it.advanceInternal(amount)
                }

                it.advanceEveryFrame()
            }
        }
    }

    @JvmStatic
    fun drawActivators() {
        Global.getCombatEngine().playerShip?.let { ship ->
            getActivators(ship)?.let {
                var lastVec = MagicLibRendering.getHUDRightOffset(ship)
                for (activator in it) {
                    activator.drawHUDBar(lastVec)
                    lastVec = Vector2f.add(lastVec, Vector2f(0f, 22f), null)
                }
            }
        }
    }

    @JvmStatic
    fun getActivators(ship: ShipAPI): List<CombatActivator>? {
        val map = ship.customData["combatActivators"] as Map<Class<*>, CombatActivator>?
        if (map != null) {
            return ArrayList(map.values)
        }
        return null
    }
}