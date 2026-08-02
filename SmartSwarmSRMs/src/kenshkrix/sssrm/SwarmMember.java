package kenshkrix.sssrm;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.MissileAPI;

/**
 * Anything {@link SwarmCoordinator} can hand a target to. Two implementations:
 *
 * <ul>
 *   <li>{@link SwarmMissileAI} -- we own the missile's AI outright and fly it ourselves.
 *   <li>{@link AdoptedMissile} -- the missile keeps its stock AI and we only redirect it.
 * </ul>
 *
 * <p>The split exists because replacing a MIRV's AI also replaces its splitting behaviour:
 * returning a plugin from pickMissileAI() for a MIRV-type missile stops it cascading
 * entirely. Adoption is the way to steer a missile whose stock behaviour we need to keep.
 */
public interface SwarmMember {

    MissileAPI getMissile();

    SwarmProfile getProfile();

    CombatEntityAPI getTarget();

    void setTarget(CombatEntityAPI target);

    boolean isActive(CombatEngineAPI engine);
}
