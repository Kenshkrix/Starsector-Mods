package data.hullmods;

import java.util.HashMap;
import java.util.Map;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;

public class ExoticArmor extends BaseHullMod {

	public static final float MANEUVER_MOD = 25f;
	public static final float HULL_MOD = -30f;
	
	private static Map mag = new HashMap();
	static {
		mag.put(HullSize.FRIGATE, 80f);
		mag.put(HullSize.DESTROYER, 160f);
		mag.put(HullSize.CRUISER, 240f);
		mag.put(HullSize.CAPITAL_SHIP, 320f);
	}
	
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		
		stats.getArmorBonus().modifyFlat(id, (Float) mag.get(hullSize));
		
		stats.getAcceleration().modifyMult(id, 1f + MANEUVER_MOD * 0.01f);
		stats.getDeceleration().modifyMult(id, 1f + MANEUVER_MOD * 0.01f);
		stats.getTurnAcceleration().modifyMult(id, 1f + MANEUVER_MOD * 0.01f);
		stats.getMaxTurnRate().modifyMult(id, 1f + MANEUVER_MOD * 0.01f);
		
		stats.getHullBonus().modifyPercent(id, HULL_MOD);
		
		//stats.getCargoMod().modifyFlat(id, -70);
	}
	
	public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return "" + ((Float) mag.get(HullSize.FRIGATE)).intValue();
		if (index == 1) return "" + ((Float) mag.get(HullSize.DESTROYER)).intValue();
		if (index == 2) return "" + ((Float) mag.get(HullSize.CRUISER)).intValue();
		if (index == 3) return "" + ((Float) mag.get(HullSize.CAPITAL_SHIP)).intValue();
		if (index == 4) return "" + (int) HULL_MOD + "%";
		if (index == 5) return "" + (int) MANEUVER_MOD + "%";
		if (index == 6) return "" + ((Float) mag.get(hullSize)).intValue();
		return null;
	}
}
