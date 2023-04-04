package activators

import com.fs.starfarer.api.BaseModPlugin
import com.fs.starfarer.api.Global

class CombatActivatorsModPlugin: BaseModPlugin() {
    override fun onGameLoad(newGame: Boolean) {
        ActivatorManager.initialize()
    }
}