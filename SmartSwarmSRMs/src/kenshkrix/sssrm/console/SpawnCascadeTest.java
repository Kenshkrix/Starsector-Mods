package kenshkrix.sssrm.console;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.FleetAssignment;
import com.fs.starfarer.api.campaign.LocationAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.ids.FleetTypes;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import org.lazywizard.console.BaseCommand;
import org.lazywizard.console.Console;

/**
 * Debug command: drops a hostile Omega fleet of Tesseracts next to the player so the Cascade
 * Resonator can be tested without waiting for one to spawn naturally.
 *
 * <p>Registered through data/console/commands.csv. Console Commands loads command classes
 * itself, so this creates no dependency -- if that mod isn't enabled, the CSV is never read
 * and this class is never touched.
 */
public class SpawnCascadeTest implements BaseCommand {

    private static final String VARIANT = "tesseract_Cascade";
    private static final int DEFAULT_COUNT = 2;
    /** Far enough to see them approach, close enough not to hunt for them. */
    private static final float SPAWN_DISTANCE = 800f;

    @Override
    public CommandResult runCommand(String args, CommandContext context) {
        if (!context.isInCampaign()) {
            Console.showMessage("This command is only usable on the campaign map.");
            return CommandResult.WRONG_CONTEXT;
        }

        int count = DEFAULT_COUNT;
        if (args != null && !args.trim().isEmpty()) {
            try {
                count = Integer.parseInt(args.trim());
            } catch (NumberFormatException e) {
                return CommandResult.BAD_SYNTAX;
            }
            if (count < 1 || count > 20) {
                Console.showMessage("Count must be between 1 and 20.");
                return CommandResult.BAD_SYNTAX;
            }
        }

        try {
            SectorEntityToken player = Global.getSector().getPlayerFleet();
            LocationAPI location = player.getContainingLocation();

            CampaignFleetAPI fleet = Global.getFactory()
                    .createEmptyFleet("omega", FleetTypes.PATROL_LARGE, true);

            for (int i = 0; i < count; i++) {
                FleetMemberAPI member = fleet.getFleetData().addFleetMember(VARIANT);
                if (member == null) {
                    Console.showMessage("Could not create variant '" + VARIANT
                            + "'. Is SmartSwarmSRMs enabled?");
                    return CommandResult.ERROR;
                }
                member.getRepairTracker().setCR(member.getRepairTracker().getMaxCR());
            }

            fleet.setName("Cascade Test");
            fleet.getFleetData().setSyncNeeded();
            fleet.forceSync();
            fleet.inflateIfNeeded();

            location.addEntity(fleet);
            fleet.setLocation(player.getLocation().x + SPAWN_DISTANCE, player.getLocation().y);

            // Omega is hostile to everything by default, but say so explicitly and make it
            // come to you -- otherwise it sits there and the test needs a manual approach.
            fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_MAKE_HOSTILE, true);
            fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_MAKE_AGGRESSIVE, true);
            fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_MAKE_ALWAYS_PURSUE, true);
            fleet.addAssignment(FleetAssignment.INTERCEPT, player, 1000f, "intercepting you");

            Console.showMessage("Spawned a hostile Omega fleet with " + count + "x "
                    + VARIANT + ", " + (int) SPAWN_DISTANCE + " units away and closing.");
            return CommandResult.SUCCESS;
        } catch (Exception e) {
            Console.showException("Failed to spawn the test fleet", e);
            return CommandResult.ERROR;
        }
    }
}
