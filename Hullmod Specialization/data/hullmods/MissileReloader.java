package data.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.WeaponAPI.WeaponType;
import com.fs.starfarer.api.util.IntervalUtil;

public class MissileReloader extends BaseHullMod {

	public static final float MIN_RELOAD_TIME = 25f;
	public static final float MAX_RELOAD_TIME = 30f;
	public static final float RELOAD_FRACTION_NUMERATOR = 1f;
	public static final float RELOAD_FRACTION_DENOMINATOR = 4f;
	
	public static String MR_DATA_KEY = "core_reloader_data_key";
	
	public static class MissileReloaderData {
		IntervalUtil interval = new IntervalUtil(MIN_RELOAD_TIME, MAX_RELOAD_TIME);
	}
	
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
	}
		
	public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return "" + (int) MIN_RELOAD_TIME;
		if (index == 1) return "" + (int) MAX_RELOAD_TIME;
		if (index == 2) return "" + (int) RELOAD_FRACTION_NUMERATOR;
		if (index == 3) return "" + (int) RELOAD_FRACTION_DENOMINATOR;
		return null;
	}
	
	@Override
	public void advanceInCombat(ShipAPI ship, float amount) {
		super.advanceInCombat(ship, amount);

		if (!ship.isAlive()) return;
		
		CombatEngineAPI engine = Global.getCombatEngine();
		
		String key = MR_DATA_KEY + "_" + ship.getId();
		MissileReloaderData data = (MissileReloaderData) engine.getCustomData().get(key);
		if (data == null) {
			data = new MissileReloaderData();
			engine.getCustomData().put(key, data);
		}
		
		data.interval.advance(amount);
		if (data.interval.intervalElapsed()) {
			for (WeaponAPI w : ship.getAllWeapons()) {
				if (w.getType() != WeaponType.MISSILE) continue;
				int currentAmmo = w.getAmmo();
				int maxAmmo = w.getMaxAmmo();
				
				if (w.usesAmmo() && currentAmmo < maxAmmo) {
					float numerator = maxAmmo * RELOAD_FRACTION_NUMERATOR;
					float reloadCount = numerator / RELOAD_FRACTION_DENOMINATOR;
					if (reloadCount < 1){
						reloadCount = 1;
					}
					int newAmmo = currentAmmo + (int)reloadCount;
					if (newAmmo > maxAmmo){
						w.setAmmo(maxAmmo);
					} else {
						w.setAmmo(newAmmo);
					}
				}
			}
		}
		
	}
	
}











