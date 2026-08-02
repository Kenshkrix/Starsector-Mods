package kenshkrix.sssrm;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CampaignUIAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;

/**
 * Catches defender fleets that only exist inside an interaction dialog.
 *
 * <p>A hypershunt's guardians are not a campaign fleet in the usual sense. They are built
 * lazily when the player triggers the defences, stashed on the entity under
 * {@code $defenderFleet}, and handed straight to a fleet interaction. They never join the star
 * system and they are not persisted, so nothing reports them spawning and there is nothing to
 * find before or after. Three earlier approaches -- a spawn listener, a scan of
 * {@code location.getFleets()}, and a daily sweep of salvage entities -- all missed them for
 * that reason.
 *
 * <p>This watches the open dialog instead. Two things make that work: it runs while paused
 * (the campaign clock is stopped for the whole dialog) and it only ever looks at the entity
 * the player is actually talking to, so it cannot poke memory on unrelated entities.
 */
public class OmegaDefenderScript implements EveryFrameScript {

    /** Where SalvageDefenderInteraction reads the guardians from. */
    public static final String DEFENDER_FLEET_KEY = "$defenderFleet";

    /** Frames, not game time -- the clock does not advance while a dialog is open. */
    private static final int CHECK_EVERY_FRAMES = 5;

    private int frames;

    @Override
    public boolean isDone() {
        return false;
    }

    /** Must be true: the guardians only exist while the dialog is up, and that pauses. */
    @Override
    public boolean runWhilePaused() {
        return true;
    }

    @Override
    public void advance(float amount) {
        if (++frames < CHECK_EVERY_FRAMES) {
            return;
        }
        frames = 0;
        checkOpenDialog();
    }

    /**
     * Refits the defender fleet of whatever the player currently has open, if it has one.
     *
     * <p>Scoped to the interaction target on purpose. Reading {@code $defenderFleet} off every
     * salvageable entity in the sector would be both wasteful and risky -- the fleet appears
     * to be materialised on access, so touching unrelated entities could have side effects.
     */
    public static boolean checkOpenDialog() {
        CampaignUIAPI ui = Global.getSector() == null ? null : Global.getSector().getCampaignUI();
        if (ui == null || !ui.isShowingDialog()) {
            return false;
        }
        InteractionDialogAPI dialog = ui.getCurrentInteractionDialog();
        if (dialog == null) {
            return false;
        }
        return refitDefendersOf(dialog.getInteractionTarget());
    }

    /** @return true if a defender fleet was found and processed by this call */
    public static boolean refitDefendersOf(SectorEntityToken entity) {
        CampaignFleetAPI defenders = defenderFleetOf(entity);
        return defenders != null && OmegaRefitListener.process(defenders);
    }

    /** Null unless this entity is currently holding a defender fleet. */
    public static CampaignFleetAPI defenderFleetOf(SectorEntityToken entity) {
        if (entity == null || entity.getMemoryWithoutUpdate() == null) {
            return null;
        }
        if (!entity.getMemoryWithoutUpdate().contains(DEFENDER_FLEET_KEY)) {
            return null;
        }
        try {
            return entity.getMemoryWithoutUpdate().getFleet(DEFENDER_FLEET_KEY);
        } catch (Exception e) {
            return null;
        }
    }
}
