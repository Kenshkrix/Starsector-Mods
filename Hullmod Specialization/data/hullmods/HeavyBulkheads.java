package data.hullmods;

import java.util.HashMap;
import java.util.Map;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.impl.campaign.ids.Stats;

public class HeavyBulkheads extends BaseHullMod {
	
	public static final float MANEUVER_MOD = -35f;
	public static final float HULL_MOD = 30f;
	private static Map mag = new HashMap();
	static {
		mag.put(HullSize.FRIGATE, 30f);
		mag.put(HullSize.DESTROYER, 60f);
		mag.put(HullSize.CRUISER, 90f);
		mag.put(HullSize.CAPITAL_SHIP, 120f);
	}

	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		
		stats.getArmorBonus().modifyFlat(id, (Float) mag.get(hullSize));
		
		stats.getAcceleration().modifyMult(id, 1f + MANEUVER_MOD * 0.01f);
		stats.getDeceleration().modifyMult(id, 1f + MANEUVER_MOD * 0.01f);
		stats.getTurnAcceleration().modifyMult(id, 1f + MANEUVER_MOD * 0.01f);
		stats.getMaxTurnRate().modifyMult(id, 1f + MANEUVER_MOD * 0.01f);
		
		stats.getHullBonus().modifyPercent(id, HULL_MOD);
		
		stats.getDynamic().getMod(Stats.INDIVIDUAL_SHIP_RECOVERY_MOD).modifyFlat(id, 1000f);
		stats.getBreakProb().modifyMult(id, 0f);
	}
	
	public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return "" + (int) HULL_MOD + "%";
		if (index == 1) return "" + ((Float) mag.get(HullSize.FRIGATE)).intValue();
		if (index == 2) return "" + ((Float) mag.get(HullSize.DESTROYER)).intValue();
		if (index == 3) return "" + ((Float) mag.get(HullSize.CRUISER)).intValue();
		if (index == 4) return "" + ((Float) mag.get(HullSize.CAPITAL_SHIP)).intValue();
		if (index == 5) return "" + (int) MANEUVER_MOD + "%";
		return null;
	}
}



