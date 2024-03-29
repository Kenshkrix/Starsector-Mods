package data.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;

public class ExternalCargoContainer extends BaseHullMod {

	private static final int BURN_LEVEL_MOD = -2;
	public static final float SPEED_MOD = -15f;
	public static final float MANEUVER_MOD = -15f;
	public static final float ARMOR_MOD = -15f;
	public static final float CARGO_MOD = 50f;
	
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		stats.getMaxBurnLevel().modifyFlat(id, BURN_LEVEL_MOD);
		
		stats.getMaxSpeed().modifyPercent(id, SPEED_MOD);
		
		stats.getAcceleration().modifyPercent(id, MANEUVER_MOD * 2f);
		stats.getDeceleration().modifyPercent(id, MANEUVER_MOD);
		stats.getTurnAcceleration().modifyPercent(id, MANEUVER_MOD * 2f);
		stats.getMaxTurnRate().modifyPercent(id, MANEUVER_MOD);
		
		stats.getArmorBonus().modifyPercent(id, ARMOR_MOD);
		
		stats.getCargoMod().modifyPercent(id, CARGO_MOD);
	}
	
	public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return "" + BURN_LEVEL_MOD;
		if (index == 1) return "" + CARGO_MOD + "%";
		if (index == 2) return "" + SPEED_MOD + "%";
		if (index == 3) return "" + MANEUVER_MOD + "%";
		if (index == 4) return "" + ARMOR_MOD + "%";
		return null;
	}

}




