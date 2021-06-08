package data.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.Global;

public class Scaffolding extends BaseHullMod {
	
	public static final float SPEED_MULT = 0f;
	public static final float BURN_LEVEL_MULT = 0f;
	public static final float MANEUVER_MULT = 0f;
	
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		ShipVariantAPI ship = stats.getVariant();
		if (ship.getUnusedOP(Global.getSector().getCharacterData().getPerson().getFleetCommanderStats()) < 100){
			ship.addMod("khs_scaffolding_invisible");
		}
	
		stats.getMaxSpeed().modifyMult(id, SPEED_MULT);
		
		stats.getMaxBurnLevel().modifyMult(id, BURN_LEVEL_MULT);
		
		stats.getAcceleration().modifyMult(id, MANEUVER_MULT);
		stats.getDeceleration().modifyMult(id, MANEUVER_MULT);
		stats.getTurnAcceleration().modifyMult(id, MANEUVER_MULT);
		stats.getMaxTurnRate().modifyMult(id, MANEUVER_MULT);
	}
}


