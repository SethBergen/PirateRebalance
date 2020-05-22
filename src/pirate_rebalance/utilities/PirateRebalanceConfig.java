package pirate_rebalance.utilities;

import org.json.JSONObject;

import com.fs.starfarer.api.Global;

public class PirateRebalanceConfig {
    public static final String CONFIG_PATH = "pr_config.json";

    public static boolean disableScriptedFirstRaid = true;
    public static boolean disableForceTargettingPlayer = true;
    public static boolean restrictPirateRange = true;

    public static int newPirateBaseInactivityMonths = 12;
    public static double preferredPirateRange = 10000f;

    static {
        loadSettings();
    }

    public static void loadSettings() {
        try {
            JSONObject config = Global.getSettings().loadJSON(CONFIG_PATH);

            disableScriptedFirstRaid = config.optBoolean("disableScriptedFirstRaid", disableScriptedFirstRaid);
            disableForceTargettingPlayer = config.optBoolean("disableForceTargettingPlayer", disableForceTargettingPlayer);
            restrictPirateRange = config.optBoolean("restrictPirateRange", restrictPirateRange);

            newPirateBaseInactivityMonths = config.optInt("newPirateBaseInactivityMonths", newPirateBaseInactivityMonths);

            preferredPirateRange = config.optDouble("preferredPirateRange", preferredPirateRange);
        } catch (Exception e) {
            throw new RuntimeException("Encountered an error while loading configs for Pirate Rebalance: " +
                                        e.getMessage(), e);
        }
    }
}
