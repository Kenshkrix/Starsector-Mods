package data.hullmods;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ShipAPI;


public class TunedBeamArrays extends BaseHullMod {
	
	public static final float BEAM_FLUX_MOD = -10f;
	public static final float BEAM_RANGE_MOD = -35f;
	
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		stats.getBeamWeaponFluxCostMult().modifyPercent(id,BEAM_FLUX_MOD);
		stats.getBeamWeaponRangeBonus().modifyPercent(id,BEAM_RANGE_MOD);
		stats.getBeamPDWeaponRangeBonus().modifyPercent(id,-BEAM_RANGE_MOD);
	}
	
	@Override
    public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return "" + (int) BEAM_FLUX_MOD + "%";
		if (index == 1) return "" + (int) BEAM_RANGE_MOD + "%";
        return null;
    }
}
