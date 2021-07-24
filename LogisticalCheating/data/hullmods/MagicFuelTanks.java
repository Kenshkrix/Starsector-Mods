package data.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;

public class MagicFuelTanks extends BaseHullMod {

	public static final float TANK_OF_HOLDING = 5000f;
	
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		stats.getFuelMod().modifyFlat(id, TANK_OF_HOLDING);
	}
	
	public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return "" + TANK_OF_HOLDING;
		return null;
	}

}




