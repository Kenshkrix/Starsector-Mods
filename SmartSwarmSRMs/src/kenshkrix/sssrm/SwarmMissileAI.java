package kenshkrix.sssrm;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.GuidedMissileAI;
import com.fs.starfarer.api.combat.MissileAIPlugin;
import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.combat.ShipCommand;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lazywizard.lazylib.combat.AIUtils;
import org.lwjgl.util.vector.Vector2f;

/**
 * Flight logic for a single swarm submunition. Deliberately dumb about target
 * selection -- it flies at whatever {@link SwarmCoordinator} assigns it, so that
 * the even-dispersal decision is made once for the whole swarm rather than
 * independently by each missile.
 */
public class SwarmMissileAI implements MissileAIPlugin, GuidedMissileAI, SwarmMember {

    /** Don't fight the turn rate over sub-degree errors; it just causes visible jitter. */
    private static final float TURN_DEADBAND_DEGREES = 0.5f;

    private final MissileAPI missile;
    private final SwarmProfile profile;
    private CombatEntityAPI target;
    private boolean registered;

    public SwarmMissileAI(MissileAPI missile, SwarmProfile profile) {
        this.missile = missile;
        this.profile = profile;
    }

    @Override
    public void advance(float amount) {
        if (!registered) {
            SwarmCoordinator coordinator = SwarmCoordinator.getInstance();
            if (coordinator != null) {
                coordinator.register(this);
                registered = true;
            }
        }

        if (missile.isFading()) {
            return;
        }

        if (target == null) {
            // Nothing assigned yet (or everything in range died) -- keep coasting forward
            // so the swarm stays together until the next assignment pass.
            missile.giveCommand(ShipCommand.ACCELERATE);
            return;
        }

        Vector2f aim = AIUtils.getBestInterceptPoint(
                missile.getLocation(), missile.getMaxSpeed(),
                target.getLocation(), target.getVelocity());
        if (aim == null) {
            aim = target.getLocation();
        }

        float desired = VectorUtils.getAngle(missile.getLocation(), aim);
        float diff = MathUtils.getShortestRotation(missile.getFacing(), desired);

        // giveCommand() is bang-bang -- there's no "turn at rate X". Steering purely on the
        // sign of the heading error means the missile is always at full spin, so it sails
        // past the correct heading and has to come back: a permanent wobble that gets worse
        // the more agile the missile is. Subtracting the angle it would sweep through while
        // arresting its current spin turns this into a proper lead-in/lead-out.
        float turnAcc = missile.getTurnAcceleration();
        float error = diff;
        if (turnAcc > 0f) {
            float av = missile.getAngularVelocity();
            error -= (av * Math.abs(av)) / (2f * turnAcc);
        }

        if (Math.abs(error) > TURN_DEADBAND_DEGREES) {
            missile.giveCommand(error > 0f ? ShipCommand.TURN_LEFT : ShipCommand.TURN_RIGHT);
        }
        missile.giveCommand(ShipCommand.ACCELERATE);
    }

    @Override
    public CombatEntityAPI getTarget() {
        return target;
    }

    @Override
    public void setTarget(CombatEntityAPI target) {
        this.target = target;
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
