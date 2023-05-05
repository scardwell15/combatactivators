package activators

import com.fs.starfarer.api.BaseModPlugin

class CombatActivatorsModPlugin: BaseModPlugin() {
    override fun onApplicationLoad() {
        ActivatorManager.initialize()
    }

    override fun onGameLoad(newGame: Boolean) {
    }
}