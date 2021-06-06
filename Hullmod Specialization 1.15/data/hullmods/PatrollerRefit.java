package data.hullmods;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.impl.hullmods.BaseLogisticsHullMod;

public class PatrollerRefit extends BaseLogisticsHullMod {
	
	public static final int BURN_LEVEL_BONUS = 1;
	public static final float SPEED_MOD = 15f;
	public static final float MANEUVER_MOD = -10f;
	public static final float SENSOR_MOD = 50f;
	public static final float SENSOR_PROFILE_MOD = 100f;
	
	
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
	
		stats.getMaxBurnLevel().modifyFlat(id, BURN_LEVEL_BONUS);
		
		stats.getMaxSpeed().modifyPercent(id, SPEED_MOD);
		
		stats.getAcceleration().modifyMult(id, 1f + MANEUVER_MOD * 0.01f);
		stats.getDeceleration().modifyMult(id, 1f + MANEUVER_MOD * 0.01f);
		stats.getTurnAcceleration().modifyMult(id, 1f + MANEUVER_MOD * 0.01f);
		stats.getMaxTurnRate().modifyMult(id, 1f + MANEUVER_MOD * 0.01f);
		
		stats.getSensorStrength().modifyPercent(id, SENSOR_MOD);
		
		stats.getSensorProfile().modifyPercent(id, SENSOR_PROFILE_MOD);
		
	}
	
	public String getDescriptionParam(int index, HullSize hullSize) {
		
		if (index == 0) return "" + BURN_LEVEL_BONUS;
		if (index == 1) return "" + (int) SPEED_MOD + "%";
		if (index == 2) return "" + (int) MANEUVER_MOD + "%";
		if (index == 3) return "" + (int) SENSOR_MOD + "%";
		if (index == 4) return "" + (int) SENSOR_PROFILE_MOD + "%";
		return null;
	}


}


