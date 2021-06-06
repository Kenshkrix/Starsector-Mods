package data.hullmods;

import java.util.HashMap;
import java.util.Map;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ShipAPI;


public class Supercapacitors extends BaseHullMod {
	
	public static final float ENERGY_DAMAGE_MOD = 15f;
	public static final float ENERGY_FLUX_MOD = 15f;
	public static final float ENERGY_ROF_MOD = -10f;
	
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		stats.getEnergyWeaponDamageMult().modifyPercent(id,ENERGY_DAMAGE_MOD);
		stats.getBeamWeaponDamageMult().modifyPercent(id,ENERGY_DAMAGE_MOD);
		
		stats.getEnergyWeaponFluxCostMod().modifyPercent(id,ENERGY_FLUX_MOD);
		stats.getBeamWeaponFluxCostMult().modifyPercent(id,ENERGY_FLUX_MOD);
		
		stats.getEnergyRoFMult().modifyPercent(id,ENERGY_ROF_MOD);
	}
	
	@Override
    public String getDescriptionParam(int index, HullSize hullSize) {
        if (index == 0) return "" + (int) ENERGY_DAMAGE_MOD + "%";
        if (index == 1) return "" + (int) ENERGY_FLUX_MOD + "%";
        if (index == 2) return "" + (int) ENERGY_ROF_MOD + "%";
        return null;
    }
}
