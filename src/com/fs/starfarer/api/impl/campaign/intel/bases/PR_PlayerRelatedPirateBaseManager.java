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

public class PR_PlayerRelatedPirateBaseManager extends PlayerRelatedPirateBaseManager {

    // Don't touch this. You'll corrupt everybody's saves.
    public static final String KEY = "$core_PR_pirateBaseManager";


    public static PR_PlayerRelatedPirateBaseManager getInstance() {
        Object test = Global.getSector().getMemoryWithoutUpdate().get(KEY);
        return (PR_PlayerRelatedPirateBaseManager) test;
    }

    public static Logger log = Global.getLogger(PR_PirateBaseIntel.class);

    protected List<PR_PirateBaseIntel> bases = new ArrayList<PR_PirateBaseIntel>();

    public PR_PlayerRelatedPirateBaseManager() {
        super();
        Global.getSector().getMemoryWithoutUpdate().set(KEY, this);
        start = Global.getSector().getClock().getTimestamp();
    }

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

        // Allow player related pirate bases to attack AI
//        intel.setTargetPlayerColonies(true);
//        intel.setForceTarget(initialTarget);
        intel.updateTarget();
        bases.add(intel);
    }

    protected void sendFirstRaid(List<MarketAPI> markets) {
        log.info("[Pirate Rebalance] Blocking initial pirate raid");
    }
}
