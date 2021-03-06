package com.fs.starfarer.api.impl.campaign.intel.bases;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import com.fs.starfarer.api.impl.campaign.intel.bases.PirateBaseIntel.PirateBaseTier;
import com.fs.starfarer.api.util.Misc;
import org.apache.log4j.Logger;
import pirate_rebalance.utilities.PirateRebalanceConfig;

public class PR_PlayerRelatedPirateBaseManager extends PlayerRelatedPirateBaseManager {

    public static Logger log = Global.getLogger(PR_PirateBaseIntel.class);

    protected List<PR_PirateBaseIntel> bases = new ArrayList<PR_PirateBaseIntel>();

    public PR_PlayerRelatedPirateBaseManager() {
        super();
    }

    @Override
    protected void addBasesAsNeeded() {
        FactionAPI player = Global.getSector().getPlayerFaction();
        List<MarketAPI> markets = Misc.getFactionMarkets(player);

        Set<StarSystemAPI> systems = new LinkedHashSet<StarSystemAPI>();
        for (MarketAPI curr : markets) {
            StarSystemAPI system = curr.getStarSystem();
            if (system != null) {
                systems.add(system);
            }
        }
        if (systems.isEmpty()) return;

        float marketTotal = markets.size();
        int numBases = (int) (marketTotal / 2);
        if (numBases < 1) numBases = 1;
        if (numBases > 2) numBases = 2;


        if (bases.size() >= numBases) {
            return;
        }

        StarSystemAPI initialTarget = null;
        float bestWeight = 0f;
        OUTER: for (StarSystemAPI curr : systems) {
            float w = 0f;
            for (MarketAPI m : Global.getSector().getEconomy().getMarkets(curr)) {
                if (m.hasCondition(Conditions.PIRATE_ACTIVITY)) continue OUTER;
                if (m.getFaction().isPlayerFaction()) {
                    w += m.getSize() * m.getSize();
                }
            }
            if (w > bestWeight) {
                bestWeight = w;
                initialTarget = curr;
            }
        }

        if (initialTarget == null) return;

        StarSystemAPI target = pickSystemForPirateBase(initialTarget);
        if (target == null) return;

        PirateBaseTier tier = pickTier(target);

        String factionId = pickPirateFaction();
        if (factionId == null) return;

        PR_PirateBaseIntel intel = new PR_PirateBaseIntel(target, factionId, tier);
        if (intel.isDone()) {
            intel = null;
            return;
        }

        if (PirateRebalanceConfig.disableForceTargettingPlayer) {
            log.info("[Pirate Rebalance] Preventing pirate base from force-targetting player");
        } else {
             intel.setTargetPlayerColonies(true);
             intel.setForceTarget(initialTarget);
        }

        intel.updateTarget();
        bases.add(intel);
    }

    @Override
    protected void sendFirstRaid(List<MarketAPI> markets) {
        if (PirateRebalanceConfig.disableScriptedFirstRaid) {
            log.info("[Pirate Rebalance] Blocking initial pirate raid");
            sentFirstRaid = true;
        } else {
            super.sendFirstRaid(markets);
        }

    }
}
