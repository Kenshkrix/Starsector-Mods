package data.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;

public class MagicCargoHolds extends BaseHullMod {

	public static final float BAG_OF_HOLDING = 5000f;
	
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		stats.getCargoMod().modifyFlat(id, BAG_OF_HOLDING);
	}
	
	public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return "" + BAG_OF_HOLDING;
		return null;
	}

}




