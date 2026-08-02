package kenshkrix.sssrm;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.DamageType;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Per-launcher tuning, keyed by the submunition's projectile spec id. Loaded once at
 * application load from data/config/sssrm_swarms.json, which other mods can merge into.
 */
public class SwarmProfile {

    public static final String CONFIG_PATH = "data/config/sssrm_swarms.json";
    private static final Logger log = Global.getLogger(SwarmProfile.class);

    private static final Map<String, SwarmProfile> PROFILES = new HashMap<>();

    /** Ordered high-to-low. Index in this list is the target's priority rank. */
    private final List<TargetClass> priority;
    private final Map<TargetClass, Integer> rank = new EnumMap<>(TargetClass.class);
    private final float rangeMult;
    private final float maxSearchRange;
    private final float retargetInterval;
    private final float eccmRetargetMult;
    private final boolean guidanceOnly;
    private final DamageType damageType;

    private SwarmProfile(List<TargetClass> priority, float rangeMult, float maxSearchRange,
                         float retargetInterval, float eccmRetargetMult,
                         boolean guidanceOnly, DamageType damageType) {
        this.priority = priority;
        this.rangeMult = rangeMult;
        this.maxSearchRange = maxSearchRange;
        this.retargetInterval = retargetInterval;
        this.eccmRetargetMult = eccmRetargetMult;
        this.guidanceOnly = guidanceOnly;
        this.damageType = damageType;
        for (int i = 0; i < priority.size(); i++) {
            rank.put(priority.get(i), i);
        }
    }

    /** Fallback used when a missile has no configured profile: everything is fair game. */
    public static SwarmProfile defaultProfile() {
        return new SwarmProfile(
                Arrays.asList(TargetClass.FRIGATE, TargetClass.DESTROYER,
                        TargetClass.CRUISER, TargetClass.CAPITAL),
                1f, 3000f, 0.25f, 0.5f, false, DamageType.HIGH_EXPLOSIVE);
    }

    public static void loadAll() {
        PROFILES.clear();
        try {
            JSONObject root = Global.getSettings().getMergedJSONForMod(CONFIG_PATH, "SSSRM");
            for (java.util.Iterator<?> it = root.keys(); it.hasNext(); ) {
                String specId = (String) it.next();
                JSONObject entry = root.getJSONObject(specId);

                List<TargetClass> priority = new ArrayList<>();
                JSONArray arr = entry.getJSONArray("priority");
                for (int i = 0; i < arr.length(); i++) {
                    String name = arr.getString(i).trim().toUpperCase();
                    try {
                        priority.add(TargetClass.valueOf(name));
                    } catch (IllegalArgumentException e) {
                        log.warn("SSSRM: unknown target class '" + name + "' in profile '" + specId + "'");
                    }
                }
                if (priority.isEmpty()) {
                    log.warn("SSSRM: profile '" + specId + "' has no valid target classes, skipping");
                    continue;
                }

                PROFILES.put(specId, new SwarmProfile(
                        priority,
                        (float) entry.optDouble("rangeMult", 1d),
                        (float) entry.optDouble("maxSearchRange", 3000d),
                        (float) entry.optDouble("retargetInterval", 0.25d),
                        (float) entry.optDouble("eccmRetargetMult", 0.5d),
                        entry.optBoolean("guidanceOnly", false),
                        parseDamageType(entry.optString("damageType", "HIGH_EXPLOSIVE"), specId)));
            }
            log.info("SSSRM: loaded " + PROFILES.size() + " swarm profile(s)");
        } catch (Exception e) {
            log.error("SSSRM: failed to load " + CONFIG_PATH, e);
        }
    }

    private static DamageType parseDamageType(String name, String specId) {
        try {
            return DamageType.valueOf(name.trim().toUpperCase());
        } catch (Exception e) {
            log.warn("SSSRM: unknown damageType '" + name + "' in profile '" + specId
                    + "', assuming HIGH_EXPLOSIVE");
            return DamageType.HIGH_EXPLOSIVE;
        }
    }

    /**
     * Used to repair missiles that spawn with no damage type at all. A MIRV spawned by
     * another MIRV arrives that way, and vanilla ship AI crashes reading it.
     */
    public DamageType getDamageType() {
        return damageType;
    }

    /** Null if this projectile spec is not a smart-swarm submunition. */
    public static SwarmProfile forSpec(String projectileSpecId) {
        if (projectileSpecId == null) {
            return null;
        }
        return PROFILES.get(projectileSpecId);
    }

    public List<TargetClass> getPriority() {
        return priority;
    }

    /** Lower is more desirable. Integer.MAX_VALUE means "not a valid target for this profile". */
    public int rankOf(TargetClass tc) {
        Integer r = rank.get(tc);
        return r == null ? Integer.MAX_VALUE : r;
    }

    public boolean accepts(TargetClass tc) {
        return tc != null && rank.containsKey(tc);
    }

    /** Fraction of the swarm's actual reach to scan for targets. */
    public float getRangeMult() {
        return rangeMult;
    }

    /** Absolute ceiling on the scan radius, regardless of computed reach. */
    public float getMaxSearchRange() {
        return maxSearchRange;
    }

    public float getRetargetInterval() {
        return retargetInterval;
    }

    /** Scales the retarget interval for ECCM-pooled swarms. Below 1.0 means they react faster. */
    public float getEccmRetargetMult() {
        return eccmRetargetMult;
    }

    /**
     * True for missiles we steer but do not own. Handing a MIRV a replacement AI kills its
     * split, so cascade stages must keep their stock AI and only have a target pushed in.
     */
    public boolean isGuidanceOnly() {
        return guidanceOnly;
    }
}
