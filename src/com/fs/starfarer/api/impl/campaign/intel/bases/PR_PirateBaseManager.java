package com.fs.starfarer.api.impl.campaign.intel.bases;

import java.util.Random;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.intel.BaseEventManager;
import com.fs.starfarer.api.impl.campaign.intel.bases.PirateBaseIntel.PirateBaseTier;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.WeightedRandomPicker;
import pirate_rebalance.utilities.PirateRebalanceSectorUtils;

public class PR_PirateBaseManager extends PirateBaseManager {

//    public static final String KEY = "$core_pirateBaseManager";
//
//    public static final float CHECK_DAYS = 10f;
//    public static final float CHECK_PROB = 0.5f;
//
//    protected long start = 0;
//    protected float extraDays = 0;
//
//    protected int numDestroyed = 0;

    public PR_PirateBaseManager() {
        super();
    }


    protected Random random = new Random();
    @Override
    protected EveryFrameScript createEvent() {
        if (random.nextFloat() < CHECK_PROB) return null;

        StarSystemAPI system = pickSystemForPirateBase();
        if (system == null) return null;

        PirateBaseTier tier = pickTier();


        String factionId = pickPirateFaction();
        if (factionId == null) return null;

        PR_PirateBaseIntel intel = new PR_PirateBaseIntel(system, factionId, tier);
        if (intel.isDone()) intel = null;

        return intel;
    }

    protected PirateBaseTier pickTier() {
        float maxPlayerMarketSize = PirateRebalanceSectorUtils.getMaxPlayerMarketSize();

        WeightedRandomPicker<PirateBaseTier> picker = new WeightedRandomPicker<PirateBaseTier>();

        if (maxPlayerMarketSize >= 7 || numDestroyed >= 12) {
            picker.add(PirateBaseTier.TIER_5_3MODULE, 10f);
            picker.add(PirateBaseTier.TIER_4_3MODULE, 10f);
        } else if (maxPlayerMarketSize >= 6 || numDestroyed >= 9) {
            picker.add(PirateBaseTier.TIER_5_3MODULE, 10f);
            picker.add(PirateBaseTier.TIER_4_3MODULE, 10f);
            picker.add(PirateBaseTier.TIER_3_2MODULE, 10f);
        } else if (maxPlayerMarketSize >= 5 || numDestroyed >= 6) {
            picker.add(PirateBaseTier.TIER_4_3MODULE, 10f);
            picker.add(PirateBaseTier.TIER_3_2MODULE, 10f);
            picker.add(PirateBaseTier.TIER_2_1MODULE, 10f);
        } else if (maxPlayerMarketSize >= 4 || numDestroyed >= 3) {
            picker.add(PirateBaseTier.TIER_3_2MODULE, 10f);
            picker.add(PirateBaseTier.TIER_2_1MODULE, 10f);
            picker.add(PirateBaseTier.TIER_1_1MODULE, 10f);
        } else {
            picker.add(PirateBaseTier.TIER_2_1MODULE, 10f);
            picker.add(PirateBaseTier.TIER_1_1MODULE, 10f);
        }

        return picker.pick();
    }
}
