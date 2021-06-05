package data.hullmods;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ShipAPI;


public class BallisticCalibrationROF extends BaseHullMod {
	
	public static final float BALLISTIC_DAMAGE_MOD = -5f;
	public static final float BALLISTIC_FLUX_MOD = 5f;
	public static final float BALLISTIC_RANGE_MOD = -5f;
	public static final float BALLISTIC_ROF_MOD = 20f;
	
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		stats.getBallisticWeaponDamageMult().modifyPercent(id,BALLISTIC_DAMAGE_MOD);
		stats.getBallisticWeaponFluxCostMod().modifyPercent(id,BALLISTIC_FLUX_MOD);
		stats.getBallisticWeaponRangeBonus().modifyPercent(id,BALLISTIC_RANGE_MOD);
		stats.getBallisticRoFMult().modifyPercent(id,BALLISTIC_ROF_MOD);
	}
	
	@Override
    public String getDescriptionParam(int index, HullSize hullSize) {
        if (index == 0) return "" + (int) BALLISTIC_DAMAGE_MOD + "%";
		if (index == 1) return "" + (int) BALLISTIC_FLUX_MOD + "%";
		if (index == 2) return "" + (int) BALLISTIC_RANGE_MOD + "%";
		if (index == 3) return "" + (int) BALLISTIC_ROF_MOD + "%";
        return null;
    }
	
	public boolean isApplicableToShip(ShipAPI ship) {
		LinkedHashSet<String> shipHullMods = (LinkedHashSet<String>)ship.getVariant().getHullMods();
		return !(shipHullMods.contains("khs_ballistic_calibration_damage") || shipHullMods.contains("khs_ballistic_calibration_flux") || shipHullMods.contains("khs_ballistic_calibration_range"));
	}
}
