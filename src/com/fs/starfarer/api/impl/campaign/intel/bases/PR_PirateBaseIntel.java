package com.fs.starfarer.api.impl.campaign.intel.bases;

import org.apache.log4j.Logger;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.WeightedRandomPicker;

import pirate_rebalance.utilities.PirateRebalanceConfig;
import pirate_rebalance.utilities.PirateRebalanceSectorUtils;

public class PR_PirateBaseIntel extends PirateBaseIntel {

    public static Logger log = Global.getLogger(PR_PirateBaseIntel.class);

    public PR_PirateBaseIntel(StarSystemAPI system, String factionId, PirateBaseTier tier) {
        super(system, factionId, tier);
        log.info(String.format("[Pirate Rebalance] Setting raidTimeoutMonths to %s",
                PirateRebalanceConfig.newPirateBaseInactivityMonths));
        raidTimeoutMonths = PirateRebalanceConfig.newPirateBaseInactivityMonths;
    }

    @Override
    protected void checkForTierChange() {
        if (bountyData != null) return;
        if (entity.isInCurrentLocation()) return;

        // If requirements for next tier are not fulfilled, return
        int numDestroyed = PR_PirateBaseManager.getInstance().getNumDestroyed();
        float maxPlayerMarketSize = PirateRebalanceSectorUtils.getMaxPlayerMarketSize();
        if (PirateRebalanceConfig.adaptivePirateBaseTierScaling &&
                !(tier == PirateBaseTier.TIER_4_3MODULE && (maxPlayerMarketSize >= 6 || numDestroyed >= 9) ||
                tier == PirateBaseTier.TIER_3_2MODULE && (maxPlayerMarketSize >= 5 || numDestroyed >= 6) ||
                tier == PirateBaseTier.TIER_2_1MODULE && (maxPlayerMarketSize >= 4 || numDestroyed >= 3) ||
                tier == PirateBaseTier.TIER_1_1MODULE)) return;

        float minMonths = Global.getSettings().getFloat("pirateBaseMinMonthsForNextTier");
        if (monthsAtCurrentTier > minMonths) {
            float prob = (monthsAtCurrentTier - minMonths) * 0.1f;
            if ((float) Math.random() < prob) {
                PirateBaseTier next = getNextTier(tier);
                if (next != null) {
                    tier = next;
                    updateStationIfNeeded();
                    monthsAtCurrentTier = 0;
                    return;
                }
            }
        }

        monthsAtCurrentTier++;
    }

    @Override
    protected StarSystemAPI pickTarget() {
        WeightedRandomPicker<StarSystemAPI> picker = new WeightedRandomPicker<StarSystemAPI>();
        boolean forceTargetIsValid = false;
        for (StarSystemAPI system : Global.getSector().getEconomy().getStarSystemsWithMarkets()) {
            float score = 0f;
            for (MarketAPI curr : Global.getSector().getEconomy().getMarkets(system)) {
                if (!affectsMarket(curr)) continue;
                if (targetPlayerColonies && !curr.getFaction().isPlayerFaction()) continue;

                if (system == forceTarget) {
                    forceTargetIsValid = true;
                }
                if (curr.hasCondition(Conditions.PIRATE_ACTIVITY)) continue;

                float w = curr.getSize();

                float preferredRange = (float)PirateRebalanceConfig.preferredPirateRange;

                float dist = Misc.getDistance(curr.getPrimaryEntity(), market.getPrimaryEntity());
                float mult = 1f - Math.max(0f, dist - preferredRange) / preferredRange;

                if (PirateRebalanceConfig.restrictPirateRange){
                    if (mult <= 0f) continue; // Beyond operational range of pirate base
                } else {
                    if (mult < 0.1f) mult = 0.1f;
                }

                if (mult > 1) mult = 1;

                if (!targetPlayerColonies && curr.getFaction().isPlayerFaction()) {
                    if (dist > preferredRange) continue;
                }

                score += w * mult;

            }
            picker.add(system, score);
        }

        if (forceTargetIsValid) {
            return forceTarget;
        }

        return picker.pick();
    }
}
