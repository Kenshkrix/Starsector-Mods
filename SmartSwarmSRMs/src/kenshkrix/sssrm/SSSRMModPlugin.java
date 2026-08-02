package kenshkrix.sssrm;

import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.PluginPick;
import com.fs.starfarer.api.campaign.CampaignPlugin;
import com.fs.starfarer.api.combat.DamageAPI;
import com.fs.starfarer.api.combat.MissileAIPlugin;
import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.combat.ShipAPI;

public class SSSRMModPlugin extends BaseModPlugin {

    @Override
    public void onApplicationLoad() {
        SwarmProfile.loadAll();
        OmegaRefitListener.loadConfig();
    }

    /**
     * Registers the spawn-time refit. Omega fleets do not pick up mod-added goal variants, so
     * the weapon is fitted as their fleets are created instead.
     *
     * <p>Guarded against duplicates: onGameLoad runs on every load, and a listener added twice
     * would roll the refit chance twice per ship.
     */
    @Override
    public void onGameLoad(boolean newGame) {
        if (!Global.getSector().getListenerManager().hasListenerOfClass(OmegaRefitListener.class)) {
            Global.getSector().getListenerManager().addListener(new OmegaRefitListener(), true);
        }
        // Listeners alone are not enough. A hypershunt's guardians are built inside the
        // interaction dialog, never join the star system, and are not persisted -- so nothing
        // reports them spawning and there is nothing to find before or after. This script
        // watches the open dialog and catches them while they briefly exist.
        Global.getSector().addTransientScript(new OmegaDefenderScript());
    }

    /**
     * Hands out the swarm AI for any missile whose projectile spec has a profile. Returning
     * null falls through to whatever the .proj file specifies, so non-swarm missiles from
     * this mod (and every other mod) are untouched.
     */
    @Override
    public PluginPick<MissileAIPlugin> pickMissileAI(MissileAPI missile, ShipAPI launchingShip) {
        SwarmProfile profile = SwarmProfile.forSpec(missile.getProjectileSpecId());
        if (profile == null) {
            return null;
        }

        // A MIRV spawned by another MIRV arrives with a null DamageType. Vanilla BasicShipAI
        // reads that type when sizing up an incoming missile and dies on it -- a hard combat
        // crash the instant the first nested split happens. This hook fires at spawn, before
        // anything can evaluate the missile, which makes it the only safe place to repair it.
        DamageAPI damage = missile.getDamage();
        if (damage != null && damage.getType() == null) {
            damage.setType(profile.getDamageType());
        }

        // guidanceOnly missiles keep their stock AI -- SwarmCoordinator adopts and steers
        // them instead. Replacing a MIRV's AI here would stop it splitting.
        if (profile.isGuidanceOnly()) {
            return null;
        }
        return new PluginPick<MissileAIPlugin>(
                new SwarmMissileAI(missile, profile),
                CampaignPlugin.PickPriority.MOD_SPECIFIC);
    }
}
