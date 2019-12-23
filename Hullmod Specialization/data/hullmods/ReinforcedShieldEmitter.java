package data.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;

public class ReinforcedShieldEmitter extends BaseHullMod {

	public static final float SHIELD_UPKEEP_MOD = 25f;
	public static final float SHIELD_MOD = -15f;
	
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		stats.getShieldUpkeepMult().modifyMult(id, 1f + SHIELD_UPKEEP_MOD * 0.01f);
		stats.getShieldDamageTakenMult().modifyMult(id, 1f + SHIELD_MOD * 0.01f);
	}
	
	public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return "" + (int) SHIELD_UPKEEP_MOD + "%";
		if (index == 1) return "" + (int) -SHIELD_MOD + "%";
		return null;
	}

	public boolean isApplicableToShip(ShipAPI ship) {
		return ship != null && ship.getShield() != null;
	}
	
	public String getUnapplicableReason(ShipAPI ship) {
		return "Ship has no shields";
	}
}
