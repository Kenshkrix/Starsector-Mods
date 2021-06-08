package data.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.Global;

public class ScaffoldingInvisible extends BaseHullMod {
	
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		ShipVariantAPI ship = stats.getVariant();
		if (ship.getUnusedOP(Global.getSector().getCharacterData().getPerson().getFleetCommanderStats()) < 100){
			ship.addMod("khs_scaffolding");
		}else{
			ship.removeMod("khs_scaffolding_invisible");
		}
	}
}


