package data.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;

public class RapidShieldEmitter extends BaseHullMod {

	public static final float SHIELD_BONUS_TURN = 200f;
	public static final float SHIELD_BONUS_UNFOLD = 200f;
	public static final float SHIELD_ARC_MOD = -30f;
	
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		stats.getShieldTurnRateMult().modifyPercent(id, SHIELD_BONUS_TURN);
		stats.getShieldUnfoldRateMult().modifyPercent(id, SHIELD_BONUS_UNFOLD);
		stats.getShieldArcBonus().modifyFlat(id, SHIELD_ARC_MOD);
	}
	
	public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return "" + (int) SHIELD_BONUS_TURN + "%";
		if (index == 1) return "" + (int) SHIELD_BONUS_UNFOLD + "%";
		if (index == 2) return "" + (int) SHIELD_ARC_MOD;
		return null;
	}

	public boolean isApplicableToShip(ShipAPI ship) {
		
		return ship != null && ship.getShield() != null;
	}
	
	public String getUnapplicableReason(ShipAPI ship) {
		return "Ship has no shields";
	}
}
