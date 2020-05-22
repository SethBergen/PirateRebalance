package pirate_rebalance.plugins;

import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.Global;

import org.apache.log4j.Logger;

public class PirateRebalancePlugin extends BaseModPlugin {
    public static Logger log;

    {
        log = Global.getLogger(PirateRebalancePlugin.class);
    }

    @Override
    public void onGameLoad(boolean newGame){
        log.info("[Pirate Rebalance] Mod Loaded");
    }
}
