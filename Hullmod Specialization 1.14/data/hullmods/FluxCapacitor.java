package data.hullmods;

import java.util.HashMap;
import java.util.Map;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;

@SuppressWarnings("unchecked")
public class FluxCapacitor extends BaseHullMod {

	private static Map mag1 = new HashMap();
	static {
		mag1.put(HullSize.FRIGATE, 900f);
		mag1.put(HullSize.DESTROYER, 1800f);
		mag1.put(HullSize.CRUISER, 2700f);
		mag1.put(HullSize.CAPITAL_SHIP, 4500f);
	}
	private static Map mag2 = new HashMap();
	static {
		mag2.put(HullSize.FRIGATE, -20f);
		mag2.put(HullSize.DESTROYER, -40f);
		mag2.put(HullSize.CRUISER, -60f);
		mag2.put(HullSize.CAPITAL_SHIP, -90f);
	}
	
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		stats.getFluxCapacity().modifyFlat(id, (Float) mag1.get(hullSize));
		stats.getFluxDissipation().modifyFlat(id, (Float) mag2.get(hullSize));
	}
	
	public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return "" + ((Float) mag1.get(HullSize.FRIGATE)).intValue();
		if (index == 1) return "" + ((Float) mag1.get(HullSize.DESTROYER)).intValue();
		if (index == 2) return "" + ((Float) mag1.get(HullSize.CRUISER)).intValue();
		if (index == 3) return "" + ((Float) mag1.get(HullSize.CAPITAL_SHIP)).intValue();
		if (index == 4) return "" + ((Float) mag2.get(HullSize.FRIGATE)).intValue();
		if (index == 5) return "" + ((Float) mag2.get(HullSize.DESTROYER)).intValue();
		if (index == 6) return "" + ((Float) mag2.get(HullSize.CRUISER)).intValue();
		if (index == 7) return "" + ((Float) mag2.get(HullSize.CAPITAL_SHIP)).intValue();
		return null;
	}


}
