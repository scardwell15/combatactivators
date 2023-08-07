package activators

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.ViewportAPI
import com.fs.starfarer.api.util.IntervalUtil
import lunalib.lunaSettings.LunaSettings
import lunalib.lunaSettings.LunaSettingsListener
import org.lwjgl.util.vector.Vector2f

object ActivatorManager {
    var keyList: List<Int> = mutableListOf()

    fun initialize() {
        reloadKeys()
        LunaSettings.addListener(LunaKeybindSettingsListener())
    }

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

            if (!activator.canAssignKey() || activators.size > keyList.size) {
                activator.key = "N/A"
            } else {
                activator.keyIndex = activators.size - 1
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
                    it.advanceInternal(amount * ship.mutableStats.timeMult.modifiedValue)
                }

                it.advanceEveryFrame()
            }
        }
    }

    @JvmStatic
    fun drawActivatorsUI(viewport: ViewportAPI) {
        if (Global.getCombatEngine().combatUI == null || Global.getCombatEngine().combatUI.isShowingCommandUI || Global.getCombatEngine().combatUI.isShowingDeploymentDialog) {
            return
        }

        Global.getCombatEngine().playerShip?.let { ship ->
            getActivators(ship)?.let {
                var lastVec = MagicLibRendering.getHUDRightOffset(ship)
                for (activator in it) {
                    activator.drawHUDBar(viewport, lastVec)
                    lastVec = Vector2f.add(lastVec, Vector2f(0f, 22f), null)
                }
            }
        }
    }

    @JvmStatic
    fun drawActivatorsWorld(viewport: ViewportAPI) {
        val ships: List<ShipAPI> = Global.getCombatEngine().ships
        for (ship in ships) {
            getActivators(ship)?.forEach {
                it.renderWorld(viewport)
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

    fun reloadKeys() {
        keyList = mutableListOf(
            LunaSettings.getInt("combatactivators", "combatActivators_KeyBind1")!!,
            LunaSettings.getInt("combatactivators", "combatActivators_KeyBind2")!!,
            LunaSettings.getInt("combatactivators", "combatActivators_KeyBind3")!!,
            LunaSettings.getInt("combatactivators", "combatActivators_KeyBind4")!!,
            LunaSettings.getInt("combatactivators", "combatActivators_KeyBind5")!!,
        ).filter { it != 0 }
    }
}

class LunaKeybindSettingsListener : LunaSettingsListener {
    override fun settingsChanged(modID: String) {
        ActivatorManager.reloadKeys()
    }
}

fun IntervalUtil.advanceAndCheckElapsed(amount: Float): Boolean {
    this.advance(amount)
    return this.intervalElapsed()
}