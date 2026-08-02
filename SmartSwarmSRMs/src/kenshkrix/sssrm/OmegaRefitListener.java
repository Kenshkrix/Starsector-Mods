package kenshkrix.sssrm;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.FleetInflater;
import com.fs.starfarer.api.campaign.listeners.FleetInflationListener;
import com.fs.starfarer.api.campaign.AICoreOfficerPlugin;
import com.fs.starfarer.api.campaign.listeners.FleetSpawnListener;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.fleet.FleetMemberType;
import com.fs.starfarer.api.loading.VariantSource;
import com.fs.starfarer.api.loading.WeaponSlotAPI;
import com.fs.starfarer.api.util.Misc;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Fits the Cascade Resonator to Omega ships as their fleets spawn.
 *
 * <p>Shipping a new goal variant did not work -- Omega fleets do not appear to pick up
 * mod-added goal variants, so tesseract_Cascade never spawned. Refitting at spawn time
 * sidesteps that entirely, and has the advantage of working with whichever variant the game
 * actually chose: an Omega Tesseract keeps its Attack or Defense or Strike loadout and simply
 * has the weapon in its large slot swapped.
 *
 * <p>Nothing vanilla is overwritten. The variant is cloned per member before editing, so the
 * shared stock variant is never mutated -- without the clone every Tesseract in the game,
 * including ones already spawned, would inherit the change.
 */
public class OmegaRefitListener implements FleetSpawnListener, FleetInflationListener {

    public static final String CONFIG_PATH = "data/config/sssrm_omega.json";

    /** LunaLib setting: how many Tesseracts an Omega fleet should field. */
    public static final String MOD_ID = "SSSRM";
    public static final String SETTING_TESSERACTS = "sssrm_omegaTesseractCount";
    /** Vanilla hypershunt guardians field two. */
    private static final int DEFAULT_TESSERACTS = 2;
    private static final int MIN_TESSERACTS = 1;
    private static final int MAX_TESSERACTS = 10;
    private static final String TESSERACT_HULL = "tesseract";
    /** Set on a fleet once handled, so repeat passes are no-ops. */
    private static final String PROCESSED_FLAG = "$sssrm_omegaProcessed";

    /** Ids stay in JSON -- they are not useful as UI, and a typo in a slider is unhelpful. */
    private static String weaponId = "sssrm_resonance";
    private static String factionId = "omega";
    /** JSON values act as the fallback whenever LunaLib is absent. */
    private static float configChance = 0.35f;
    private static boolean configEnabled = true;

    public static void loadConfig() {
        try {
            JSONObject json = Global.getSettings().getMergedJSONForMod(CONFIG_PATH, "SSSRM");
            configEnabled = json.optBoolean("enabled", true);
            weaponId = json.optString("weaponId", "sssrm_resonance");
            factionId = json.optString("factionId", "omega");
            configChance = (float) json.optDouble("chancePerShip", 0.35d);
            Global.getLogger(OmegaRefitListener.class).info(
                    "SSSRM: omega refit defaults " + (configEnabled ? "on" : "off")
                            + ", " + (int) (configChance * 100) + "% chance of " + weaponId
                            + " (LunaLib overrides these when present)");
        } catch (Exception e) {
            Global.getLogger(OmegaRefitListener.class)
                    .warn("SSSRM: could not read " + CONFIG_PATH + ", using defaults", e);
        }
    }

    @Override
    public void reportFleetSpawnedToListener(CampaignFleetAPI fleet) {
        process(fleet);
    }

    /**
     * Fires when a fleet's loadouts are generated. This is the hook that matters for
     * hypershunt guardians: they are built lazily inside the interaction dialog and are not
     * persisted, so nothing exists to find beforehand and no spawn is ever reported.
     */
    @Override
    public void reportFleetInflated(CampaignFleetAPI fleet, FleetInflater inflater) {
        process(fleet);
    }

    /**
     * Resizes and refits an Omega fleet. Safe to call repeatedly -- a processed fleet is
     * flagged in its own memory and skipped afterwards, so the listener and the periodic
     * sweep can both target the same fleet without doubling the refit rolls.
     *
     * @return true if this call was the one that processed it
     */
    public static boolean process(CampaignFleetAPI fleet) {
        if (!refitEnabled() || fleet == null || fleet.getFaction() == null) {
            return false;
        }
        if (!factionId.equals(fleet.getFaction().getId())) {
            return false;
        }
        if (fleet.getMemoryWithoutUpdate().getBoolean(PROCESSED_FLAG)) {
            return false;
        }
        fleet.getMemoryWithoutUpdate().set(PROCESSED_FLAG, true);

        // Resize before refitting, so ships added here get a refit roll of their own.
        resizeTesseracts(fleet);

        Random random = new Random();
        float chance = refitChance();
        for (FleetMemberAPI member : fleet.getFleetData().getMembersListCopy()) {
            if (random.nextFloat() >= chance) {
                continue;
            }
            refit(member);
        }
        return true;
    }

    /**
     * Brings the fleet's Tesseract count to whatever the LunaLib setting asks for.
     *
     * <p>Only touches fleets that already field at least one -- that keeps it to guardian-type
     * fleets and avoids conjuring Tesseracts into escort or patrol groups that never had any.
     */
    private static void resizeTesseracts(CampaignFleetAPI fleet) {
        List<FleetMemberAPI> tesseracts = new ArrayList<>();
        for (FleetMemberAPI member : fleet.getFleetData().getMembersListCopy()) {
            if (TESSERACT_HULL.equals(member.getHullId())) {
                tesseracts.add(member);
            }
        }
        if (tesseracts.isEmpty()) {
            return;
        }

        int want = tesseractCount();
        int have = tesseracts.size();

        if (want == have) {
            maxOutCR(tesseracts);
            return;
        }

        if (want < have) {
            for (int i = have - 1; i >= want; i--) {
                fleet.getFleetData().removeFleetMember(tesseracts.get(i));
                // Keep the local list in step, or the CR pass below touches ships that are
                // no longer in the fleet.
                tesseracts.remove(i);
            }
        } else {
            // Copy an existing one so additions match whatever variant actually spawned,
            // rather than forcing a particular loadout.
            // Prefer an example that actually has an AI core to copy. Picking index 0
            // blindly would produce uncaptained extras whenever that particular ship
            // happened to be the one without one.
            FleetMemberAPI template = tesseracts.get(0);
            for (FleetMemberAPI candidate : tesseracts) {
                PersonAPI captain = candidate.getCaptain();
                if (captain != null && captain.isAICore()) {
                    template = candidate;
                    break;
                }
            }
            for (int i = have; i < want; i++) {
                ShipVariantAPI variant = template.getVariant();
                FleetMemberAPI added = Global.getFactory()
                        .createFleetMember(FleetMemberType.SHIP, variant.clone());
                fleet.getFleetData().addFleetMember(added);
                tesseracts.add(added);

                // Vanilla Tesseracts are flown by an Omega core, and a fleet member created
                // this way has no captain at all -- extras would otherwise field an
                // uncommanded hull with none of the core's skills.
                PersonAPI captain = matchingCore(template.getCaptain(), fleet);
                if (captain != null) {
                    added.setCaptain(captain);
                }
            }
        }
        maxOutCR(tesseracts);
        fleet.getFleetData().setSyncNeeded();
    }

    /**
     * Every Tesseract at full readiness, not just the ones added here. A guardian that spawns
     * degraded fights well below its listed strength, which makes it useless as a yardstick
     * for how much fleet it actually takes to beat one.
     */
    private static void maxOutCR(List<FleetMemberAPI> members) {
        for (FleetMemberAPI member : members) {
            if (member.getRepairTracker() != null) {
                member.getRepairTracker().setCR(member.getRepairTracker().getMaxCR());
            }
        }
    }

    /**
     * Builds a fresh AI core officer of the same type as the template's captain.
     *
     * <p>A new instance rather than the same PersonAPI: sharing one captain across two ships
     * gives both the same officer object, which the game does not expect.
     */
    private static PersonAPI matchingCore(PersonAPI template, CampaignFleetAPI fleet) {
        if (template == null || !template.isAICore() || template.getAICoreId() == null) {
            return null;
        }
        try {
            AICoreOfficerPlugin plugin = Misc.getAICoreOfficerPlugin(template.getAICoreId());
            if (plugin == null) {
                return null;
            }
            return plugin.createPerson(template.getAICoreId(),
                    fleet.getFaction().getId(), new Random());
        } catch (Exception e) {
            Global.getLogger(OmegaRefitListener.class)
                    .warn("SSSRM: could not clone AI core captain", e);
            return null;
        }
    }

    /** Clamped, and falls back to the vanilla count whenever LunaLib is not usable. */
    static int tesseractCount() {
        int value = SSSRMSettings.getInt(SSSRMSettings.OMEGA_TESSERACT_COUNT, DEFAULT_TESSERACTS);
        return Math.max(MIN_TESSERACTS, Math.min(MAX_TESSERACTS, value));
    }

    static boolean refitEnabled() {
        return SSSRMSettings.getBoolean(SSSRMSettings.OMEGA_REFIT_ENABLED, configEnabled);
    }

    /** Read live rather than cached, so moving the slider affects the next fleet. */
    static float refitChance() {
        float value = SSSRMSettings.getFloat(SSSRMSettings.OMEGA_REFIT_CHANCE, configChance);
        return Math.max(0f, Math.min(1f, value));
    }

    private static void refit(FleetMemberAPI member) {
        ShipVariantAPI variant = member.getVariant();
        if (variant == null) {
            return;
        }
        String slot = findLargeMissileCapableSlot(variant);
        if (slot == null) {
            return;
        }
        if (weaponId.equals(variant.getWeaponId(slot))) {
            return;
        }

        // Clone before editing. Stock variants are shared instances -- mutating one would
        // change every ship using it, retroactively and across the whole save.
        ShipVariantAPI copy = variant.clone();
        copy.setSource(VariantSource.REFIT);
        copy.addWeapon(slot, weaponId);
        member.setVariant(copy, false, true);
        member.updateStats();
    }

    /** First large slot that can mount a missile, ignoring built-ins and decoratives. */
    private static String findLargeMissileCapableSlot(ShipVariantAPI variant) {
        for (String slotId : variant.getNonBuiltInWeaponSlots()) {
            WeaponSlotAPI slot = variant.getSlot(slotId);
            if (slot == null || slot.isDecorative()) {
                continue;
            }
            if (slot.getSlotSize() != WeaponAPI.WeaponSize.LARGE) {
                continue;
            }
            WeaponAPI.WeaponType type = slot.getWeaponType();
            if (type == WeaponAPI.WeaponType.MISSILE
                    || type == WeaponAPI.WeaponType.UNIVERSAL
                    || type == WeaponAPI.WeaponType.SYNERGY
                    || type == WeaponAPI.WeaponType.COMPOSITE) {
                return slotId;
            }
        }
        return null;
    }
}
