package kenshkrix.sssrm.console;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.LocationAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import org.lazywizard.console.BaseCommand;
import org.lazywizard.console.Console;
import org.lwjgl.util.vector.Vector2f;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Debug command: finds the Coronal Hypershunts in the sector and teleports the player to one.
 *
 * <p>Exists to test whether Omega variants spawn naturally -- hypershunts are guarded by
 * Omega fleets, so arriving at one is the quickest way to see what the faction actually
 * fields without waiting to stumble across an encounter.
 *
 * <p>Registered through data/console/commands.csv. Console Commands loads command classes
 * itself, so this creates no dependency on it.
 */
public class GoToHypershunt implements BaseCommand {

    /** Arrive at a distance -- close enough to draw the guardians, not on top of the thing. */
    private static final float ARRIVAL_OFFSET = 600f;

    @Override
    public CommandResult runCommand(String args, CommandContext context) {
        // Deliberately stricter than isInCampaign(): teleporting out from under a docked
        // market interaction leaves the dialog pointing at somewhere you no longer are.
        if (context != CommandContext.CAMPAIGN_MAP) {
            Console.showMessage("Only usable on the campaign map, and not while docked.");
            return CommandResult.WRONG_CONTEXT;
        }

        try {
            CampaignFleetAPI player = Global.getSector().getPlayerFleet();
            List<SectorEntityToken> shunts =
                    new ArrayList<>(Global.getSector().getEntitiesWithTag(Tags.CORONAL_TAP));
            if (shunts.isEmpty()) {
                Console.showMessage("No coronal hypershunt exists in this sector.");
                return CommandResult.ERROR;
            }

            // Nearest first, measured in hyperspace so it holds across systems.
            final Vector2f origin = player.getLocationInHyperspace();
            Collections.sort(shunts, new Comparator<SectorEntityToken>() {
                @Override
                public int compare(SectorEntityToken a, SectorEntityToken b) {
                    return Float.compare(hyperDist(origin, a), hyperDist(origin, b));
                }
            });

            int index = 0;
            if (args != null && !args.trim().isEmpty()) {
                try {
                    index = Integer.parseInt(args.trim()) - 1;
                } catch (NumberFormatException e) {
                    return CommandResult.BAD_SYNTAX;
                }
                if (index < 0 || index >= shunts.size()) {
                    Console.showMessage("Pick 1-" + shunts.size() + "; found "
                            + shunts.size() + " hypershunt(s).");
                    return CommandResult.BAD_SYNTAX;
                }
            }

            SectorEntityToken target = shunts.get(index);
            LocationAPI destination = target.getContainingLocation();
            if (destination == null) {
                Console.showMessage("That hypershunt is not in a loaded location.");
                return CommandResult.ERROR;
            }

            player.clearAssignments();
            LocationAPI current = player.getContainingLocation();
            if (current != destination) {
                current.removeEntity(player);
                destination.addEntity(player);
                Global.getSector().setCurrentLocation(destination);
            }
            player.setLocation(target.getLocation().x + ARRIVAL_OFFSET,
                    target.getLocation().y + ARRIVAL_OFFSET);

            Console.showMessage("Moved to " + describe(target) + ", "
                    + (int) ARRIVAL_OFFSET + " units out.");

            // Report the guardians, since "did the refit apply" is the usual reason to be here.
            com.fs.starfarer.api.campaign.CampaignFleetAPI defenders =
                    kenshkrix.sssrm.OmegaDefenderScript.defenderFleetOf(target);
            if (defenders == null) {
                Console.showMessage("  No defender fleet attached -- these are built inside "
                        + "the dialog and discarded afterwards, so this is expected. The "
                        + "refit is applied while the dialog is open instead.");
            } else {
                kenshkrix.sssrm.OmegaDefenderScript.refitDefendersOf(target);
                int tesseracts = 0, armed = 0;
                for (com.fs.starfarer.api.fleet.FleetMemberAPI m
                        : defenders.getFleetData().getMembersListCopy()) {
                    if ("tesseract".equals(m.getHullId())) {
                        tesseracts++;
                    }
                    if (m.getVariant() != null
                            && m.getVariant().getNonBuiltInWeaponSlots() != null) {
                        for (String s : m.getVariant().getNonBuiltInWeaponSlots()) {
                            if ("sssrm_resonance".equals(m.getVariant().getWeaponId(s))) {
                                armed++;
                                break;
                            }
                        }
                    }
                }
                Console.showMessage("  Guardians: " + defenders.getFleetData().getMembersListCopy().size()
                        + " ships, " + tesseracts + " Tesseract(s), "
                        + armed + " carrying the Cascade Resonator.");
            }
            if (shunts.size() > 1) {
                Console.showMessage("Sector has " + shunts.size()
                        + " hypershunts; pass an index to pick another:");
                for (int i = 0; i < shunts.size(); i++) {
                    Console.showMessage("  " + (i + 1) + ". " + describe(shunts.get(i)));
                }
            }
            return CommandResult.SUCCESS;
        } catch (Exception e) {
            Console.showException("Failed to reach a hypershunt", e);
            return CommandResult.ERROR;
        }
    }

    private static float hyperDist(Vector2f from, SectorEntityToken to) {
        Vector2f at = to.getLocationInHyperspace();
        if (from == null || at == null) {
            return Float.MAX_VALUE;
        }
        float dx = at.x - from.x, dy = at.y - from.y;
        return (float) Math.sqrt(dx * dx + dy * dy);
    }

    private static String describe(SectorEntityToken shunt) {
        String system = shunt.getStarSystem() == null
                ? "hyperspace"
                : shunt.getStarSystem().getBaseName();
        return shunt.getName() + " (" + system + ")";
    }
}
