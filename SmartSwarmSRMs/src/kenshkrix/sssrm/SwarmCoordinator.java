package kenshkrix.sssrm;

import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.input.InputEventAPI;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Owns the swarm-wide targeting decision. Registered as an every-frame combat plugin in
 * data/config/settings.json, so the game creates one per battle and calls {@link #init}.
 *
 * <p>Submunitions are bucketed into groups by (launching ship, projectile spec). Each group
 * re-solves its own assignment on a fixed interval rather than per-frame -- the solve is
 * O(missiles x targets) and the result is stable enough that running it 4x/sec is invisible.
 */
public class SwarmCoordinator extends BaseEveryFrameCombatPlugin {

    /**
     * Never scan tighter than this, however little flight time is left. Without it a swarm
     * about to expire computes a near-zero radius, finds nothing, and releases the target
     * it was a fraction of a second from hitting.
     */
    private static final float MIN_SCAN_RANGE = 400f;

    /** Vanilla ECCM Package. Gates cross-ship pooling and per-missile reachability. */
    private static final String ECCM_HULLMOD_ID = "eccm";

    /**
     * Global scale on every profile's retargetInterval, from LunaLib. Cached per battle
     * rather than read per frame -- the interval is consulted every frame for every group,
     * and a settings lookup there would cost more than the solve it is gating.
     */
    private static float retargetMult = 1f;

    /**
     * Headroom over the raw kill estimate before a target stops drawing missiles. Cached per
     * battle alongside retargetMult, for the same reason.
     */
    private static float overkillBuffer = 0.2f;

    private static SwarmCoordinator instance;

    private final Map<String, SwarmGroup> groups = new java.util.HashMap<>();
    /** Identity set of missiles already adopted, so the scan doesn't register duplicates. */
    private final java.util.Set<MissileAPI> adopted =
            java.util.Collections.newSetFromMap(new IdentityHashMap<MissileAPI, Boolean>());
    private CombatEngineAPI engine;

    /** Null outside of combat. Missile AIs register themselves through this on first advance. */
    public static SwarmCoordinator getInstance() {
        return instance;
    }

    @Override
    public void init(CombatEngineAPI engine) {
        this.engine = engine;
        this.groups.clear();
        this.adopted.clear();
        instance = this;
        // Re-read once per battle, so a settings change applies to the next fight.
        retargetMult = Math.max(0.1f, Math.min(10f,
                SSSRMSettings.getFloat(SSSRMSettings.RETARGET_MULT, 1f)));
        overkillBuffer = Math.max(0f, Math.min(2f,
                SSSRMSettings.getFloat(SSSRMSettings.OVERKILL_BUFFER, 0.2f)));
    }

    public void register(SwarmMember ai) {
        MissileAPI missile = ai.getMissile();
        ShipAPI source = missile.getSource();
        boolean eccm = hasECCM(source);

        // Without ECCM a launcher only coordinates with itself, so two ships firing at the
        // same formation each independently cover it and their leftovers pile onto the same
        // high-priority targets. ECCM ships share one pool per side instead: one allocation,
        // one remainder, and the enemy actually divided between them.
        String key = eccm
                ? "eccm|" + missile.getOwner() + "|" + missile.getProjectileSpecId()
                : (source == null ? "orphan" : source.getId()) + "|" + missile.getProjectileSpecId();

        SwarmGroup group = groups.get(key);
        if (group == null) {
            group = new SwarmGroup(ai.getProfile(), eccm);
            groups.put(key, group);
        }
        group.members.add(ai);
    }

    /**
     * Picks up missiles that never registered themselves. A guidanceOnly missile keeps its
     * stock AI, so no SwarmMissileAI is ever constructed for it and nothing calls register()
     * -- the coordinator has to go looking. Cheap enough at a few hundred missiles, and it
     * only touches specs that actually have a guidanceOnly profile.
     */
    private void adoptLooseMissiles() {
        for (Iterator<MissileAPI> it = adopted.iterator(); it.hasNext(); ) {
            MissileAPI m = it.next();
            if (m.isFading() || !engine.isEntityInPlay(m)) {
                it.remove();
            }
        }
        for (MissileAPI missile : engine.getMissiles()) {
            if (missile.isFading() || adopted.contains(missile)) {
                continue;
            }
            SwarmProfile profile = SwarmProfile.forSpec(missile.getProjectileSpecId());
            if (profile == null || !profile.isGuidanceOnly()) {
                continue;
            }
            register(new AdoptedMissile(missile, profile));
            adopted.add(missile);
        }
    }

    private static boolean hasECCM(ShipAPI ship) {
        return ship != null
                && ship.getVariant() != null
                && ship.getVariant().hasHullMod(ECCM_HULLMOD_ID);
    }

    @Override
    public void advance(float amount, List<InputEventAPI> events) {
        if (engine == null || engine.isPaused()) {
            return;
        }
        adoptLooseMissiles();

        for (Iterator<Map.Entry<String, SwarmGroup>> it = groups.entrySet().iterator(); it.hasNext(); ) {
            SwarmGroup group = it.next().getValue();
            group.advance(amount, engine);
            if (group.members.isEmpty()) {
                it.remove();
            }
        }
    }

    /**
     * A set of submunitions that share one allocation. Without ECCM that's the output of a
     * single launching ship; with ECCM it's every ECCM ship on a side pooled together.
     */
    private static class SwarmGroup {

        final SwarmProfile profile;
        /** ECCM pool: spans multiple ships, so the centroid can't stand in for position. */
        final boolean pooled;
        final List<SwarmMember> members = new ArrayList<>();
        float sinceReassign;

        SwarmGroup(SwarmProfile profile, boolean pooled) {
            this.profile = profile;
            this.pooled = pooled;
        }

        void advance(float amount, CombatEngineAPI engine) {
            members.removeIf(swarmMember -> !swarmMember.isActive(engine));
            if (members.isEmpty()) {
                return;
            }

            sinceReassign += amount;
            float interval = pooled
                    ? profile.getRetargetInterval() * profile.getEccmRetargetMult()
                    : profile.getRetargetInterval();
            interval *= retargetMult;
            if (sinceReassign < interval) {
                // Between passes, still drop targets that died this frame so nothing
                // spends up to retargetInterval seconds chasing a corpse.
                for (SwarmMember ai : members) {
                    if (ai.getTarget() != null && !isEngageable(ai.getTarget(), engine)) {
                        ai.setTarget(null);
                    }
                }
                return;
            }
            sinceReassign = 0f;
            reassign(engine);
        }

        void reassign(CombatEngineAPI engine) {
            final Vector2f centroid = centroid();
            int owner = members.get(0).getMissile().getOwner();

            List<CombatEntityAPI> targets = gatherTargets(engine, owner, centroid, scanRange(centroid));
            if (targets.isEmpty()) {
                for (SwarmMember ai : members) {
                    ai.setTarget(null);
                }
                return;
            }

            // Priority tier first, then proximity to the swarm as a whole.
            Collections.sort(targets, new Comparator<CombatEntityAPI>() {
                @Override
                public int compare(CombatEntityAPI a, CombatEntityAPI b) {
                    int rankA = profile.rankOf(TargetClass.of(a));
                    int rankB = profile.rankOf(TargetClass.of(b));
                    if (rankA != rankB) {
                        return rankA < rankB ? -1 : 1;
                    }
                    float distA = MathUtils.getDistance(a.getLocation(), centroid);
                    float distB = MathUtils.getDistance(b.getLocation(), centroid);
                    return Float.compare(distA, distB);
                }
            });

            int missileCount = members.size();
            int targetCount = targets.size();

            int[] quota = pooled
                    ? overkillAwareQuota(targets, missileCount)
                    : evenQuota(targetCount, missileCount);

            Map<CombatEntityAPI, Integer> indexOf = new IdentityHashMap<>();
            for (int i = 0; i < targetCount; i++) {
                indexOf.put(targets.get(i), i);
            }

            // Pass 1 -- hysteresis. A missile keeps its current target if that target is
            // still in the pool and hasn't used up its quota. Without this, missiles get
            // yanked between targets every pass and never close the distance on any of them.
            int[] filled = new int[targetCount];
            List<SwarmMember> unassigned = new ArrayList<>();
            for (SwarmMember ai : members) {
                Integer i = ai.getTarget() == null ? null : indexOf.get(ai.getTarget());
                if (i != null && filled[i] < quota[i] && canReach(ai, targets.get(i))) {
                    filled[i]++;
                } else {
                    ai.setTarget(null);
                    unassigned.add(ai);
                }
            }

            // Pass 2 -- fill every open slot with the nearest missile that's still free and
            // can actually get there. In an unpooled group slots and free missiles are equal
            // by construction, so everything gets assigned; in a pooled one a slot can go
            // unfilled because nothing in range can service it, and a missile can end the
            // pass unassigned. Both are fine -- they're reconsidered next pass.
            for (int i = 0; i < targetCount && !unassigned.isEmpty(); i++) {
                CombatEntityAPI target = targets.get(i);
                while (filled[i] < quota[i] && !unassigned.isEmpty()) {
                    int bestIdx = -1;
                    float bestDist = Float.MAX_VALUE;
                    for (int k = 0; k < unassigned.size(); k++) {
                        SwarmMember candidate = unassigned.get(k);
                        float dist = MathUtils.getDistance(
                                candidate.getMissile().getLocation(), target.getLocation());
                        if (dist < bestDist && canReach(candidate, target)) {
                            bestDist = dist;
                            bestIdx = k;
                        }
                    }
                    if (bestIdx < 0) {
                        break; // nothing left that can reach this target
                    }
                    unassigned.remove(bestIdx).setTarget(target);
                    filled[i]++;
                }
            }
        }

        /**
         * How far this swarm can actually still fly, rather than a fixed number. Both
         * getMaxSpeed() and getMaxFlightTime() are per-instance values, so any hullmod or
         * skill that boosts missile speed or endurance widens the scan radius for free --
         * a hardcoded range would silently disagree with the missiles' real reach.
         *
         * <p>Uses the longest-lived member because a group accumulates later salvos, so
         * remaining flight times can differ a lot within one group. Assignment is
         * nearest-first anyway, so distant targets tend to draw the missiles best placed
         * to reach them.
         */
        float scanRange(Vector2f centroid) {
            float range = 0f;
            for (SwarmMember ai : members) {
                float reach = reachOf(ai);
                if (pooled) {
                    // Members can be scattered across the map, so the scan has to be a circle
                    // big enough to contain every member's own reach -- a plain centroid
                    // radius would look in the empty space between two distant swarms.
                    reach += MathUtils.getDistance(ai.getMissile().getLocation(), centroid);
                }
                range = Math.max(range, reach);
            }
            return Math.max(range, MIN_SCAN_RANGE);
        }

        /**
         * Even split by count. Spare missiles go to the highest-priority targets; with fewer
         * missiles than targets this degrades to "cover the top N priorities, one each".
         */
        int[] evenQuota(int targetCount, int missileCount) {
            int[] quota = new int[targetCount];
            int base = missileCount / targetCount;
            int remainder = missileCount % targetCount;
            for (int i = 0; i < targetCount; i++) {
                quota[i] = base + (i < remainder ? 1 : 0);
            }
            return quota;
        }

        /**
         * ECCM allocation: an even split that a target drops out of once it has been
         * allocated enough missiles to die.
         *
         * <p>Even distribution is the default and the kill estimate is only a ceiling --
         * missiles are handed out round-robin in priority order, and a target that reaches
         * its cap stops receiving while the rest keep sharing. That is the inverse of filling
         * each target to its full requirement in turn, which stacks the whole swarm onto the
         * first target and only spills once it is saturated.
         *
         * <p>The cap carries a buffer over the raw estimate, because that estimate is
         * deliberately approximate -- allocating exactly the predicted number would leave
         * targets alive whenever it reads slightly low.
         */
        int[] overkillAwareQuota(List<CombatEntityAPI> targets, int missileCount) {
            int targetCount = targets.size();
            int[] quota = new int[targetCount];

            // A guidanceOnly member is a cascade stage. Its own damage figure describes the
            // stage itself, not the many smaller missiles it is about to become, so a kill
            // estimate taken off it is meaningless -- and its damage type can be null
            // outright. Stages get the plain even split.
            MissileAPI reference = profile.isGuidanceOnly() ? null : estimateReference();
            if (reference == null) {
                return evenQuota(targetCount, missileCount);
            }

            float buffer = overkillBuffer;
            int[] cap = new int[targetCount];
            for (int i = 0; i < targetCount; i++) {
                int need = KillEstimate.missilesToKill(targets.get(i), reference);
                cap[i] = (int) Math.ceil(need * (1f + buffer));
            }

            int remaining = missileCount;
            boolean progress = true;
            while (remaining > 0 && progress) {
                progress = false;
                for (int i = 0; i < targetCount && remaining > 0; i++) {
                    if (quota[i] >= cap[i]) {
                        continue;
                    }
                    quota[i]++;
                    remaining--;
                    progress = true;
                }
            }

            // Every target already saturated and missiles still spare. Spread them rather
            // than leave them idle -- an unassigned missile just coasts.
            for (int i = 0; remaining > 0; i = (i + 1) % targetCount) {
                quota[i]++;
                remaining--;
            }
            return quota;
        }

        /**
         * First member we can actually derive a kill estimate from. Never assume members
         * carry usable damage data -- getDamageType() can come back null.
         */
        MissileAPI estimateReference() {
            for (SwarmMember member : members) {
                if (KillEstimate.canEstimate(member.getMissile())) {
                    return member.getMissile();
                }
            }
            return null;
        }

        /** How far this one missile can still fly, after profile scaling and clamping. */
        float reachOf(SwarmMember ai) {
            MissileAPI missile = ai.getMissile();
            float remaining = missile.getMaxFlightTime() - missile.getFlightTime();
            if (remaining <= 0f) {
                return MIN_SCAN_RANGE;
            }
            float reach = missile.getMaxSpeed() * remaining * profile.getRangeMult();
            // Floor so a missile in its last moments doesn't drop the target it's about to hit.
            return Math.max(Math.min(reach, profile.getMaxSearchRange()), MIN_SCAN_RANGE);
        }

        /**
         * Pooled groups check each missile individually, so nothing gets handed a target on
         * the far side of the battle that it would time out before reaching. Unpooled groups
         * are one launcher's output flying in formation -- the group scan already covers it.
         */
        boolean canReach(SwarmMember ai, CombatEntityAPI target) {
            if (!pooled) {
                return true;
            }
            return MathUtils.getDistance(ai.getMissile().getLocation(), target.getLocation())
                    <= reachOf(ai);
        }

        List<CombatEntityAPI> gatherTargets(CombatEngineAPI engine, int owner, Vector2f centroid,
                                            float range) {
            List<CombatEntityAPI> out = new ArrayList<>();
            float rangeSq = range * range;

            for (ShipAPI ship : engine.getShips()) {
                // getOwner() > 1 is neutral scenery, not a combatant.
                if (ship.getOwner() == owner || ship.getOwner() > 1) {
                    continue;
                }
                if (!profile.accepts(TargetClass.of(ship)) || !isEngageable(ship, engine)) {
                    continue;
                }
                if (distSq(ship.getLocation(), centroid) > rangeSq) {
                    continue;
                }
                out.add(ship);
            }

            if (profile.accepts(TargetClass.MISSILE)) {
                for (MissileAPI missile : engine.getMissiles()) {
                    if (missile.getOwner() == owner || missile.getOwner() > 1 || missile.isFading()) {
                        continue;
                    }
                    // Flares are ordinary missile entities, so a profile that engages missiles
                    // would otherwise allocate them a full even share as though they were real
                    // targets. These are smart missiles with or without ECCM -- they see
                    // through decoys. This is a design choice, not an oversight.
                    if (missile.isFlare() || missile.isDecoyFlare()) {
                        continue;
                    }
                    if (distSq(missile.getLocation(), centroid) > rangeSq) {
                        continue;
                    }
                    out.add(missile);
                }
            }
            return out;
        }

        Vector2f centroid() {
            float x = 0f, y = 0f;
            for (SwarmMember ai : members) {
                Vector2f loc = ai.getMissile().getLocation();
                x += loc.x;
                y += loc.y;
            }
            return new Vector2f(x / members.size(), y / members.size());
        }
    }

    /** A target is engageable if it's still in play and can actually be hit right now. */
    private static boolean isEngageable(CombatEntityAPI entity, CombatEngineAPI engine) {
        if (entity == null || !engine.isEntityInPlay(entity)) {
            return false;
        }
        if (entity instanceof ShipAPI) {
            ShipAPI ship = (ShipAPI) entity;
            // Phased ships are untargetable; hulks and hull pieces are already dead.
            return ship.isAlive() && !ship.isHulk() && !ship.isPiece() && !ship.isPhased();
        }
        if (entity instanceof MissileAPI) {
            return !((MissileAPI) entity).isFading();
        }
        return true;
    }

    private static float distSq(Vector2f a, Vector2f b) {
        float dx = a.x - b.x;
        float dy = a.y - b.y;
        return dx * dx + dy * dy;
    }
}
