package data.hullmods;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.impl.hullmods.BaseLogisticsHullMod;

public class SilentEngines extends BaseLogisticsHullMod {

	private static final int BURN_LEVEL_BONUS = -2;
	public static final float PROFILE_MULT = 0.25f;
	public static final float HEALTH_BONUS = 100f;
	public static final float HULL_BONUS = 10f;
	
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		stats.getEngineHealthBonus().modifyPercent(id, HEALTH_BONUS);
		stats.getHullBonus().modifyPercent(id, HULL_BONUS);
		stats.getHullBonus().modifyPercent(id, HULL_BONUS);
		stats.getSensorProfile().modifyMult(id, PROFILE_MULT);
		stats.getMaxBurnLevel().modifyFlat(id, BURN_LEVEL_BONUS);
	}
	
	public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return "" + (int) HEALTH_BONUS + "%";
		if (index == 1) return "" + (int) HULL_BONUS + "%";
		if (index == 2) return "" + (int) ((1f - PROFILE_MULT) * 100f) + "%";
		if (index == 3) return "" + BURN_LEVEL_BONUS;
		return null;
	}


}
