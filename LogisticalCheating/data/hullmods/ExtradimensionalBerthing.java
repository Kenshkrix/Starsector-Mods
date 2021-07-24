package data.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;

public class ExtradimensionalBerthing extends BaseHullMod {

	public static final float MAGNIFICENT_MANOR = 5000f;
	
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		stats.getMaxCrewMod().modifyFlat(id, MAGNIFICENT_MANOR);
	}
	
	public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return "" + MAGNIFICENT_MANOR;
		return null;
	}

}




