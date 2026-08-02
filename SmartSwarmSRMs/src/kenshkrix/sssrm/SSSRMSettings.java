package kenshkrix.sssrm;

import com.fs.starfarer.api.Global;

/**
 * Reads LunaLib settings when LunaLib is present, and falls back otherwise.
 *
 * <p>LunaLib is optional. Every accessor takes the fallback the caller would have used
 * anyway -- normally the value loaded from this mod's own JSON config -- so with LunaLib
 * absent the mod behaves exactly as its config files say.
 *
 * <p>All LunaLib types stay behind {@link LunaSettingsBridge}; nothing here names one, so
 * this class is safe to load unconditionally.
 */
public final class SSSRMSettings {

    public static final String MOD_ID = "SSSRM";

    public static final String OMEGA_TESSERACT_COUNT = "sssrm_omegaTesseractCount";
    public static final String OMEGA_REFIT_ENABLED = "sssrm_omegaRefitEnabled";
    public static final String OMEGA_REFIT_CHANCE = "sssrm_omegaRefitChance";
    public static final String RETARGET_MULT = "sssrm_retargetIntervalMult";
    public static final String OVERKILL_BUFFER = "sssrm_overkillBuffer";

    private static Boolean available;

    private SSSRMSettings() {
    }

    public static boolean lunaAvailable() {
        if (available == null) {
            try {
                available = Global.getSettings().getModManager().isModEnabled("lunalib");
            } catch (Exception e) {
                available = false;
            }
        }
        return available;
    }

    public static int getInt(String field, int fallback) {
        if (!lunaAvailable()) {
            return fallback;
        }
        try {
            Integer value = LunaSettingsBridge.getInt(MOD_ID, field);
            return value == null ? fallback : value;
        } catch (Throwable t) {
            // Missing or renamed field must never break combat or fleet spawning.
            return fallback;
        }
    }

    public static float getFloat(String field, float fallback) {
        if (!lunaAvailable()) {
            return fallback;
        }
        try {
            Float value = LunaSettingsBridge.getFloat(MOD_ID, field);
            return value == null ? fallback : value;
        } catch (Throwable t) {
            return fallback;
        }
    }

    public static boolean getBoolean(String field, boolean fallback) {
        if (!lunaAvailable()) {
            return fallback;
        }
        try {
            Boolean value = LunaSettingsBridge.getBoolean(MOD_ID, field);
            return value == null ? fallback : value;
        } catch (Throwable t) {
            return fallback;
        }
    }
}
