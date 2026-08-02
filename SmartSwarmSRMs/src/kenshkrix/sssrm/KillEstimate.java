package kenshkrix.sssrm;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.combat.ShieldAPI;
import com.fs.starfarer.api.combat.ShipAPI;

/**
 * Rough estimate of how many submunitions it takes to destroy a target, so an
 * ECCM-coordinated swarm can stop putting six missiles into a frigate that two would kill.
 *
 * <p>Deliberately approximate. The estimate only ever <i>binds</i> when a target is weak
 * relative to the swarm -- fighters, missiles, chewed-up frigates. Anything big enough for
 * the modelling shortcuts here to matter needs more missiles than a swarm carries, so the
 * cap never comes into play and the allocation falls back to spreading everything out.
 * Where it does approximate, it rounds toward needing <i>more</i> missiles, so the failure
 * mode is a bit of overkill rather than a target left alive.
 */
final class KillEstimate {

    /** Sanity ceiling so a bad estimate can't swallow an entire swarm's allocation. */
    private static final int MAX_ESTIMATE = 999;

    private static Float minArmorDamageMult;

    private KillEstimate() {
    }

    /**
     * Whether this missile carries enough information to estimate anything. getDamageType()
     * can return null -- seen in the wild on cascade stages -- and every multiplier below
     * dereferences it, so callers must check this before trusting an estimate.
     */
    static boolean canEstimate(MissileAPI reference) {
        return reference != null
                && reference.getDamageType() != null
                && reference.getDamageAmount() > 0f;
    }

    static int missilesToKill(CombatEntityAPI target, MissileAPI reference) {
        if (!canEstimate(reference)) {
            return 1;
        }
        float perHit = reference.getDamageAmount();
        if (!(target instanceof ShipAPI)) {
            // Missiles and anything else fragile enough to be on the target list die to one hit.
            return 1;
        }

        ShipAPI ship = (ShipAPI) target;
        DamageType type = reference.getDamageType();
        if (type == null) return 1;

        // Damage actually delivered per hit while armor is intact. Using the armored figure
        // for the whole hull pool under-rates the missiles slightly, which is the safe way
        // to be wrong here.
        float armor = ship.getArmorGrid() == null ? 0f : ship.getArmorGrid().getArmorRating();
        float hitStrength = perHit * type.getArmorMult();
        float reduction = Math.max(hitStrength / (hitStrength + Math.max(armor, 1f)),
                minArmorDamageMult());
        float delivered = hitStrength * reduction;
        if (delivered <= 0f) {
            return MAX_ESTIMATE;
        }

        int needed = ceilDiv(ship.getHitpoints() + armor, delivered);

        // A raised shield covering the approach has to be filled before anything reaches the
        // hull. Arc is checked against one reference missile -- in a pooled swarm the members
        // can be spread around the target, so this is a proxy for the swarm's approach rather
        // than a per-missile answer.
        ShieldAPI shield = ship.getShield();
        if (shield != null && shield.isOn() && ship.getFluxTracker() != null
                && type.getShieldMult() > 0f
                && shield.isWithinArc(reference.getLocation())) {
            float remainingFlux =
                    ship.getFluxTracker().getMaxFlux() - ship.getFluxTracker().getCurrFlux();
            float fluxPerDamage = shield.getFluxPerPointOfDamage();
            if (remainingFlux > 0f && fluxPerDamage > 0f) {
                float rawToFill = remainingFlux / (type.getShieldMult() * fluxPerDamage);
                needed += ceilDiv(rawToFill, perHit);
            }
        }

        return Math.max(1, Math.min(needed, MAX_ESTIMATE));
    }

    /**
     * Armor never reduces damage below this fraction. Read from settings rather than
     * hardcoded so retuning mods stay consistent with the estimate.
     */
    private static float minArmorDamageMult() {
        if (minArmorDamageMult == null) {
            float value = 0.15f;
            try {
                value = 1f - Global.getSettings().getFloat("maxArmorDamageReduction");
            } catch (Exception e) {
                Global.getLogger(KillEstimate.class)
                        .warn("SSSRM: maxArmorDamageReduction unreadable, assuming 0.85");
            }
            minArmorDamageMult = value;
        }
        return minArmorDamageMult;
    }

    private static int ceilDiv(float numerator, float denominator) {
        return (int) Math.ceil(numerator / denominator);
    }
}
