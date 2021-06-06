package data.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;

public class IntegratedMagazines extends BaseHullMod {

	public static final float AMMO_BONUS = 50f;
	public static final float HULL_MULT = 0.9f;
	
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		stats.getBallisticAmmoBonus().modifyPercent(id, AMMO_BONUS);
		stats.getEnergyAmmoBonus().modifyPercent(id, AMMO_BONUS);
		stats.getHullBonus().modifyMult(id, HULL_MULT);
	}
	
	public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return "" + (int) AMMO_BONUS + "%";
		if (index == 1) return "" + (int) ((1f - HULL_MULT) * 100f) + "%";
		return null;
	}


}
