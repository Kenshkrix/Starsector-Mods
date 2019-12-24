package data.hullmods;

import java.util.HashMap;
import java.util.Map;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShieldAPI;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.util.IntervalUtil;

@SuppressWarnings("unchecked")
public class FluxNetwork extends BaseHullMod {

	private static Map mag = new HashMap();
	static {
		mag.put(HullSize.FRIGATE, 40f);
		mag.put(HullSize.DESTROYER, 80f);
		mag.put(HullSize.CRUISER, 120f);
		mag.put(HullSize.CAPITAL_SHIP, 200f);
	}
	
	public static class FluxNetworkData {
		IntervalUtil interval = new IntervalUtil(1f, 1f);
	}
	
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		stats.getFluxDissipation().modifyFlat(id, (Float) mag.get(hullSize));
	}
	
	@Override
	public void advanceInCombat(ShipAPI ship, float amount) {
		super.advanceInCombat(ship, amount);

		if (!ship.isAlive()) return;
		
		CombatEngineAPI engine = Global.getCombatEngine();
		
		String key = "core_network_data_key" + "_" + ship.getId();
		FluxNetworkData data = (FluxNetworkData) engine.getCustomData().get(key);
		if (data == null) {
			data = new FluxNetworkData();
			engine.getCustomData().put(key, data);
		}
		
		data.interval.advance(amount);
		if (data.interval.intervalElapsed()) {
			if (ship.getCurrFlux() > (ship.getMaxFlux() / 2f)){
				ship.getFluxTracker().increaseFlux(((Float) mag.get(ship.getHullSize())), true);
			}
		}
		
	}
	
	public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return "" + ((Float) mag.get(HullSize.FRIGATE)).intValue();
		if (index == 1) return "" + ((Float) mag.get(HullSize.DESTROYER)).intValue();
		if (index == 2) return "" + ((Float) mag.get(HullSize.CRUISER)).intValue();
		if (index == 3) return "" + ((Float) mag.get(HullSize.CAPITAL_SHIP)).intValue();
		return null;
	}


}
