package kenshkrix.sssrm;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.GuidedMissileAI;
import com.fs.starfarer.api.combat.MissileAIPlugin;
import com.fs.starfarer.api.combat.MissileAPI;

/**
 * A missile the coordinator steers without owning. It keeps whatever AI the engine gave it
 * and we only push a target into that AI, so any stock behaviour riding along with it --
 * for a MIRV, the split timer -- keeps working.
 *
 * <p>Needed because handing a MIRV a replacement AI via pickMissileAI() suppresses its
 * cascade: the missile flies intelligently and then never splits.
 */
public class AdoptedMissile implements SwarmMember {

    private final MissileAPI missile;
    private final SwarmProfile profile;

    /**
     * What the coordinator wants this missile on, tracked separately from what the stock AI
     * actually holds. The two deliberately diverge: see {@link #setTarget}.
     */
    private CombatEntityAPI assigned;

    public AdoptedMissile(MissileAPI missile, SwarmProfile profile) {
        this.missile = missile;
        this.profile = profile;
    }

    /** getMissileAI() may hand back a wrapper; the unwrapped plugin is the one holding the target. */
    private GuidedMissileAI guidance() {
        MissileAIPlugin ai = missile.getUnwrappedMissileAI();
        if (!(ai instanceof GuidedMissileAI)) {
            ai = missile.getMissileAI();
        }
        return ai instanceof GuidedMissileAI ? (GuidedMissileAI) ai : null;
    }

    @Override
    public CombatEntityAPI getTarget() {
        return assigned;
    }

    /**
     * Pushes a target into the stock AI -- but never a null one.
     *
     * <p>The coordinator clears targets routinely: on every reassignment pass, whenever a
     * target dies, and whenever a swarm finds nothing in range. That is fine for a missile we
     * own, but a stock MIRV AI is not built to be handed a null mid-flight, and doing so is
     * what started the combat crashes. The cascade ran through several splits cleanly before
     * these stages were steered at all.
     *
     * <p>So a clear is recorded locally and withheld from the stock AI, which simply keeps
     * flying at whatever it last had until we have a real target to give it. The coordinator's
     * own bookkeeping stays correct because {@link #getTarget} reports the local value.
     */
    @Override
    public void setTarget(CombatEntityAPI target) {
        this.assigned = target;
        if (target == null) {
            return;
        }
        GuidedMissileAI ai = guidance();
        if (ai != null) {
            ai.setTarget(target);
        }
    }

    @Override
    public MissileAPI getMissile() {
        return missile;
    }

    @Override
    public SwarmProfile getProfile() {
        return profile;
    }

    @Override
    public boolean isActive(CombatEngineAPI engine) {
        return !missile.isFading() && engine.isEntityInPlay(missile);
    }
}
