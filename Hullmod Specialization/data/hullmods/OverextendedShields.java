package data.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;

public class OverextendedShields extends BaseHullMod {

	public static final float SHIELD_ARC_MOD = 90f;
	public static final float SHIELD_MOD = 10f;
	public static final float SHIELD_TURN_MULT = 0.5f;
	public static final float SHIELD_UNFOLD_MULT = 0.75f;
	
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		stats.getShieldTurnRateMult().modifyMult(id, SHIELD_TURN_MULT);
		stats.getShieldUnfoldRateMult().modifyMult(id, SHIELD_UNFOLD_MULT);
		stats.getShieldArcBonus().modifyFlat(id, SHIELD_ARC_MOD);
		stats.getShieldDamageTakenMult().modifyMult(id, 1f + SHIELD_MOD * 0.01f);
	}
	
	public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return "" + (int) SHIELD_ARC_MOD;
		if (index == 1) return "" + (int) SHIELD_MOD + "%";
		if (index == 2) return "" + (int) -((1f - SHIELD_TURN_MULT) * 100f) + "%";
		if (index == 3) return "" + (int) -((1f - SHIELD_UNFOLD_MULT) * 100f) + "%";
		return null;
	}

	public boolean isApplicableToShip(ShipAPI ship) {
		
		return ship != null && ship.getShield() != null;
	}
	
	public String getUnapplicableReason(ShipAPI ship) {
		return "Ship has no shields";
	}
}
