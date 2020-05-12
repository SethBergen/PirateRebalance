package com.fs.starfarer.api.impl.campaign.intel.bases;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.comm.IntelInfoPlugin;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.DebugFlags;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.intel.bases.PirateBaseIntel.PirateBaseTier;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.WeightedRandomPicker;
import org.apache.log4j.Logger;

public class PR_PlayerRelatedPirateBaseManager extends PlayerRelatedPirateBaseManager {

    public static final String KEY = "$core_PR_pirateBaseManager";


    public static PR_PlayerRelatedPirateBaseManager getInstance() {
        Object test = Global.getSector().getMemoryWithoutUpdate().get(KEY);
        return (PR_PlayerRelatedPirateBaseManager) test;
    }


    protected long start = 0;
    protected boolean sentFirstRaid = false;
    protected IntervalUtil monthlyInterval = new IntervalUtil(20f, 40f);
    protected int monthsPlayerColoniesExist = 0;
    protected int baseCreationTimeout = 0;
    protected Random random = new Random();

    public static Logger log = Global.getLogger(PR_PirateBaseIntel.class);

    protected List<PR_PirateBaseIntel> bases = new ArrayList<PR_PirateBaseIntel>();

    public PR_PlayerRelatedPirateBaseManager() {
        super();
        Global.getSector().getMemoryWithoutUpdate().set(KEY, this);
        start = Global.getSector().getClock().getTimestamp();
    }


    public void advance(float amount) {

        for (PR_PirateBaseIntel intel : bases) {
            intel.advance(amount);
        }

        float days = Misc.getDays(amount);

        if (DebugFlags.RAID_DEBUG) {
            days *= 100f;
        }

        monthlyInterval.advance(days);

        if (monthlyInterval.intervalElapsed()) {
            removeDestroyedBases();

            FactionAPI player = Global.getSector().getPlayerFaction();
            List<MarketAPI> markets = Misc.getFactionMarkets(player);

            if (markets.isEmpty()) {
                return;
            }

            monthsPlayerColoniesExist++;

            if (!sentFirstRaid) {
                if (monthsPlayerColoniesExist >= 4 && !markets.isEmpty()) {
                    sendFirstRaid(markets);
                    baseCreationTimeout = 3 + random.nextInt(4);
                }
                return;
            }

            if (baseCreationTimeout > 0) {
                baseCreationTimeout--;
            } else {
                if (random.nextFloat() > 0.5f) {
                    addBasesAsNeeded();
                }
            }
        }
    }

    protected void removeDestroyedBases() {
        Iterator<PR_PirateBaseIntel> iter = bases.iterator();
        while (iter.hasNext()) {
            PR_PirateBaseIntel intel = iter.next();
            if (intel.isEnded() && !intel.getMarket().isInEconomy()) {
                iter.remove();

                int baseTimeout = 3;
                switch (intel.getTier()) {
                    case TIER_1_1MODULE: baseTimeout = 3; break;
                    case TIER_2_1MODULE: baseTimeout = 3; break;
                    case TIER_3_2MODULE: baseTimeout = 4; break;
                    case TIER_4_3MODULE: baseTimeout = 5; break;
                    case TIER_5_3MODULE: baseTimeout = 6; break;
                }
                baseCreationTimeout += baseTimeout + random.nextInt(baseTimeout + 1);
            }
        }
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

        //factionId = Factions.HEGEMONY;

        PR_PirateBaseIntel intel = new PR_PirateBaseIntel(target, factionId, tier);
        if (intel.isDone()) {
            intel = null;
            return;
        }

        intel.setTargetPlayerColonies(true);
        intel.setForceTarget(initialTarget);
        intel.updateTarget();
        bases.add(intel);
    }

//    public String pickPirateFaction() {
//        WeightedRandomPicker<String> picker = new WeightedRandomPicker<String>(random);
//        for (FactionAPI faction : Global.getSector().getAllFactions()) {
//            if (!faction.isHostileTo(Factions.PLAYER)) continue;
//
//            if (faction.getCustomBoolean(Factions.CUSTOM_MAKES_PIRATE_BASES)) {
//                picker.add(faction.getId(), 1f);
//            }
//        }
//        return picker.pick();
//    }

    protected void sendFirstRaid(List<MarketAPI> markets) {
        log.info("PIRATES SENDING FIRST RAID");
        if (markets.isEmpty()) return;

        sentFirstRaid = true;

        WeightedRandomPicker<MarketAPI> picker = new WeightedRandomPicker<MarketAPI>(random);
        picker.addAll(markets);
        MarketAPI target = picker.pick();

        PR_PirateBaseIntel closest = null;
        float minDist = Float.MAX_VALUE;
        for (IntelInfoPlugin p : Global.getSector().getIntelManager().getIntel(PR_PirateBaseIntel.class)) {
            PR_PirateBaseIntel intel = (PR_PirateBaseIntel) p;
            if (intel.isEnding()) continue;

            float dist = Misc.getDistance(intel.getMarket().getPrimaryEntity(), target.getPrimaryEntity());
            if (dist < minDist) {
                minDist = dist;
                closest = intel;
            }
        }

        if (closest != null && target != null) {
            float raidFP = 120 + 30f * random.nextFloat();
//			raidFP = 1000;
//			raidFP = 500;
            closest.startRaid(target.getStarSystem(), raidFP);
        }
    }

//    protected PirateBaseTier pickTier(StarSystemAPI system) {
//        log.info("PIRATES PICKING TIER");
//        float max = 0f;
//        for (MarketAPI m : Global.getSector().getEconomy().getMarkets(system)) {
//            if (m.getFaction().isPlayerFaction()) {
//                max = Math.max(m.getSize(), max);
//            }
//        }
//        if (max >= 7) {
//            return PirateBaseTier.TIER_5_3MODULE;
//        } else if (max >= 6) {
//            return PirateBaseTier.TIER_4_3MODULE;
//        } else if (max >= 5) {
//            return PirateBaseTier.TIER_3_2MODULE;
//        } else if (max >= 4) {
//            return PirateBaseTier.TIER_2_1MODULE;
//        } else {
//            return PirateBaseTier.TIER_1_1MODULE;
//        }
//
//    }

    protected StarSystemAPI pickSystemForPirateBase(StarSystemAPI initialTarget) {
        log.info("PIRATES PICKING BASE");
        WeightedRandomPicker<StarSystemAPI> veryFar = new WeightedRandomPicker<StarSystemAPI>(random);
        WeightedRandomPicker<StarSystemAPI> far = new WeightedRandomPicker<StarSystemAPI>(random);
        WeightedRandomPicker<StarSystemAPI> picker = new WeightedRandomPicker<StarSystemAPI>(random);

        for (StarSystemAPI system : Global.getSector().getStarSystems()) {
            float days = Global.getSector().getClock().getElapsedDaysSince(system.getLastPlayerVisitTimestamp());
            if (days < 45f) continue;

            if (system.getCenter().getMemoryWithoutUpdate().contains(PirateBaseManager.RECENTLY_USED_FOR_BASE)) continue;

            float weight = 0f;
            if (system.hasTag(Tags.THEME_MISC_SKIP)) {
                weight = 1f;
            } else if (system.hasTag(Tags.THEME_MISC)) {
                weight = 3f;
            } else if (system.hasTag(Tags.THEME_REMNANT_NO_FLEETS)) {
                weight = 3f;
            } else if (system.hasTag(Tags.THEME_RUINS)) {
                weight = 5f;
            } else if (system.hasTag(Tags.THEME_CORE_UNPOPULATED)) {
                weight = 1f;
            }
            if (weight <= 0f) continue;

            float usefulStuff = system.getCustomEntitiesWithTag(Tags.OBJECTIVE).size() +
                    system.getCustomEntitiesWithTag(Tags.STABLE_LOCATION).size();
            if (usefulStuff <= 0) continue;

            if (Misc.getMarketsInLocation(system).size() > 0) continue;

            float dist = Misc.getDistance(initialTarget.getLocation(), system.getLocation());

            float distMult = 100000f / dist;
            distMult *= distMult;

            if (dist > 30000f) {
                veryFar.add(system, weight * usefulStuff * distMult);
            } else if (dist > 10000f) {
                far.add(system, weight * usefulStuff * distMult);
            } else {
                picker.add(system, weight * usefulStuff * distMult);
            }
        }

        if (picker.isEmpty()) {
            picker.addAll(far);
        }
        if (picker.isEmpty()) {
            picker.addAll(veryFar);
        }

        return picker.pick();
    }
}
