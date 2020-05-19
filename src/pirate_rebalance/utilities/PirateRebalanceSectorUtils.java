package pirate_rebalance.utilities;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;

public class PirateRebalanceSectorUtils {
    public static float getMaxPlayerMarketSize() {
        float maxPlayerMarketSize = 0f;
        for (StarSystemAPI system : Global.getSector().getEconomy().getStarSystemsWithMarkets()) {
            for (MarketAPI market : Global.getSector().getEconomy().getMarkets(system)) {
                if (market.getFaction().isPlayerFaction()) {
                    maxPlayerMarketSize = Math.max(market.getSize(), maxPlayerMarketSize);
                }
            }
        }
        return maxPlayerMarketSize;
    }
}
