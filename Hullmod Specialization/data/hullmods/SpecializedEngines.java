package data.hullmods;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.impl.hullmods.BaseLogisticsHullMod;

public class SpecializedEngines extends BaseLogisticsHullMod {
	
	public static final int BURN_LEVEL_BONUS = 5;
	public static final float MANEUVER_MOD = -45f;
	public static final float ARMOR_PENALTY_MULT = 0.5f;
	
	
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
	
		stats.getMaxBurnLevel().modifyFlat(id, BURN_LEVEL_BONUS);
		
		stats.getAcceleration().modifyMult(id, 1f + MANEUVER_MOD * 0.01f);
		stats.getDeceleration().modifyMult(id, 1f + MANEUVER_MOD * 0.01f);
		stats.getTurnAcceleration().modifyMult(id, 1f + MANEUVER_MOD * 0.01f);
		stats.getMaxTurnRate().modifyMult(id, 1f + MANEUVER_MOD * 0.01f);
		
		stats.getArmorBonus().modifyMult(id, ARMOR_PENALTY_MULT);
	}
	
	public String getDescriptionParam(int index, HullSize hullSize) {
		
		if (index == 0) return "" + BURN_LEVEL_BONUS;
		if (index == 1) return "" + ((int) MANEUVER_MOD) + "%";
		return null;
	}


}


