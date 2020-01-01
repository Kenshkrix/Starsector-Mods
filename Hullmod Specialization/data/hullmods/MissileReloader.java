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

	
	public static String MR_DATA_KEY = "core_reloader_data_key";
	
	public static class MissileReloaderData {
		IntervalUtil interval = new IntervalUtil(15f, 20f);
	}
	
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
	}
		
	public String getDescriptionParam(int index, HullSize hullSize) {
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
				int newAmmo = maxAmmo;
				
				if (w.usesAmmo() && currentAmmo < maxAmmo) {
					newAmmo = currentAmmo + (maxAmmo / 4);
					if (newAmmo == currentAmmo){
						newAmmo = newAmmo + 1;
					}
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











