package data.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;

@SuppressWarnings("unchecked")
public class TunedSensors extends BaseHullMod {

	private static final float SIGHT_INCREASE = 50f;
	private static final float SENSOR_INCREASE = 40f;
	
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		stats.getSightRadiusMod().modifyPercent(id, SIGHT_INCREASE);
		stats.getSensorStrength().modifyFlat(id, SENSOR_INCREASE);
	}
	
	public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return "" + (int) SIGHT_INCREASE + "%";
		if (index == 1) return "" + (int) SENSOR_INCREASE;
		return null;
	}


}
