package data.hullmods;

import java.util.HashMap;
import java.util.Map;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ShipAPI;


public class GaussCoils extends BaseHullMod {
	
	public static final float BALLISTIC_FLUX_MOD = 15f;
	public static final float BALLISTIC_ROF_MOD = 20f;
	
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		stats.getBallisticWeaponFluxCostMod().modifyPercent(id,BALLISTIC_FLUX_MOD);
		
		stats.getBallisticRoFMult().modifyPercent(id,BALLISTIC_ROF_MOD);
	}
	
	@Override
    public String getDescriptionParam(int index, HullSize hullSize) {
        if (index == 0) return "" + (int) BALLISTIC_FLUX_MOD + "%";
        if (index == 1) return "" + (int) BALLISTIC_ROF_MOD + "%";
        return null;
    }
}
