package kenshkrix.sssrm;

import lunalib.lunaSettings.LunaSettings;

/**
 * Isolates every reference to LunaLib in one class.
 *
 * <p>LunaLib is deliberately not a declared dependency -- the mod runs fine without it, just
 * with settings at their defaults. That only holds if no always-loaded class mentions a
 * LunaLib type: the JVM resolves classes on first active use, so a reference sitting in
 * {@link OmegaRefitListener} would fail verification when LunaLib is absent. Keeping the
 * reference here means the class is never touched unless the caller has already confirmed
 * LunaLib is enabled.
 *
 * <p>Callers must gate on that check and catch Throwable -- see
 * {@link SSSRMSettings}.
 */
final class LunaSettingsBridge {

    private LunaSettingsBridge() {
    }

    static Integer getInt(String modId, String fieldId) {
        return LunaSettings.getInt(modId, fieldId);
    }

    static Float getFloat(String modId, String fieldId) {
        return LunaSettings.getFloat(modId, fieldId);
    }

    static Boolean getBoolean(String modId, String fieldId) {
        return LunaSettings.getBoolean(modId, fieldId);
    }
}
