package data.hullmods;

import java.util.HashMap;
import java.util.Map;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ShipAPI;

public class SuperiorTurretGyros extends BaseHullMod {
	
	private static Map mag = new HashMap();
	static {
		mag.put(HullSize.FRIGATE, -30f);
		mag.put(HullSize.DESTROYER, -60f);
		mag.put(HullSize.CRUISER, -120f);
		mag.put(HullSize.CAPITAL_SHIP, -240f);
	}
	
	public static final float TURRET_SPEED_BONUS = 125f;
	
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		stats.getWeaponTurnRateBonus().modifyPercent(id, TURRET_SPEED_BONUS);
		stats.getBeamWeaponTurnRateBonus().modifyPercent(id, TURRET_SPEED_BONUS);
		stats.getArmorBonus().modifyFlat(id, (Float) mag.get(hullSize));
	}
	
	public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return "" + (int) TURRET_SPEED_BONUS + "%";
		if (index == 1) return "" + ((Float) mag.get(HullSize.FRIGATE)).intValue();
		if (index == 2) return "" + ((Float) mag.get(HullSize.DESTROYER)).intValue();
		if (index == 3) return "" + ((Float) mag.get(HullSize.CRUISER)).intValue();
		if (index == 4) return "" + ((Float) mag.get(HullSize.CAPITAL_SHIP)).intValue();
		return null;
	}


}
